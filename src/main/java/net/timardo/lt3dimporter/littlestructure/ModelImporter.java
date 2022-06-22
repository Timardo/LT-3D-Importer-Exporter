package net.timardo.lt3dimporter.littlestructure;

import javax.annotation.Nullable;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.item.ItemMultiTiles;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ModelImporter extends LittleStructurePremade {
    
    public String model;
    public String gridSize = ItemMultiTiles.currentContext.size + "";
    public String precision = "0.05";
    public String texFile;
    public boolean useMtl = false;
    public int color = -1;
    public boolean useTex = true;
    public String maxSize = this.gridSize;
    public ItemStack baseBlock = new ItemStack(LittleTiles.dyeableBlock);
    public Slot output = new Slot(new InventoryBasic("out", false, 1), 0, 197, 81);

    public ModelImporter(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }

    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) { 
        this.model = nbt.getString("lt_model");
        this.gridSize = nbt.hasKey("lt_grid") ? nbt.getString("lt_grid") : this.gridSize;
        this.precision = nbt.hasKey("lt_prec") ? nbt.getString("lt_prec") : this.precision;
        this.texFile = nbt.getString("lt_tex");
        this.useMtl = nbt.hasKey("lt_mtl") ? nbt.getBoolean("lt_mtl") : this.useMtl;
        this.color = nbt.hasKey("lt_col") ? nbt.getInteger("lt_col") : this.color;
        this.useTex = nbt.hasKey("lt_useTex") ? nbt.getBoolean("lt_useTex") : this.useTex;
        this.maxSize = nbt.hasKey("lt_size") ? nbt.getString("lt_size") : this.maxSize;
        this.output.inventory.setInventorySlotContents(0, new ItemStack(nbt.getCompoundTag("lt_slot")));
        this.baseBlock = nbt.hasKey("lt_base") ? new ItemStack(nbt.getCompoundTag("lt_base")) : this.baseBlock;
    }

    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setString("lt_model", this.model);
        nbt.setString("lt_grid", this.gridSize);
        nbt.setString("lt_prec", this.precision);
        nbt.setString("lt_tex", this.texFile);
        nbt.setInteger("lt_col", this.color);
        nbt.setBoolean("lt_useTex", this.useTex);
        nbt.setBoolean("lt_mtl", this.useMtl);
        nbt.setString("lt_size", this.maxSize);
        
        if (!this.output.inventory.getStackInSlot(0).isEmpty()) {
            nbt.setTag("lt_slot", this.output.inventory.getStackInSlot(0).serializeNBT());
        }
        
        nbt.setTag("lt_base", this.baseBlock.serializeNBT());
    }
    
    @Override
    public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, EntityPlayer player, EnumHand hand, @Nullable ItemStack itemInHand, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (!world.isRemote) {
            this.sendUpdatePacket(); // notifies clients about any changes so they will always have correct data // TODO update all viewers with every change
            LittleStructureGuiHandler.openGui("modelimporter", new NBTTagCompound(), player, this);
        }
        
        return true;
    }
}
