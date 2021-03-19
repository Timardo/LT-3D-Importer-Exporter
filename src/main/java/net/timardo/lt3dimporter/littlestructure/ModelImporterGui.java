package net.timardo.lt3dimporter.littlestructure;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.LittleSubGuiUtils;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.timardo.lt3dimporter.converter.Converter;
import net.timardo.lt3dimporter.network.PacketStructureNBT;

public class ModelImporterGui extends SubGui {
    public GuiTextfield modelFile;
    public GuiComboBox gridSizes;
    public GuiTextfield minPrecision;
    public GuiTextfield texFile;
    public GuiColorPicker colorPicker;
    public GuiCheckBox useTex;
    public GuiCheckBox useColor;
    public GuiCheckBox useMtl;
    public GuiStackSelectorAll baseBlock;
    public GuiTextfield maxSize;
    public ModelImporter parentStructure;
    
    public ModelImporterGui(ModelImporter s) {
        super(220, 186);
        this.parentStructure = s;
    }

    @Override
    public void createControls() {
        this.modelFile = new GuiTextfield("modelfile", "", 5, 5, 128, 14);
        this.modelFile.maxLength = 32768; // people exceeding this are insane
        this.modelFile.setCustomTooltip("Path to model");
        this.modelFile.text = this.parentStructure.model;
        addControl(this.modelFile);
        
        this.gridSizes = new GuiComboBox("gridsizes", 144, 5, 27, LittleGridContext.getNames());
        this.gridSizes.setCustomTooltip("Grid");
        this.gridSizes.select(this.parentStructure.gridSize);
        addControl(this.gridSizes);
        
        this.minPrecision = new GuiTextfield("modelfile", "0.7", 182, 5, 27, 14);
        this.minPrecision.allowedChars = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'}; // only positive float values
        this.minPrecision.setCustomTooltip("Precision");
        this.minPrecision.text = this.parentStructure.precision;
        addControl(this.minPrecision);
        
        this.baseBlock = new GuiStackSelectorAll("baseblock", 5, 30, 106, getPlayer(), new GuiStackSelectorAll.CreativeCollector(new LittleSubGuiUtils.LittleBlockSelector()), true);
        this.baseBlock.setCustomTooltip("Base block");
        this.baseBlock.setSelectedForce(this.parentStructure.baseBlock);
        addControl(this.baseBlock);
        
        this.useTex = new GuiCheckBox("useTex", "Use Texture", 141, 27, false) {
            
            @Override
            public boolean mousePressed(int posX, int posY, int button) {
                if (button != -21) playSound(SoundEvents.UI_BUTTON_CLICK);
                if (useTex.value) return false;
                
                useTex.value = true;
                useColor.value = false;
                raiseEvent(new GuiControlChangedEvent(this)); // TODO find out how these events work and update all viewers of this structure's GUI when something changes
                removeControl(colorPicker);
                addControl(texFile);
                addControl(useMtl);
                
                return true;
            }
        };
        
        this.useColor = new GuiCheckBox("useColor", "Use Color", 141, 40, false) {
            
            @Override
            public boolean mousePressed(int posX, int posY, int button) {
                if (button != -21) playSound(SoundEvents.UI_BUTTON_CLICK);
                if (useColor.value) return false;
                
                useTex.value = false;
                useColor.value = true;
                raiseEvent(new GuiControlChangedEvent(this));
                removeControl(texFile);
                removeControl(useMtl);
                addControl(colorPicker);
                
                return true;
            }
        };
        
        this.useMtl = new GuiCheckBox("useMtl", "Use .mtl File", 2, 77, false) {
            
            @Override
            public boolean mousePressed(int posX, int posY, int button) {
                if (button != -21) playSound(SoundEvents.UI_BUTTON_CLICK);
                
                this.value = !this.value;
                
                if (useMtl.value) {
                    texFile.enabled = false;
                } else { 
                    texFile.enabled = true;
                }
                
                raiseEvent(new GuiControlChangedEvent(this));
                
                return true;
            }
        };
        
        addControl(this.useTex);
        addControl(this.useColor);
        
        this.texFile = new GuiTextfield("texfile", "", 5, 55, 128, 14);
        this.texFile.maxLength = 32768;
        this.texFile.setCustomTooltip("Path to texture");
        this.texFile.text = this.parentStructure.texFile;
        
        this.colorPicker = new GuiColorPicker("colorpicker", 4, 55, ColorUtils.IntToRGBA(this.parentStructure.color), LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer()));
        
        this.maxSize = new GuiTextfield("maxsize", this.parentStructure.maxSize, 144, 55, 65, 14);
        this.maxSize.setCustomTooltip("Max size");
        addControl(this.maxSize);
        
        addControl(new GuiButton("convertbutton", "Import!", 144, 80, 42, 14) {
            
            @Override
            public void onClicked(int var1, int var2, int var3) {
                Converter converter = new Converter(
                    modelFile.text,
                    texFile.text,
                    colorPicker.color,
                    Integer.parseInt(maxSize.text),
                    Integer.parseInt(gridSizes.getCaption()),
                    Float.parseFloat(minPrecision.text),
                    baseBlock.getSelected(),
                    useTex.value,
                    useMtl.value,
                    getPlayer(),
                    parentStructure
                );
                
                Thread t = new Thread(converter);
                t.start();
            }
            
        });
        
        if (this.parentStructure.useTex) {
            this.useTex.mousePressed(0, 0, -21);
        } else {
            this.useColor.mousePressed(0, 0, -21);
        }
        
        if (parentStructure.useMtl) {
            this.useMtl.mousePressed(0, 0, -21);
        }
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
        updateSettings();
    }

    public void updateSettings() {
        PacketStructureNBT nbtPacket = new PacketStructureNBT();
        NBTTagCompound packetNBT = new NBTTagCompound();
        packetNBT.setBoolean("item", false);
        packetNBT.setTag("loc", new StructureLocation(this.parentStructure).write());
        packetNBT.setString("model", this.modelFile.text);
        packetNBT.setString("tex_file", this.texFile.text);
        packetNBT.setInteger("color", ColorUtils.RGBAToInt(this.colorPicker.color));
        packetNBT.setString("max_size", this.maxSize.text);
        packetNBT.setString("grid", this.gridSizes.getCaption());
        packetNBT.setString("precision", this.minPrecision.text);
        packetNBT.setTag("base_block", this.baseBlock.getSelected().serializeNBT());
        packetNBT.setBoolean("use_tex", this.useTex.value);
        packetNBT.setBoolean("use_mtl", this.useMtl.value);
        nbtPacket.setNBT(packetNBT);
        PacketHandler.sendPacketToServer(nbtPacket);
    }
}
