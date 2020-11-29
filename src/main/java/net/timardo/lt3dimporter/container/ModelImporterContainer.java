package net.timardo.lt3dimporter.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

public class ModelImporterContainer extends SubContainer {

    public InventoryBasic output = new InventoryBasic("out", false, 1);
    
    public ModelImporterContainer(EntityPlayer player) {
        super(player);
    }

    @Override
    public void createControls() {
        this.addSlotToContainer(new Slot(output, 0, 123, 80));
        
    }

    @Override
    public void onPacketReceive(NBTTagCompound var1) {
        
    }
    
}
