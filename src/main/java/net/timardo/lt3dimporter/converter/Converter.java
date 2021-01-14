package net.timardo.lt3dimporter.converter;

import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.io.FileNotFoundException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.timardo.lt3dimporter.LT3DImporter;
import net.timardo.lt3dimporter.littlestructure.ModelImporter;
import net.timardo.lt3dimporter.network.PacketStructureNBT;
import net.timardo.lt3dimporter.obj3d.LightObj;

public class Converter implements Runnable {
    
    private static final int BASE_COLOR = ColorUtils.RGBToInt(new Vec3i(255, 255, 255));
    private String model;
    private String tex;
    private Color col;
    private int size;
    private int grid;
    private float precision;
    private ItemStack base;
    private boolean isTex;
    private EntityPlayer player;
    private ModelImporter structure;
    
    public Converter(String modelFile, String texName, Color color, int maxSize, int grid, float minprecision, ItemStack baseBlock, boolean texture, /*ModelImporterGui parent,*/ EntityPlayer player, ModelImporter s) {
        this.model = modelFile;
        this.tex = texName;
        this.col = color;
        this.size = maxSize;
        this.grid = grid;
        this.precision = minprecision;
        this.base = baseBlock;
        this.isTex = texture;
        this.player = player;
        this.structure = s;
    }
    
    @Override
    public void run() {
        NBTTagCompound tag = null;
        
        if (isTex && tex.isEmpty()) {
            postMessage(TextFormatting.RED + "Empty texture field!");
            return;
        }
        
        try {
            tag = convertModelToRecipe();
        } catch (ImportException ie) {
            postMessage(TextFormatting.RED + ie.getReason());
            return;
        }

        PacketStructureNBT nbtPacket = new PacketStructureNBT();
        NBTTagCompound packetNBT = new NBTTagCompound();
        packetNBT.setBoolean("item", true);
        packetNBT.setTag("loc", new StructureLocation(this.structure).write());
        packetNBT.setTag("recipe_nbt", tag);
        nbtPacket.setNBT(packetNBT);
        PacketHandler.sendPacketToServer(nbtPacket);
    }

    private void postMessage(String msg) {
        this.player.sendMessage(new TextComponentString(msg));
    }

    /**
     * Basic algorithm to load, convert, parse and return an Advanced Recipe ItemStack from .obj model
     * 
     * @return a NBT tag for Advanced Recipe
     */
    private NBTTagCompound convertModelToRecipe() throws ImportException {
        InputStream objInputStream = null;
        
        try {
            objInputStream = new FileInputStream(this.model);
        } catch (FileNotFoundException | NullPointerException e) {
            LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
            throw new ImportException("Cannot read model file! Does it exist?");
        }
        
        LightObj obj = null;
        
        try {
            obj = (LightObj) ObjReader.read(objInputStream, new LightObj());
        } catch (IOException e) { 
            LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
            throw new ImportException("Unreadable model file! Is it in obj format?");
        }
        
        obj = ObjUtils.triangulate(obj, new LightObj(true));
        
        try {
            objInputStream.close();
        } catch (IOException | NullPointerException e) {
            LT3DImporter.logger.error("Error closing the stream! If you are getting this error too often, please, open an issue on GitHub (link on CurseForge). Include this info:" + System.lineSeparator() + ExceptionUtils.getStackTrace(e));
            postMessage(TextFormatting.RED + "Error closing the stream! Check log!");
        }
        
        Texture tex = new ColoredTexture(BASE_COLOR);
        
        if (this.isTex) {
            try {
                tex = new NormalTexture(this.tex);
            } catch (IOException e) {
                LT3DImporter.logger.error(ExceptionUtils.getStackTrace(e));
                throw new ImportException("Error loading texture file! Is it in supported format? (JPEG, PNG, BMP)");
            }
        } else {
            tex = new ColoredTexture(ColorUtils.RGBAToInt(this.col));
        }
        
        double[] s = obj.getSides();
        double ratio = this.size / Math.max(s[0], Math.max(s[1], s[2]));
        LittleGridContext context = LittleGridContext.get(grid);
        ConvertedModel outputModel = new ConvertedModel(tex, context, obj, ratio, this.base);

        for (Triangle t : obj.triangles) {
            t.calcBlocks(this.precision, ratio, outputModel);
        }
        
        /*if (tex instanceof ColoredTexture) // TODO check if this actually helps in terms of placing, RAM and rendering performance
            BasicCombiner.combinePreviews(tiles);*/

        LT3DImporter.logger.debug("prepare for some waiting..."); // TODO remove this timing thing (in release?)
        long time = System.currentTimeMillis();
        NBTTagCompound nbt = fastSavePreview(outputModel);
        LT3DImporter.logger.debug("done in " + (System.currentTimeMillis() - time) + "ms");
        return nbt;
    }
    
    /**
     *  <b>MUCH</b> faster equivalent of LittlePreview.savePreview which is tweaked a bit to fit the needs of this mod
     * 
     * @param model - the model
     */
    private NBTTagCompound fastSavePreview(ConvertedModel model) {
        NBTTagCompound ret = new NBTTagCompound();
        model.previews.getContext().set(ret);
        
        int minX = (int) Math.round(model.obj.boxCoords[3] * model.ratio);
        int minY = (int) Math.round(model.obj.boxCoords[4] * model.ratio);
        int minZ = (int) Math.round(model.obj.boxCoords[5] * model.ratio);
        int maxX = (int) Math.round(model.obj.boxCoords[0] * model.ratio);
        int maxY = (int) Math.round(model.obj.boxCoords[1] * model.ratio);
        int maxZ = (int) Math.round(model.obj.boxCoords[2] * model.ratio);
        
        new LittleVec(maxX - minX, maxY - minY, maxZ - minZ).writeToNBT("size", ret);
        new LittleVec(minX, minY, minZ).writeToNBT("min", ret); // TODO set base point to center of the object with min Y
        
        if (model.previews.totalSize() >= LittlePreview.lowResolutionMode) { // TODO do this elsewhere and save some more power
            NBTTagList list = new NBTTagList();
            HashSet<BlockPos> positions = new HashSet<>();
            
            for (int i = 0; i < model.previews.size(); i++) {
                BlockPos pos = model.previews.get(i).box.getMinVec().getBlockPos(model.previews.getContext());
                
                if (!positions.contains(pos)) {
                    positions.add(pos);
                    list.appendTag(new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ() }));
                }
            }
            
            ret.setTag("pos", list);
        } else
            ret.removeTag("pos");
        
        NBTTagList list = new NBTTagList();
        
        for (Integer color : model.colorMap.keySet()) {
            List<LittlePreview> pList = model.colorMap.get(color);
            ArrayList<LittlePreview> tileList = new ArrayList<LittlePreview>(pList);
            LittlePreview grouping = tileList.remove(0);
            NBTTagCompound groupNBT = null;
            
            for (Iterator<LittlePreview> iterator2 = tileList.iterator(); iterator2.hasNext();) {
                LittlePreview preview = (LittlePreview) iterator2.next();
                
                if (groupNBT == null)
                    groupNBT = grouping.startNBTGrouping();
                
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
        ret.setInteger("count", model.previews.size());
        
        return ret;
    }
}
