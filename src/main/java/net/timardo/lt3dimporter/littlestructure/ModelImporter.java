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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ModelImporter extends LittleStructurePremade {

    public ModelImporter(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }

    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) { }

    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) { }
    
    @Override
    public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, EntityPlayer player, EnumHand hand, @Nullable ItemStack itemInHand, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (!world.isRemote)
            LittleStructureGuiHandler.openGui("modelimporter", new NBTTagCompound(), player, this);
        
        return true;
    }
}
