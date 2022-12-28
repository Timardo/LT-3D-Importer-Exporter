package net.timardo.lt3dimporter.exporter;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

import de.javagl.obj.Mtl;
import de.javagl.obj.MtlWriter;
import de.javagl.obj.Mtls;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjWriter;
import de.javagl.obj.Objs;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.timardo.lt3dimporter.LT3DImporter;
import net.timardo.lt3dimporter.Utils;
import net.timardo.lt3dimporter.obj3d.LightObjFace;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.exception.ExceptionUtils;

import static net.timardo.lt3dimporter.Utils.*;

public class Exporter implements Runnable {
    
    private static final List<EnumFacing> FACINGS = Arrays.asList(EnumFacing.VALUES);
    private EntityPlayer player;
    private ItemStack blueprint;
    private Obj obj = Objs.create();
    private List<Mtl> mtls = new ArrayList<Mtl>();
    private Map<Integer, Map<Long, Integer>> vertices = new HashMap<Integer, Map<Long, Integer>>(); // all vertices
    private Map<Long, Integer> textureCoords = new HashMap<Long, Integer>(); // all texture coordinates with their indices stored as Long from two float values
    /*private Map<Integer, Integer> normalMap = new HashMap<Integer, Integer>();*/ // all normals TODO normals are currently not being exported as they probably do not exist in vanilla minecraft
    private Map<String, Map<Integer, Integer>> textures = new HashMap<String, Map<Integer, Integer>>(); // all textures in format texturename->map of different colors mapped to their indices TODO: change to actual color codes (optional through config ?)
    private Map<Long, Map<Long, Map<String, Integer>>> uniqueFaces = new HashMap<Long, Map<Long, Map<String, Integer>>>(); // unique faces
    private Map<String, Map<Long, Map<Long, List<int[]>>>> materialGroups = new HashMap<String, Map<Long, Map<Long, List<int[]>>>>(); // map materials - [material name, first 2 vertices, second 2 vertices, additional vertex data (texture indices)
    private Map<String, Boolean> materialDuplicateFaceRemoveMap = new HashMap<String, Boolean>();
    private String outputFolder;
    private String outputFileName;

    public Exporter(EntityPlayer sender, ItemStack blueprint, String outputFolder, String outputFileName) { // TODO more options to come
        this.player = sender;
        this.blueprint = blueprint;
        this.outputFolder = outputFolder;
        this.outputFileName = outputFileName;
        
        if (this.outputFolder.startsWith("\"") || this.outputFolder.endsWith("\""))
            this.outputFolder = this.outputFolder.replace("\"", "");
    }
    
    @Override
    public void run() {
        this.exportModel(this.blueprint);

        try {
            String fullPath = this.outputFolder + (this.outputFolder.isEmpty() ? "" : File.separator) + this.outputFileName;
            File folder = new File(this.outputFolder);
            folder.mkdirs();
            OutputStream mtlOutputStream = new FileOutputStream(fullPath + ".mtl");
            MtlWriter.write(this.mtls, mtlOutputStream);
            OutputStream objOutputStream = new FileOutputStream(fullPath + ".obj");
            this.obj.setMtlFileNames(Arrays.asList(this.outputFileName + ".mtl"));
            ObjWriter.write(this.obj, objOutputStream);
            mtlOutputStream.close();
            objOutputStream.close();
        } catch (IOException e) {
            LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
        }
        
        postMessage(this.player, "Exported");
    }
    
