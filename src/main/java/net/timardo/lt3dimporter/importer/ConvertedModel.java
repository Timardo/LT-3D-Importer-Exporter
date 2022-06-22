package net.timardo.lt3dimporter.importer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.timardo.lt3dimporter.obj3d.LightObj;

public class ConvertedModel {
    public LinkedHashMap<Integer, List<LittlePreview>> colorMap;
    public Set<Long> blocks;
    public Map<String, ITexture> textureMap;
    public LittleGridContext context;
    public LightObj obj;
    public double ratio;
    public Block baseBlock;
    public int meta;
    public NBTTagList previews;
    public Set<BlockPos> posSet;
    
    public ConvertedModel(Map<String, ITexture> texMap, LittleGridContext context, LightObj obj, double ratio, ItemStack baseBlock) {
        this.colorMap = new LinkedHashMap<Integer, List<LittlePreview>>();
        this.blocks = new LinkedHashSet<Long>();
        this.textureMap = texMap;
        this.context = context;
        this.obj = obj;
        this.ratio = ratio;
        this.baseBlock = Block.getBlockFromItem(baseBlock.getItem());
        this.meta = baseBlock.getMetadata();
        this.previews = new NBTTagList();
        this.posSet = new HashSet<>();
    }

    public void addTile(BlockPos blockPos, double[] uv, String material) {
        long longPos = blockPos.toLong();

        if (!this.blocks.add(longPos)) return; // block is already registered (maybe with different color but whatever)
        
        int color = this.textureMap.get(material).colorTile(uv);
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

        BlockPos pos = tile.getPreviewTile().box.getMinVec().getBlockPos(this.context);
        
        if (!this.posSet.contains(pos)) {
            this.posSet.add(pos);
            this.previews.appendTag(new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ() }));
        }
    }
}
