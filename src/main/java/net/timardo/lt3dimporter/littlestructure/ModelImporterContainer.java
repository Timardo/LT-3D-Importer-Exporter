package net.timardo.lt3dimporter.littlestructure;

import com.creativemd.creativecore.common.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class ModelImporterContainer extends SubContainer {

    public ModelImporter parentStructure;
    
    public ModelImporterContainer(EntityPlayer player, ModelImporter structure) {
        super(player);
        this.parentStructure = structure;
    }

    @Override
    public void createControls() {
        this.addSlotToContainer(this.parentStructure.output);
        this.addPlayerSlotsToContainer(player, 29, 105);
    }

    @Override
    public void onPacketReceive(NBTTagCompound var1) {} // use packet system for better multiplayer experience
}
