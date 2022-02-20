package net.timardo.lt3dimporter;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

import de.javagl.obj.Mtl;
import de.javagl.obj.MtlWriter;
import de.javagl.obj.Mtls;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjWriter;
import de.javagl.obj.Objs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.timardo.lt3dimporter.obj3d.LightObjFace;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import static net.timardo.lt3dimporter.Utils.*;

public class Exporter implements Runnable {
    
    private ICommandSender sender;

    public Exporter(MinecraftServer server, ICommandSender sender) {
        this.sender = sender;
    }
    
    @Override
    public void run() {
        System.out.println("exporting");
        ItemStack item = ((EntityPlayer)this.sender).getHeldItemMainhand();
        
        if (item == ItemStack.EMPTY || !(item.getItem() instanceof ItemLittleRecipeAdvanced)) {
            postMessage(this.sender, TextFormatting.RED + "Empty hand or not a blueprint!");
            return;
        }
        
        Obj obj = Objs.create();
        List<Mtl> mtls = new ArrayList<Mtl>();
        exportModel(item, obj, mtls);

        try {
            OutputStream mtlOutputStream = new FileOutputStream("exported.mtl");
            MtlWriter.write(mtls, mtlOutputStream);
            OutputStream objOutputStream = new FileOutputStream("exported.obj");
            obj.setMtlFileNames(Arrays.asList("exported.mtl"));
            ObjWriter.write(obj, objOutputStream);
            mtlOutputStream.close();
            objOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Method to convert {@link ItemStack} data to a 3D model. Method has bunch of comments mainly for me becuase I know I would get lost easily in that mess
     * 
     * @param stack - stack containing an LT structure
     * @param obj - {@link Obj} in which the result should be written
     * @param mtls - materials for the obj
     */
    public void exportModel(ItemStack stack, Obj obj, List<Mtl> mtls) {
        int verticesCount = 0;
        List<? extends RenderBox> cubes = LittlePreview.getCubes(stack, false);
        Map<Integer, Map<Long, Integer>> vertices = new HashMap<Integer, Map<Long, Integer>>(); // all vertices with their indices stored as Long from BlockPos TODO check RAM usage/possibility of buffering/file cache
        Map<Long, Integer> textureCoords = new HashMap<Long, Integer>(); // all texture coordinates with their indices stored as Long from two float values
        //Map<Integer, Integer> normalMap = new HashMap<Integer, Integer>(); // all normals
        Map<String, Map<Integer, Integer>> textures = new HashMap<String, Map<Integer, Integer>>(); // all textures in format texturename->map of different colors mapped to their indices
        //Map<Long, Map<Long, Tuple<int[], String>>> uniqueFaces = new HashMap<Long, Map<Long, Tuple<int[], String>>>(); // unique faces - will use later probably
        Map<String, Map<Long, Map<Long, List<int[]>>>> materialGroups = new HashMap<String, Map<Long, Map<Long, List<int[]>>>>(); // map materials
        
        for (int i = 0; i < cubes.size(); i++) {
            RenderBox cube = cubes.get(i);
            IBakedModel blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(cube.getBlockState());
            // System.out.println(blockModel.getClass());
            List<BakedQuad> quads = new ArrayList<BakedQuad>(); // all quads making up this tile
            Arrays.asList(EnumFacing.values()).forEach(facing -> quads.addAll(CreativeBakedModel.getBakedQuad(null, cube, null, cube.getOffset(), cube.getBlockState(), blockModel, null, facing, 0, false)));
            
            // TODO rework this part to implement support for merginf duplicate faces with compound textures because Blender deletes them..
            
            for (BakedQuad quad : quads) { // TODO: add support for biome-dependent textures
                // build data structure
                int[] vertexData = quad.getVertexData(); // data containing position, normals, UV and color - no purpose found yet
                VertexFormat format = quad.getFormat(); // get format of data, AFAIK should be DefaultVertexFormats.ITEM TODO check if this is always the case
                int index = 0;
                int uvOffset = format.getUvOffsetById(0) / 4; // divided by 4 because offset is in bytes and each integer in int[] has 4 bytes
                //int normalOffset = format.getNormalOffset() / 4;
                Map<Integer, Set<Long>> uniqueVertices = new HashMap<Integer, Set<Long>>();
                int[] vertexIndices = new int[4];
                int[] texCoordIndices = new int[4];
                //int[] normalIndices = new int[4];
                int duplicateOffset = 0;
                
                for (int j = 0; j < 4; j++) { // all quads have 4 vertices, even triangle "looking" ones
                    index = format.getIntegerSize() * j; // real index
                    float x = Float.intBitsToFloat(vertexData[index]);
                    float y = Float.intBitsToFloat(vertexData[index + 1]);
                    float z = Float.intBitsToFloat(vertexData[index + 2]);
                    
                    int xI = Float.floatToRawIntBits(x);
                    long yzL = (((long)Float.floatToRawIntBits(y)) << 32) | (Float.floatToRawIntBits(z) & 0xffffffffL);
                    
                    // skip duplicate vertices and data in case of triangles
                    if (uniqueVertices.containsKey(xI)) {
                        if (uniqueVertices.get(xI).contains(yzL)) {
                            duplicateOffset--;
                            
                            if (duplicateOffset < -1) break; // skip face to ignore 2-point faces
                            
                            vertexIndices = Arrays.copyOf(vertexIndices, 4 + duplicateOffset);
                            texCoordIndices = Arrays.copyOf(texCoordIndices, 4 + duplicateOffset);
                            //normalIndices = Arrays.copyOf(normalIndices, 4 + duplicateOffset);
                            continue;
                        } else {
                            uniqueVertices.get(xI).add(yzL);
                        }
                    } else {
                        uniqueVertices.put(xI, new HashSet<Long>() {{add(yzL);}});
                    }
                    
                    float u = quad.getSprite().getUnInterpolatedU(Float.intBitsToFloat(vertexData[index + uvOffset])) / 16.0F % 1; // get U and V from data
                    float v = quad.getSprite().getUnInterpolatedV(Float.intBitsToFloat(vertexData[index + uvOffset + 1])) / 16.0F % 1;
                    Long uv = (((long)Float.floatToRawIntBits(u)) << 32) | (Float.floatToRawIntBits(v) & 0xffffffffL); // store UV as long for better performance
                    /* ignore normals for now TODO export normal maps
                    int normals = vertexData[index + normalOffset]; // data containing normals, first 3 bytes should be normal data
                    byte normalI = (byte)(normals & 255);
                    byte normalJ = (byte)((normals >> 8) & 255);
                    byte normalK = (byte)((normals >> 16) & 255);
                    */
                    if (!vertices.containsKey(xI)) {
                        vertices.put(xI, new HashMap<Long, Integer>());
                    }
                    
                    if (!vertices.get(xI).containsKey(yzL)) {
                        vertices.get(xI).put(yzL, verticesCount);
                        verticesCount++;
                        obj.addVertex(x, y, z);
                    }
                    
                    if (!textureCoords.containsKey(uv)) {
                        textureCoords.put(uv, textureCoords.size());
                        obj.addTexCoord(u, 1.0F - v); // flip v to preserve texture rotation
                    }
                    /*
                    if (!normalMap.containsKey(normals)) {
                        normalMap.put(normals, normalMap.size());
                        obj.addNormal(((int) normalI + 128) / 255.0F, ((int) normalJ + 128) / 255.0F, ((int) normalK + 128) / 255.0F);
                    }
                    */
                    vertexIndices[j + duplicateOffset] = vertices.get(xI).get(yzL);
                    texCoordIndices[j + duplicateOffset] = textureCoords.get(uv);
                    // normalIndices[j + duplicateOffset] = normalMap.get(normals);
                }
                
                
                String matName = processTextures(quad, cube, textures, mtls);
                
                if (duplicateOffset >= -1) { // only add face if it's a quad or a triangle
                    Long firstTwo = (((long)vertexIndices[0]) << 32) | (vertexIndices[1] & 0xffffffffL);
                    Long lastTwo = (((long)vertexIndices[2]) << 32) | ((vertexIndices.length == 3 ? -1 : vertexIndices[3]) & 0xffffffffL);
                    
                    if (!materialGroups.containsKey(matName)) {
                        materialGroups.put(matName, new HashMap<Long, Map<Long, List<int[]>>>());
                    }
                    
                    if (!materialGroups.get(matName).containsKey(firstTwo)) {
                        materialGroups.get(matName).put(firstTwo, new HashMap<Long, List<int[]>>());
                    }
                    
                    if (!materialGroups.get(matName).get(firstTwo).containsKey(lastTwo)) {
                        materialGroups.get(matName).get(firstTwo).put(lastTwo, new ArrayList<int[]>());
                    }
                    
                    int[] faceData = /*ArrayUtils.addAll(*/texCoordIndices/*, normalIndices)*/;
                    materialGroups.get(matName).get(firstTwo).get(lastTwo).add(faceData); // add entry
                    /* 
                    if (!uniqueFaces.containsKey(firstTwo)) {
                        uniqueFaces.put(firstTwo, new HashMap<Long, Tuple<int[], String>>());
                    }
                    
                    if (!uniqueFaces.get(firstTwo).containsKey(lastTwo)) {
                        Tuple<int[], String> otherData = new Tuple<int[], String>(ArrayUtils.addAll(texCoordIndices, normalIndices), matName);
                        uniqueFaces.get(firstTwo).put(lastTwo, otherData); // add data for face
                        lastI = i; // prevent voiding faces with multiple quads
                    } else if (uniqueFaces.get(firstTwo).get(lastTwo) != null) {
                        if (lastI != i) {
                            uniqueFaces.get(firstTwo).put(lastTwo, null); // set face data to null to ignore them later
                        } else {
                            // TODO: merge textures? add two faces on same location?
                        }
                    }*/
                }
            }
        }
        
        for (Entry<String, Map<Long, Map<Long, List<int[]>>>> material : materialGroups.entrySet()) {
            obj.setActiveMaterialGroupName(material.getKey());
            
            for (Entry<Long, Map<Long, List<int[]>>> firstMap : material.getValue().entrySet()) {
                long firstTwo = firstMap.getKey();

                for (Entry<Long, List<int[]>> secondMap : firstMap.getValue().entrySet()) {
                    long secondTwo = secondMap.getKey();
                    
                    for (int[] otherData : secondMap.getValue()) { //  this list contains only duplicate faces found in one cube TODO: handle duplicates of different and same textures (NOT MATERIALS!)
                        int[] vertexIndices = new int[] { (int)(firstTwo >> 32), (int)(firstTwo), (int)(secondTwo >> 32), (int)(secondTwo) };
                        
                        if (vertexIndices[3] == -1) vertexIndices = Arrays.copyOf(vertexIndices, 3); // this is a triangle
                        
                        int[] texCoordIndices = /*Arrays.copyOf(*/otherData/*, vertexIndices.length)*/;
                        //int[] normalIndices = new int[vertexIndices.length];
                        //System.arraycopy(otherData, vertexIndices.length, normalIndices, 0, vertexIndices.length);
                        ObjFace objFace = new LightObjFace(vertexIndices, texCoordIndices, /*normalIndices*/null); // TODO: check normals
                        obj.addFace(objFace);
                    }
                }
            }
        }
        
        /*for (Entry<Long, Map<Long, Tuple<int[], String>>> firstMap : uniqueFaces.entrySet()) {
            long firstTwo = firstMap.getKey();

            for (Entry<Long, Tuple<int[], String>> secondMap : firstMap.getValue().entrySet()) {
                if (secondMap.getValue() == null) continue; // ignore empty entries
                
                long secondTwo = secondMap.getKey();
                Tuple<int[], String> otherData = secondMap.getValue();
                int[] vertexIndices = new int[] { (int)(firstTwo >> 32), (int)(firstTwo), (int)(secondTwo >> 32), (int)(secondTwo) };
                
                if (vertexIndices[3] == -1) vertexIndices = Arrays.copyOf(vertexIndices, 3); // this is a triangle
                
                int[] texCoordIndices = Arrays.copyOf(otherData.getFirst(), vertexIndices.length);
                int[] normalIndices = new int[vertexIndices.length];
                System.arraycopy(otherData.getFirst(), vertexIndices.length, normalIndices, 0, vertexIndices.length);
                ObjFace objFace = new LightObjFace(vertexIndices, texCoordIndices, normalIndices); // TODO: check normals
                obj.setActiveMaterialGroupName(otherData.getSecond());
                obj.addFace(objFace);
            }
        }*/
    }

    private String processTextures(BakedQuad quad, RenderBox cube, Map<String, Map<Integer, Integer>> textures, List<Mtl> mtls) {
        TextureAtlasSprite sprite = quad.getSprite();
        String iconName = sprite.getIconName();
        String matName = iconName.substring(0, iconName.indexOf(':')) + "_" + iconName.substring(iconName.lastIndexOf('/') + 1); //base material name
        int baseBlockColor = Minecraft.getMinecraft().getBlockColors().colorMultiplier(cube.getBlockState(), null, null, quad.getTintIndex()) | 0xff000000; // set alpha to 255 regardless of given color
        int finalColor = multiplyColor(baseBlockColor, cube.color);
        boolean buildTexture = false;
        
        if (textures.containsKey(iconName)) { // texture is already defined, check color
            if (!textures.get(iconName).containsKey(finalColor)) { // color is new, create it
                textures.get(iconName).put(finalColor, textures.get(iconName).size());
                buildTexture = true;
            }
        } else {
            textures.put(iconName, new HashMap<Integer, Integer>() {{put(finalColor, 0);}});
            buildTexture = true;
        }
        
        matName = matName + textures.get(iconName).get(finalColor).toString(); // append index of this color as material name
        
        if (buildTexture) buildNewTexture(sprite, finalColor, matName, mtls, quad.hasTintIndex()); // must call the method AFTER appending material color index
        
        return matName;
    }
    
    private void buildNewTexture(TextureAtlasSprite sprite, int color, String matName, List<Mtl> mtls, boolean hasTintIndex) {
        int[][] textureData = sprite.getFrameTextureData(0); // only get the first frame TODO support for animated textures? (would require a script for blender probably)
        int[] rawFinalTextureData = new int[textureData[0].length];
        
        for (int k = 0; k < textureData[0].length; k++) { // only getting the first texture data TODO check what the index actually means (constructing more textures into one? normal map? roughness map?)
            rawFinalTextureData[k] = /*hasTintIndex ? */multiplyColor(textureData[0][k], color)/* : textureData[0][k]*/; // multiply color, currently recolors all textures, even those which should not be will be fixed with rework at line 110
        }
        
        BufferedImage image = new BufferedImage(sprite.getIconWidth(), sprite.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, sprite.getIconWidth(), sprite.getIconHeight(), rawFinalTextureData, 0, sprite.getIconWidth());
        String texturePath = "textures/" + matName.substring(0, matName.indexOf('_')) + "/" + matName.substring(matName.indexOf('_') + 1) + ".png";
        File textureFile = new File(texturePath);
        textureFile.mkdirs();

        try {
            ImageIO.write(image, "png", textureFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Mtl currentMaterial = Mtls.create(matName); // TODO: add support for more material attributes
        //currentMaterial.setNs(100.0F); // set by default
        currentMaterial.setKa(1.0F, 1.0F, 1.0F); // ambient color
        currentMaterial.setKd(1.0F, 1.0F, 1.0F); // actual color - not used, messes up rendering, which uses different blend technique than LT
        // currentMaterial.setKs(0.0F, 0.0F, 0.0F); // specular reflection - defaults to zero
        // currentMaterial.setKe(0.0F, 0.0F, 0.0F); not supported by Obj lib defines emissive color parameter
        // currentMaterial.setNi(1.0F); not supported by Obj lib defines optical density parameter
        currentMaterial.setD(1.0F); // transparency of the whole material
        currentMaterial.setMapKd(texturePath);
        mtls.add(currentMaterial);
    }
}
