package net.timardo.lt3dimporter.converter;

import java.io.*;
import java.util.*;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.block.BlockLTColored;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.combine.BasicCombiner;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.timardo.lt3dimporter.obj3d.LightObj;

public class ConvertUtil {
    
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
    public static ItemStack convertToRecipe(String modelFile, String texName, String[] color, String maxSize, String grid) {
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
        
        Map<Long, Double[]> blocks = new LinkedHashMap<Long, Double[]>();
        double[] ss = obj.getSides();
        double ratio = Double.parseDouble(maxSize) / Math.max(ss[0], Math.max(ss[1], ss[2]));

        for (Triangle t : obj.triangles) {
            blocks.putAll(t.calcBlocks(0.7, ratio)); // TODO make minPrecision configurable, 0.7 will do for most purposes
        }

        LittleGridContext context = LittleGridContext.get(Integer.parseInt(grid));
        Texture t = new ColoredTexture(BASE_COLOR);
        
        if (color != null)
            t = new ColoredTexture(ColorUtils.RGBToInt(new Vec3i(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]))));
        if (texName != null) {
            try {
                t = new NormalTexture(texName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        List<LittlePreview> tiles = new ArrayList<LittlePreview>();

        for (Long l : blocks.keySet()) {
            BlockPos pos = BlockPos.fromLong(l);
            double u = blocks.get(l) == null ? 0 : blocks.get(l)[0];
            double v = blocks.get(l) == null ? 0 : blocks.get(l)[1];
            LittleTileColored tile = new LittleTileColored(LittleTiles.coloredBlock, BlockLTColored.EnumType.clean.ordinal(), t.colorTile(u, v));
            tile.setBox(new LittleBox(new LittleVec(pos.getX(), pos.getY(), pos.getZ())));
            tiles.add(tile.getPreviewTile());
            
            
        }

        if (t instanceof ColoredTexture) // only combine models without texture for better performance
            BasicCombiner.combinePreviews(tiles);

        ItemStack stack = new ItemStack(LittleTiles.recipeAdvanced);
        LittlePreviews previews = new LittlePreviews(context);

        for (LittlePreview tile : tiles) {
            previews.addWithoutCheckingPreview(tile);
        }
        
        System.out.println("prepare for some waiting...");
        long time = System.currentTimeMillis();
        LittlePreview.savePreview(previews, stack); // this one is a handful Tenno
        System.out.println("aaand done, in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");
        return stack;
    }
}
