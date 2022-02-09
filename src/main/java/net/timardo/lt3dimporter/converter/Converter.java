package net.timardo.lt3dimporter.converter;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.FileNotFoundException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.timardo.lt3dimporter.LT3DImporter;
import net.timardo.lt3dimporter.littlestructure.ModelImporter;
import net.timardo.lt3dimporter.network.PacketStructureNBT;
import net.timardo.lt3dimporter.obj3d.LightObj;
import net.timardo.lt3dimporter.obj3d.LightObjGroup;

import static net.timardo.lt3dimporter.Utils.*;

public class Converter implements Runnable {
    private String model;
    private String tex;
    private Color col;
    private int size;
    private int grid;
    private float precision;
    private ItemStack base;
    private boolean isTex;
    private boolean useMtl;
    private EntityPlayer player;
    private ModelImporter structure;
    
    public Converter(String modelFile, String texName, Color color, int maxSize, int grid, float minprecision, ItemStack baseBlock, boolean texture, boolean useMtl, EntityPlayer player, ModelImporter s) {
        this.model = modelFile;
        this.tex = texName;
        this.col = color;
        this.size = maxSize;
        this.grid = grid;
        this.precision = minprecision;
        this.base = baseBlock;
        this.isTex = texture;
        this.useMtl = useMtl;
        this.player = player;
        this.structure = s;
    }
    
    @Override
    public void run() {
        NBTTagCompound tag = null;
        
        if (this.isTex && tex.isEmpty() && !this.useMtl) {
            postMessage(this.player, TextFormatting.RED + "Empty texture field!");
            return;
        }
        
        LT3DImporter.logger.debug("prepare for some waiting..."); // TODO remove this timing thing (in release?)
        long time = System.currentTimeMillis();
        
        try {
            tag = convertModelToRecipe();
        } catch (ImportException ie) {
            postMessage(this.player, TextFormatting.RED + ie.getReason());
            return;
        }
        
        LT3DImporter.logger.debug("done in " + (System.currentTimeMillis() - time) + "ms");
        PacketStructureNBT nbtPacket = new PacketStructureNBT();
        NBTTagCompound packetNBT = new NBTTagCompound();
        packetNBT.setBoolean("item", true);
        packetNBT.setTag("loc", new StructureLocation(this.structure).write());
        packetNBT.setTag("recipe_nbt", tag);
        nbtPacket.setNBT(packetNBT);
        PacketHandler.sendPacketToServer(nbtPacket); // TODO: filter out large packets before they brick a player or a chunk
    }

