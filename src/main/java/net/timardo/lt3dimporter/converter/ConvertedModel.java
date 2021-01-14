package net.timardo.lt3dimporter.converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.timardo.lt3dimporter.obj3d.LightObj;

public class ConvertedModel { // TODO parse mtl files for multiple textures in one .obj file
    
    public LinkedHashMap<Integer, List<LittlePreview>> colorMap;
    public Set<Long> blocks;
    public Texture texture;
    public LittlePreviews previews; // TODO get rid of this field
    public LightObj obj;
    public double ratio;
    public Block baseBlock;
    public int meta;
    
    public ConvertedModel(Texture tex, LittleGridContext context, LightObj obj, double ratio, ItemStack baseBlock) {
        this.colorMap = new LinkedHashMap<Integer, List<LittlePreview>>();
        this.blocks = new LinkedHashSet<Long>();
        this.texture = tex;
        this.previews = new LittlePreviews(context);
        this.obj = obj;
        this.ratio = ratio;
        this.baseBlock = Block.getBlockFromItem(baseBlock.getItem());
        this.meta = baseBlock.getMetadata();
    }

    public void addTile(BlockPos blockPos, double[] uv) {
        long longPos = blockPos.toLong();

        if (!this.blocks.add(longPos)) return; // block is already registered (maybe with different color but whatever)
        
        int color = this.texture.colorTile(uv);
        List<LittlePreview> pList = this.colorMap.get(color);
        LittleTileColored tile = new LittleTileColored(this.baseBlock, this.meta, color);
        tile.setBox(new LittleBox(new LittleVec(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        
        if (pList == null) {
            pList = new ArrayList<LittlePreview>();
            pList.add(tile.getPreviewTile());
            this.colorMap.put(color, pList);
        } else {
            pList.add(tile.getPreviewTile());
        }

        this.previews.addWithoutCheckingPreview(tile.getPreviewTile());
    }
}