    /**
     * Method to convert {@link ItemStack} data to a 3D model. Method has bunch of comments mainly for me becuase I know I would get lost easily in that mess
     * TODO REFACTOR THIS MESS TO A READABLE STATE
     * 
     * @param stack - stack containing an LT structure
     */
    public void exportModel(ItemStack stack) {
        int verticesCount = 0;
        List<? extends RenderBox> cubes = LittlePreview.getCubes(stack, false);
        
        for (int i = 0; i < cubes.size(); i++) {
            RenderBox cube = cubes.get(i);
            IBakedModel blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(cube.getBlockState());
            List<BakedQuad> quads = new ArrayList<BakedQuad>(); // all quads making up this tile
            FACINGS.forEach(facing -> quads.addAll(CreativeBakedModel.getBakedQuad(null, cube, null, cube.getOffset(), cube.getBlockState(), blockModel, null, facing, 0, false)));
            // check if this particular tile should use tint index or not, later used to color compound textures
            boolean useTintIndex = false;
            HashMap<Long, Map<Long, ArrayList<Tuple<int[], BakedQuad>>>> faces = new HashMap<Long, Map<Long, ArrayList<Tuple<int[], BakedQuad>>>>();
            
            for (BakedQuad quad : quads) {
                if (quad.hasTintIndex()) {
                    useTintIndex = true;
                    break;
                }
            }

            for (BakedQuad quad : quads) {
                // build data structure
                int[] vertexData = quad.getVertexData(); // data containing position, normals, UV and color - no purpose found yet for the last three
                VertexFormat format = quad.getFormat(); // get format of data, AFAIK should be DefaultVertexFormats.ITEM TODO check if this is always the case
                int index = 0;
                int uvOffset = format.getUvOffsetById(0) / 4; // divided by 4 because offset is in bytes and each integer in int[] has 4 bytes
                /*int normalOffset = format.getNormalOffset() / 4;*/
                Map<Integer, Set<Long>> uniqueVertices = new HashMap<Integer, Set<Long>>();
                int[] vertexIndices = new int[4];
                int[] texCoordIndices = new int[4];
                /*int[] normalIndices = new int[4];*/
                int duplicateOffset = 0;
                
                for (int j = 0; j < 4; j++) { // all quads have 4 vertices, even triangle "looking" ones TODO check if this is universally true for all blocks and custom models
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
                            
                            vertexIndices = Arrays.copyOf(vertexIndices, 4 + duplicateOffset); // shorten the array
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
                    if (!this.vertices.containsKey(xI)) {
                        this.vertices.put(xI, new HashMap<Long, Integer>());
                    }
                    
                    if (!this.vertices.get(xI).containsKey(yzL)) {
                        this.vertices.get(xI).put(yzL, verticesCount);
                        verticesCount++;
                        this.obj.addVertex(x, y, z);
                    }
                    
                    if (!this.textureCoords.containsKey(uv)) {
                        this.textureCoords.put(uv, this.textureCoords.size());
                        this.obj.addTexCoord(u, 1.0F - v); // flip v to preserve texture rotation
                    }
                    /*
                    if (!normalMap.containsKey(normals)) {
                        normalMap.put(normals, normalMap.size());
                        this.obj.addNormal(((int) normalI + 128) / 255.0F, ((int) normalJ + 128) / 255.0F, ((int) normalK + 128) / 255.0F);
                    }
                    */
                    vertexIndices[j + duplicateOffset] = this.vertices.get(xI).get(yzL);
                    texCoordIndices[j + duplicateOffset] = this.textureCoords.get(uv);
                    /*normalIndices[j + duplicateOffset] = normalMap.get(normals);*/
                }
                
                if (duplicateOffset >= -1) { // only add face if it's a quad or a triangle
                    Long firstTwo = (((long)vertexIndices[0]) << 32) | (vertexIndices[1] & 0xffffffffL);
                    Long lastTwo = (((long)vertexIndices[2]) << 32) | ((vertexIndices.length == 3 ? -1 : vertexIndices[3]) & 0xffffffffL);

                    if (!faces.containsKey(firstTwo)) {
                        faces.put(firstTwo, new HashMap<Long, ArrayList<Tuple<int[], BakedQuad>>>());
                    }
                    
                    if (!faces.get(firstTwo).containsKey(lastTwo)) {
                        faces.get(firstTwo).put(lastTwo, new ArrayList<Tuple<int[], BakedQuad>>());
                    }
                    
                    faces.get(firstTwo).get(lastTwo).add(new Tuple<int[], BakedQuad>(texCoordIndices, quad));
                }
            }
            
            for (Entry<Long, Map<Long, ArrayList<Tuple<int[], BakedQuad>>>> faceEntry : faces.entrySet()) {
                long firstTwo = faceEntry.getKey();
                
                for (Entry<Long, ArrayList<Tuple<int[], BakedQuad>>> secondMap : faceEntry.getValue().entrySet()) {
                    long lastTwo = secondMap.getKey();
                    ArrayList<Tuple<int[], BakedQuad>> duplicateFaces = secondMap.getValue();
                    ArrayList<BufferedImage> faceTextures = new ArrayList<BufferedImage>();
                    String matName = "";
                    
                    for (Tuple<int[], BakedQuad> singleFace : duplicateFaces) {
                        BakedQuad quad = singleFace.getSecond();
                        TextureAtlasSprite sprite = quad.getSprite();
                        String iconName = sprite.getIconName();
                        String materialName = iconName.substring(0, iconName.indexOf(':')) + "_" + iconName.substring(iconName.lastIndexOf('/') + 1); //base material name
                        int baseBlockColor = Minecraft.getMinecraft().getBlockColors().colorMultiplier(cube.getBlockState(), this.player.getEntityWorld(), new BlockPos(32, 64, 285), quad.getTintIndex()) | 0xff000000; // get color by biome (currently just defualt) and set alpha to 255 regardless of given color
                        int alpha = ColorUtils.getAlpha(cube.color);
                        int cubeColorOnly = (cube.color | 0xff000000);
                        int finalColor = multiplyColor((cubeColorOnly != -1 || (cubeColorOnly == -1 && alpha != 255)) && baseBlockColor != -1 ? multiplyColor(0xff999999, cube.color) : cube.color, cubeColorOnly != -1 && baseBlockColor != -1 ? -1 : baseBlockColor); // for some weird reason colored blocks with colormap ignore given color map and multiply the cube color with 0.6, but using color map instead of cube color if only alpha is set in cube color
                        int[][] textureData = sprite.getFrameTextureData(0); // only get the first frame ?? TODO support for animated textures? (would require a script for blender probably), also why is it 2D array?
                        int[] rawFinalTextureData = new int[textureData[0].length];
                        int alphaColor = alpha << 24 | 0x00ffffff; // only use alpha to multiply textures with no tint index on blocks which have quads with tint index
                        
                        for (int k = 0; k < textureData[0].length; k++) { // only getting the first texture data TODO check what the index actually means (constructing more textures into one? normal map? roughness map?)
                            rawFinalTextureData[k] = multiplyColor(textureData[0][k], (alpha != 255 && (cube.color | 0xff000000) != -1) || !useTintIndex || (useTintIndex && quad.hasTintIndex()) ? finalColor : alphaColor); // multiply color only if alpha has changed AND cube color is actually set (not only alpha) or the tile does not use tint index or it does use tint index and has tint index
                        }
                        
                        BufferedImage image = new BufferedImage(sprite.getIconWidth(), sprite.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                        image.setRGB(0, 0, sprite.getIconWidth(), sprite.getIconHeight(), rawFinalTextureData, 0, sprite.getIconWidth());
                        matName += materialName;
                        faceTextures.add(image);
                    }

                    boolean buildTexture = false;
                    
                    if (this.textures.containsKey(matName)) { // texture is already defined, check color
                        if (!this.textures.get(matName).containsKey(cube.color)) { // color is new, create it
                            this.textures.get(matName).put(cube.color, this.textures.get(matName).size());
                            buildTexture = true;
                        }
                    } else {
                        this.textures.put(matName, new HashMap<Integer, Integer>() {{put(cube.color, 0);}});
                        buildTexture = true;
                    }
                    
                    matName = matName + "_" + cube.color; // append the color in integer form
                    
                    if (buildTexture) {
                        BufferedImage finalTexture = new BufferedImage(faceTextures.get(0).getWidth(), faceTextures.get(0).getHeight(), BufferedImage.TYPE_INT_ARGB); // TODO currently getting just the first image assuming all images making up a face are of the same size
                        Graphics graphics = finalTexture.getGraphics();
                        faceTextures.forEach(image -> graphics.drawImage(image, 0, 0, null));
                        graphics.dispose();
                        
                        String texturePath = "textures/" + matName.substring(0, matName.indexOf('_')) + "/" + matName.substring(matName.indexOf('_') + 1) + ".png";
                        File textureFile = new File((this.outputFolder.isEmpty() ? "" : this.outputFolder + File.separator) + texturePath);
                        textureFile.mkdirs();

                        try {
                            ImageIO.write(finalTexture, "png", textureFile);
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
                        this.mtls.add(currentMaterial);
                    }
                    
                    int[] sortedVertexIndices = Utils.heapSort(new int[] { (int)(firstTwo >> 32), (int)(firstTwo), (int)(lastTwo >> 32), (int)(lastTwo) });
                    Long firstTwoSorted = (((long)sortedVertexIndices[0]) << 32) | (sortedVertexIndices[1] & 0xffffffffL);
                    Long lastTwoSorted = (((long)sortedVertexIndices[2]) << 32) | ((sortedVertexIndices.length == 3 ? -1 : sortedVertexIndices[3]) & 0xffffffffL);
                    
                    if (!this.uniqueFaces.containsKey(firstTwoSorted)) {
                        this.uniqueFaces.put(firstTwoSorted, new HashMap<Long, Map<String, Integer>>());
                    }
                    
                    if (!this.uniqueFaces.get(firstTwoSorted).containsKey(lastTwoSorted)) {
                        this.uniqueFaces.get(firstTwoSorted).put(lastTwoSorted, new HashMap<String, Integer>());
                    }
                    
                    if (!this.uniqueFaces.get(firstTwoSorted).get(lastTwoSorted).containsKey(matName)) {
                        this.uniqueFaces.get(firstTwoSorted).get(lastTwoSorted).put(matName, 1);
                    } else {
                        this.uniqueFaces.get(firstTwoSorted).get(lastTwoSorted).put(matName, this.uniqueFaces.get(firstTwoSorted).get(lastTwoSorted).get(matName) + 1);
                    }
                    
                    if (!this.materialGroups.containsKey(matName)) {
                        this.materialGroups.put(matName, new HashMap<Long, Map<Long, List<int[]>>>());
                        this.materialDuplicateFaceRemoveMap.put(matName, !(cube.block instanceof BlockLeaves)); // put true if duplicate faces of the same material (texture) should be removed, false otherwise (only leaves afaik)
                    }
                    
                    if (!this.materialGroups.get(matName).containsKey(firstTwo)) {
                        this.materialGroups.get(matName).put(firstTwo, new HashMap<Long, List<int[]>>());
                    }
                    
                    if (!this.materialGroups.get(matName).get(firstTwo).containsKey(lastTwo)) {
                        this.materialGroups.get(matName).get(firstTwo).put(lastTwo, new ArrayList<int[]>());
                    }
                    
                    this.materialGroups.get(matName).get(firstTwo).get(lastTwo).add(duplicateFaces.get(0).getFirst()); // add entry TODO currently getting just the first texture indices for this face, they can be different for some blocks
                }
            }
            
        }
        
        for (Entry<String, Map<Long, Map<Long, List<int[]>>>> material : this.materialGroups.entrySet()) {
            int leavesIndex = 0;
            this.obj.setActiveMaterialGroupName(material.getKey());
            this.obj.setActiveGroupNames(Collections.singletonList(material.getKey()));
            
            for (Entry<Long, Map<Long, List<int[]>>> firstMap : material.getValue().entrySet()) {
                long firstTwo = firstMap.getKey();

                for (Entry<Long, List<int[]>> secondMap : firstMap.getValue().entrySet()) {
                    long lastTwo = secondMap.getKey();
                    int[] vertexIndices = new int[] { (int)(firstTwo >> 32), (int)(firstTwo), (int)(lastTwo >> 32), (int)(lastTwo) };
                    int[] sortedVertexIndices = Utils.heapSort(vertexIndices);
                    Long firstTwoSorted = (((long)sortedVertexIndices[0]) << 32) | (sortedVertexIndices[1] & 0xffffffffL);
                    Long lastTwoSorted = (((long)sortedVertexIndices[2]) << 32) | ((sortedVertexIndices.length == 3 ? -1 : sortedVertexIndices[3]) & 0xffffffffL); // TODO put -1 at the start for smaller data sizes?
                    
                    if (this.materialDuplicateFaceRemoveMap.get(material.getKey()) && this.uniqueFaces.get(firstTwoSorted).get(lastTwoSorted).get(material.getKey()) > 1) {
                        continue; // multiple identical faces - ignore them all - saves a bit of data and makes transparent textures (like glass) look natural
                    }
                    
                    if (!this.materialDuplicateFaceRemoveMap.get(material.getKey()) && this.uniqueFaces.get(firstTwoSorted).get(lastTwoSorted).get(material.getKey()) > 1) {
                        this.obj.setActiveGroupNames(Collections.singletonList(material.getKey() + leavesIndex++)); // put every single leaves tile to a separate group to prevent blender from deleting duplicate faces TODO optional? (afaik only blender does this)
                    }
                    
                    if (vertexIndices[3] == -1) vertexIndices = Arrays.copyOf(vertexIndices, 3); // this is a triangle
                    
                    for (int[] otherData : secondMap.getValue()) { // TODO check if this still actually needs to be a list (there should not be exact same duplicate faces anymore)
                        int[] texCoordIndices = /*Arrays.copyOf(*/otherData/*, vertexIndices.length)*/;
                        /*int[] normalIndices = new int[vertexIndices.length];
                        /System.arraycopy(otherData, vertexIndices.length, normalIndices, 0, vertexIndices.length);*/
                        ObjFace objFace = new LightObjFace(vertexIndices, texCoordIndices, /*normalIndices*/null); // TODO: check normals
                        this.obj.addFace(objFace);
                    }
                }
            }
        }
    }
}