    /**
     * Basic algorithm to load, convert, parse and return an NBT containg the structure from an .obj model
     * 
     * @return an NBT tag for Blueprint
     */
    private NBTTagCompound convertModelToRecipe() throws ImportException {
        InputStream objInputStream = null;
        LightObj obj = null;
        
        if (this.model.startsWith("\"") || this.model.endsWith("\""))
            this.model = this.model.replace("\"", "");
        
        try {
            objInputStream = new FileInputStream(this.model);
        } catch (FileNotFoundException | NullPointerException e) {
            LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
            throw new ImportException("Cannot read model file! Does it exist?");
        }
        
        try {
            obj = (LightObj) ObjReader.read(objInputStream, new LightObj());
        } catch (IOException e) {
            LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
            throw new ImportException("Unreadable model file! Is it in .obj format?");
        }

        try {
            objInputStream.close();
        } catch (IOException | NullPointerException e) {
            LT3DImporter.logger.error("Error closing the objInputStream! If you are getting this error too often, please, open an issue on GitHub (link on CurseForge). Include this info:" + System.lineSeparator() + ExceptionUtils.getStackTrace(e));
            postMessage(this.player, TextFormatting.RED + "Error closing the objInputStream! Check log!");
        }
        
        obj = ObjUtils.triangulate(obj, new LightObj(true));
        Map<String, Texture> texMap = new HashMap<String, Texture>(); // mapping material to texture
        // texture part
        if (this.isTex) {
            if (this.useMtl) {
                List<Mtl> mtls = new ArrayList<Mtl>(); // in most cases the MTL file should be only one
                String rootDir = FilenameUtils.getFullPath(new File(this.model).getAbsolutePath());
                
                for (String fileName : obj.getMtlFileNames())
                {
                    InputStream stream = null;
                    
                    try {
                        stream = new FileInputStream(rootDir + fileName);
                    } catch (FileNotFoundException e) {
                        LT3DImporter.logger.error("Error loading texture, file '" + rootDir + fileName + "' doesn't exist! Full stacktrace below:");
                        LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
                        throw new ImportException("One or more MTL files declared in the obj file do not exist!");
                    }
                    
                    List<Mtl> mtls0 = null;
                    
                    try {
                        mtls0 = MtlReader.read(stream);
                    } catch (IOException e) {
                        LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
                        throw new ImportException("There was a problem reading an MTL file, check log!");
                    }
                    
                    try {
                        stream.close();
                    } catch (IOException | NullPointerException e) {
                        LT3DImporter.logger.error("Error closing the stream! If you are getting this error too often, please, open an issue on GitHub (link on CurseForge). Include this info:" + System.lineSeparator() + ExceptionUtils.getStackTrace(e));
                        postMessage(this.player, TextFormatting.RED + "Error closing the stream! Check log!");
                    }
                    
                    mtls.addAll(mtls0);
                }
                
                if (mtls.isEmpty()) {
                    throw new ImportException("No MTL entry declared in this .obj file!");
                }
                
                Map<String, Mtl> mtlMap = new HashMap<String, Mtl>();
                
                for (Mtl mtl : mtls) {
                    if (mtlMap.containsKey(mtl.getName())) {
                        throw new ImportException("Duplicate material found in declared MTL files! (" + mtl.getName() + ")");
                    }
                    
                    mtlMap.put(mtl.getName(), mtl);
                }
                
                for (int i = 0; i < obj.getNumMaterialGroups(); i++) {
                    String material = ((LightObjGroup) obj.getMaterialGroup(i)).getName();
                    
                    try {
                        texMap.put(material, new MtlTexture(mtlMap.get(material), rootDir));
                    } catch (IOException e) {
                        LT3DImporter.logger.error("Error loading texture, file '" + mtlMap.get(material).getMapKd() + "' is in unsupported format! Full stacktrace below:");
                        LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
                        throw new ImportException("Error loading one or more texture files declared in the MTL file! Is it in supported format? (JPEG, PNG, BMP)");
                    }
                }
            } else {
                try {
                    Texture tex = new NormalTexture(this.tex);
                
                    for (int i = 0; i < obj.getNumMaterialGroups(); i++) {
                        texMap.put(((LightObjGroup) obj.getMaterialGroup(i)).getName(), tex); // all materials have the same texture
                    }
                } catch (IOException e) {
                    LT3DImporter.logger.error("Error loading texture, file '" + this.tex + "' is in unsupported format! Full stacktrace below:");
                    LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
                    throw new ImportException("Error loading texture file! Is it in supported format? (JPEG, PNG, BMP)");
                }
            }
        } else {
            Texture tex = new ColoredTexture(ColorUtils.RGBAToInt(this.col));
            
            for (int i = 0; i < obj.getNumMaterialGroups(); i++) { // one condition + several loops for better efficiency
                texMap.put(((LightObjGroup) obj.getMaterialGroup(i)).getName(), tex); // all materials have the same color
            }
        }
        // end of texture part
        double[] s = obj.getSides();
        double ratio = this.size / Math.max(s[0], Math.max(s[1], s[2]));
        LittleGridContext context = LittleGridContext.get(grid);
        ConvertedModel outputModel = new ConvertedModel(texMap, context, obj, ratio, this.base);
        
        for (int i = 0; i < obj.getNumMaterialGroups(); i++) {
            LightObjGroup group = (LightObjGroup) obj.getMaterialGroup(i);
            
            for (Triangle t : group.triangles)
                t.calcBlocks(this.precision, ratio, outputModel, group.getName());
        }
        
        /*if (tex instanceof ColoredTexture) // TODO check if this actually helps in terms of placing, RAM and rendering performance
            BasicCombiner.combinePreviews(tiles);*/

        NBTTagCompound nbt = fastSavePreview(outputModel);
        return nbt;
    }
    
    /**
     *  <b>MUCH</b> faster equivalent of {@link LittlePreview#savePreview(LittlePreviews, ItemStack)} which is tweaked a bit to fit the needs of this mod
     * 
     * @param model - the model
     */
    private NBTTagCompound fastSavePreview(ConvertedModel model) {
        NBTTagCompound ret = new NBTTagCompound();
        model.context.set(ret);
        
        int minX = (int) Math.round(model.obj.boxCoords[3] * model.ratio);
        int minY = (int) Math.round(model.obj.boxCoords[4] * model.ratio);
        int minZ = (int) Math.round(model.obj.boxCoords[5] * model.ratio);
        int maxX = (int) Math.round(model.obj.boxCoords[0] * model.ratio);
        int maxY = (int) Math.round(model.obj.boxCoords[1] * model.ratio);
        int maxZ = (int) Math.round(model.obj.boxCoords[2] * model.ratio);
        
        new LittleVec(maxX - minX, maxY - minY, maxZ - minZ).writeToNBT("size", ret);
        new LittleVec(minX, minY, minZ).writeToNBT("min", ret); // TODO set base point to center of the object with min Y
        
        if (model.blocks.size() >= LittlePreview.lowResolutionMode) {
            ret.setTag("pos", model.previews);
        } else {
            ret.removeTag("pos");
        }
        
        NBTTagList list = new NBTTagList();
        
        for (Integer color : model.colorMap.keySet()) {
            List<LittlePreview> pList = model.colorMap.get(color);
            ArrayList<LittlePreview> tileList = new ArrayList<LittlePreview>(pList);
            LittlePreview grouping = tileList.remove(0);
            NBTTagCompound groupNBT = null;
            
            for (Iterator<LittlePreview> iterator2 = tileList.iterator(); iterator2.hasNext();) {
                LittlePreview preview = (LittlePreview) iterator2.next();
                
                if (groupNBT == null) {
                    groupNBT = grouping.startNBTGrouping();
                }
                
                grouping.groupNBTTile(groupNBT, preview);
                iterator2.remove();
            }

            if (groupNBT == null) {
                NBTTagCompound nbt = new NBTTagCompound();
                grouping.writeToNBT(nbt);
                list.appendTag(nbt);
            } else {
                list.appendTag(groupNBT);
            }
        }
        
        ret.setTag("tiles", list);
        ret.setInteger("count", model.blocks.size());
        
        return ret;
    }
}
