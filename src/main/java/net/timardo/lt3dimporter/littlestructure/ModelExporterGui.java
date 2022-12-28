package net.timardo.lt3dimporter.littlestructure;

import static net.timardo.lt3dimporter.Utils.postMessage;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.timardo.lt3dimporter.exporter.Exporter;
import net.timardo.lt3dimporter.network.PacketStructureExporterNBT;

public class ModelExporterGui extends SubGui {
    
    public GuiTextfield outputFolder;
    public GuiTextfield outputFile;
    // TODO: implement these options
    //public GuiCheckBox createGroups;
    //public GuiCheckBox additionalFacePostProcessing;
    //public GuiCheckBox exportTextureSprite;
    //public GuiCheckBox exportColorAsMaterialParam;
    public ModelExporter parentStructure;
    
    public ModelExporterGui(ModelExporter s) {
        super(197, 136);
        this.parentStructure = s;
    }

    @Override
    public void createControls() {
        this.outputFolder = new GuiTextfield("exportfolder", "", 5, 5, 128, 14);
        this.outputFolder.maxLength = 32768;
        this.outputFolder.setCustomTooltip("Path to folder for exporting");
        this.outputFolder.text = this.parentStructure.outputFolder;
        addControl(this.outputFolder);
        
        this.outputFile = new GuiTextfield("exportfile", "", 5, 31, 128, 14);
        this.outputFile.maxLength = 32768;
        this.outputFile.setCustomTooltip("File name for exporting");
        this.outputFile.text = this.parentStructure.outputFile;
        addControl(this.outputFile);

        //this.createGroups = new GuiCheckBox("groups", "Create groups", 141, 14, false);
        //this.additionalFacePostProcessing = new GuiCheckBox("faces", "Additional face post processing", 141, 27, false);
        //this.exportTextureSprite = new GuiCheckBox("sprite", "Create sprite", 141, 40, false);
        //this.exportColorAsMaterialParam = new GuiCheckBox("mtlcolor", "Use MTL color property", 141, 53, false);
        
        //addControl(this.createGroups);
        //addControl(this.additionalFacePostProcessing);
        //addControl(this.exportTextureSprite);
        //addControl(this.exportColorAsMaterialParam);

        addControl(new GuiButton("exportbutton", "Export!", 145, 5, 42, 14) {
            
            @Override
            public void onClicked(int var1, int var2, int var3) {
                ItemStack stack = ModelExporterGui.this.parentStructure.input.inventory.getStackInSlot(0);
                
                if (stack.isEmpty() || !(stack.getItem() instanceof ItemLittleRecipeAdvanced)) {
                     postMessage(this.getPlayer(), TextFormatting.RED + "No blueprint to export!");
                     return;
                }
                
                Exporter exporter = new Exporter(
                    getPlayer(),
                    parentStructure.input.inventory.getStackInSlot(0),
                    ModelExporterGui.this.outputFolder.text,
                    ModelExporterGui.this.outputFile.text
                );
                
                Thread t = new Thread(exporter);
                t.start();
            }
        });
        
        // TODO: get rid of these if statements and initialize correct values at GUI creation
        /*if (this.parentStructure.createGroups) {
            this.createGroups.value = true;
        } else {
            this.createGroups.value = false;
        }*/
        
        /*if (this.parentStructure.additionalFacePostProcessing) {
            this.additionalFacePostProcessing.value = true;
        } else {
            this.additionalFacePostProcessing.value = false;
        }*/
        
        /*if (this.parentStructure.exportTextureSprite) {
            this.exportTextureSprite.value = true;
        } else {
            this.exportTextureSprite.value = false;
        }*/
        
        /*if (this.parentStructure.exportColorAsMaterialParam) {
            this.exportColorAsMaterialParam.value = true;
        } else {
            this.exportColorAsMaterialParam.value = false;
        }*/
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
        updateSettings();
    }

    public void updateSettings() {
        PacketStructureExporterNBT nbtPacket = new PacketStructureExporterNBT();
        NBTTagCompound packetNBT = new NBTTagCompound();
        packetNBT.setTag("loc", new StructureLocation(this.parentStructure).write());
        packetNBT.setString("exportfolder", this.outputFolder.text);
        packetNBT.setString("exportfile", this.outputFile.text);
        //packetNBT.setBoolean("groups", this.createGroups.value);
        //packetNBT.setBoolean("faces", this.additionalFacePostProcessing.value);
        //packetNBT.setBoolean("sprite", this.exportTextureSprite.value);
        //packetNBT.setBoolean("mtlcolor", this.exportColorAsMaterialParam.value);
        nbtPacket.setNBT(packetNBT);
        PacketHandler.sendPacketToServer(nbtPacket);
    }
}
