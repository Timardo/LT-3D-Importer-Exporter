package net.timardo.lt3dimporter.littlestructure;

import javax.annotation.Nullable;

import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
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

public class ModelExporter extends LittleStructurePremade {
    
    public String outputFolder;
    public String outputFile;
    public boolean createGroups;
    public boolean additionalFacePostProcessing; // TODO: additional removal of faces of different materials/blocks based on transparency
    public boolean exportTextureSprite; // TODO: add option to export models with all textures in one file and one material
    public boolean exportColorAsMaterialParam; // TODO: add option to export colored textures using MTL colors (Blender is able to use multiply instead of basic blend color mixing technique)
    public Slot input = new Slot(new InventoryBasic("in", false, 1), 0, 160, 32);

    public ModelExporter(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }

    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        this.outputFolder = nbt.getString("lt_folder");
        this.outputFile = nbt.getString("lt_file");
        this.createGroups = nbt.getBoolean("lt_groups");
        this.additionalFacePostProcessing = nbt.getBoolean("lt_faces");
        this.exportTextureSprite = nbt.getBoolean("lt_sprite");
        this.exportColorAsMaterialParam = nbt.getBoolean("lt_mtlcolor");
        this.input.inventory.setInventorySlotContents(0, new ItemStack(nbt.getCompoundTag("lt_slot")));
    }

    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setString("lt_folder", this.outputFolder);
        nbt.setString("lt_file", this.outputFile);
        nbt.setBoolean("lt_groups", this.createGroups);
        nbt.setBoolean("lt_faces", this.additionalFacePostProcessing);
        nbt.setBoolean("lt_sprite", this.exportTextureSprite);
        nbt.setBoolean("lt_mtlcolor", this.exportColorAsMaterialParam);
        
        if (!this.input.inventory.getStackInSlot(0).isEmpty()) {
            nbt.setTag("lt_slot", this.input.inventory.getStackInSlot(0).serializeNBT());
        }
    }
    
    @Override
    public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, EntityPlayer player, EnumHand hand, @Nullable ItemStack itemInHand, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (!world.isRemote) {
            this.sendUpdatePacket(); // notifies clients about any changes so they will always have correct data // TODO update all viewers with every change
            LittleStructureGuiHandler.openGui("modelexporter", new NBTTagCompound(), player, this);
        }
        
        return true;
    }
}
