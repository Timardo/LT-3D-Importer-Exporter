package net.timardo.lt3dimporter.littlestructure;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.item.ItemRecipeAdvanced;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModelImporterContainer extends SubContainer {

    public InventoryBasic output = new InventoryBasic("out", false, 1);
    
    public ModelImporterContainer(EntityPlayer player) {
        super(player);
    }

    @Override
    public void createControls() {
        this.addSlotToContainer(new Slot(output, 0, 197, 81));
        this.addPlayerSlotsToContainer(player, 29, 105);
        
    }

    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        ItemStack slot = output.getStackInSlot(0);
        if (!(slot.getItem() instanceof ItemRecipeAdvanced || getPlayer().isCreative())) return;
        ItemStack recipe = new ItemStack(LittleTiles.recipeAdvanced);
        recipe.setTagCompound(nbt);
        output.setInventorySlotContents(0, recipe);
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
        WorldUtils.dropItem(getPlayer(), output.getStackInSlot(0)); // TODO make it save the data
    }
}
