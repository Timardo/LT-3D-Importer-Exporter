package net.timardo.lt3dimporter.littlestructure;

import com.creativemd.creativecore.common.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class ModelExporterContainer extends SubContainer {
    public ModelExporter parentStructure;
    
    public ModelExporterContainer(EntityPlayer player, ModelExporter structure) {
        super(player);
        this.parentStructure = structure;
    }

    @Override
    public void createControls() {
        this.addSlotToContainer(this.parentStructure.input);
        this.addPlayerSlotsToContainer(player, 16, 56);
    }

    @Override
    public void onPacketReceive(NBTTagCompound var1) {} // use packet system for better multiplayer experience
}
