package net.timardo.lt3dimporter.converter;

import java.io.*;
import java.util.*;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.timardo.lt3dimporter.obj3d.LightObj;

public class ConvertUtil implements Runnable {
    
    private static final int BASE_COLOR = ColorUtils.RGBToInt(new Vec3i(255, 255, 255));
    
    /**
     * Basic algorithm to load, convert, parse and return an Advanced Recipe ItemStack from .obj model
     * 
     * @param modelFile - path to the model, with extension
     * @param texName - path to the texture, with extension
     * @param color - string array of 3 integers referencing a color in RGB format 
     * @param maxSize - maximum amount of little tiles in any given dimension
     * @param grid - grid size to use for the output
     * @return an Advanced Recipe ItemStack to play with
     */
    public static ItemStack convertModelToRecipe(String modelFile, String texName, String[] color, String maxSize, String grid, String minprecision) { // TODO new thread
        InputStream objInputStream = null;
        
        try {
            objInputStream = new FileInputStream(modelFile);
        } catch (FileNotFoundException | NullPointerException e) {
            return null;
        }
        
        LightObj obj = null;
        
        try {
            obj = (LightObj) ObjReader.read(objInputStream, new LightObj());
        } catch (IOException e) { 
            System.out.print(e.getStackTrace());
            return null;
        }
        
        obj = ObjUtils.triangulate(obj, new LightObj(true));
        
        try {
            objInputStream.close();
        } catch (IOException | NullPointerException e) {
            return null;
        }
        
        Texture tex = new ColoredTexture(BASE_COLOR);
        
        if (color != null)
            tex = new ColoredTexture(ColorUtils.RGBToInt(new Vec3i(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]))));
        if (texName != null) {
            try {
                tex = new NormalTexture(texName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        double[] s = obj.getSides();
        double ratio = Double.parseDouble(maxSize) / Math.max(s[0], Math.max(s[1], s[2]));
        LittleGridContext context = LittleGridContext.get(Integer.parseInt(grid));
        ConvertedModel outputModel = new ConvertedModel(tex, context, obj, ratio);

        for (Triangle t : obj.triangles) {
            t.calcBlocks(Double.parseDouble(minprecision), ratio, outputModel);
        }
        
        /*if (tex instanceof ColoredTexture) // TODO check if this actually helps in terms of placing, RAM and rendering performance
            BasicCombiner.combinePreviews(tiles);*/

        ItemStack stack = new ItemStack(LittleTiles.recipeAdvanced);
        System.out.println("prepare for some waiting...");
        long time = System.currentTimeMillis();
        fastSavePreview(outputModel, stack);
        System.out.println("done in " + (System.currentTimeMillis() - time) + "ms");
        return stack;
    }
    
    /**
     *  <b>MUCH</b> faster equivalent of LittlePreview.savePreview which is tweaked a bit to fit the needs of this mod
     * 
     * @param model the model
     * @param stack itemstack that gets filled by NBT generated from the model
     */
    public static void fastSavePreview(ConvertedModel model, ItemStack stack) {
        stack.setTagCompound(new NBTTagCompound());
        model.previews.getContext().set(stack.getTagCompound());
        
        int minX = (int) Math.round(model.obj.boxCoords[3] * model.ratio);
        int minY = (int) Math.round(model.obj.boxCoords[4] * model.ratio);
        int minZ = (int) Math.round(model.obj.boxCoords[5] * model.ratio);
        int maxX = (int) Math.round(model.obj.boxCoords[0] * model.ratio);
        int maxY = (int) Math.round(model.obj.boxCoords[1] * model.ratio);
        int maxZ = (int) Math.round(model.obj.boxCoords[2] * model.ratio);
        
        new LittleVec(maxX - minX, maxY - minY, maxZ - minZ).writeToNBT("size", stack.getTagCompound());
        new LittleVec(minX, minY, minZ).writeToNBT("min", stack.getTagCompound());
        
        if (model.previews.totalSize() >= LittlePreview.lowResolutionMode) {
            NBTTagList list = new NBTTagList();
            HashSet<BlockPos> positions = new HashSet<>();
            
            for (int i = 0; i < model.previews.size(); i++) {
                BlockPos pos = model.previews.get(i).box.getMinVec().getBlockPos(model.previews.getContext());
                
                if (!positions.contains(pos)) {
                    positions.add(pos);
                    list.appendTag(new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ() }));
                }
            }
            
            stack.getTagCompound().setTag("pos", list);
        } else
            stack.getTagCompound().removeTag("pos");
        
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
        
        stack.getTagCompound().setTag("tiles", list);
        stack.getTagCompound().setInteger("count", model.previews.size());
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
}
