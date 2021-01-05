package net.timardo.lt3dimporter.littlestructure;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.LittleSubGuiUtils;
import com.creativemd.littletiles.common.item.ItemMultiTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.timardo.lt3dimporter.converter.Converter;
import net.timardo.lt3dimporter.converter.ImportException;

public class ModelImporterGui extends SubGui {
    
    public GuiTextfield modelFile;
    public GuiComboBox gridSizes;
    public GuiTextfield minPrecision;
    public GuiTextfield texFile;
    public GuiColorPicker colorPicker;
    public GuiCheckBox useTex;
    public GuiCheckBox useColor;
    public GuiStackSelectorAll baseBlock;
    public GuiTextfield maxSize;
    public GuiButton convertButton;
    private ModelImporterGui instance;
    
    public ModelImporterGui() {
        super(220, 186);
        this.instance = this;
    }

    @Override
    public void createControls() {
        this.modelFile = new GuiTextfield("modelfile", "", 5, 5, 128, 14);
        this.modelFile.maxLength = 32768; // people exceeding this are insane
        this.modelFile.setCustomTooltip("Path to model");
        addControl(this.modelFile);
        
        this.gridSizes = new GuiComboBox("gridsizes", 144, 5, 27, LittleGridContext.getNames());
        this.gridSizes.select(ItemMultiTiles.currentContext.size);
        this.gridSizes.setCustomTooltip("Grid");
        addControl(this.gridSizes);
        
        this.minPrecision = new GuiTextfield("modelfile", "0.7", 182, 5, 27, 14);
        this.minPrecision.allowedChars = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'}; // only positive float values
        this.minPrecision.setCustomTooltip("Precision");
        addControl(this.minPrecision);
        
        this.baseBlock = new GuiStackSelectorAll("baseblock", 5, 30, 106, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
        this.baseBlock.setCustomTooltip("Base block");
        this.baseBlock.setSelectedForce(new ItemStack(LittleTiles.coloredBlock));
        
        addControl(this.baseBlock);
        
        this.useTex = new GuiCheckBox("useTex", "Use Texture", 141, 27, true) {
            @Override
            public boolean mousePressed(int posX, int posY, int button) {
                playSound(SoundEvents.UI_BUTTON_CLICK);
                
                if (useTex.value) return false;
                
                useTex.value = true;
                useColor.value = false;
                raiseEvent(new GuiControlChangedEvent(this)); // TODO find out how these events work
                removeControl(colorPicker);
                addControl(texFile);
                return true;
            }
        };
        
        this.useColor = new GuiCheckBox("useColor", "Use Color", 141, 40, false) {
            @Override
            public boolean mousePressed(int posX, int posY, int button) {
                playSound(SoundEvents.UI_BUTTON_CLICK);
                
                if (useColor.value) return false;
                
                useTex.value = false;
                useColor.value = true;
                raiseEvent(new GuiControlChangedEvent(this));
                removeControl(texFile);
                addControl(colorPicker);
                return true;
            }
        };
        
        addControl(this.useTex);
        addControl(this.useColor);
        
        this.texFile = new GuiTextfield("texfile", "", 5, 55, 128, 14);
        this.texFile.maxLength = 32768;
        this.texFile.setCustomTooltip("Path to texture");
        addControl(this.texFile);
        
        this.colorPicker = new GuiColorPicker("colorpicker", 4, 55, new Color(255, 255, 255, 255), LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer()));
        
        this.maxSize = new GuiTextfield("maxsize", (Integer.parseInt(this.gridSizes.caption) * 16) + "", 144, 55, 65, 14);
        this.maxSize.setCustomTooltip("Max size");
        addControl(this.maxSize);
        
        this.convertButton = new GuiButton("convertbutton", "Import!", 144, 80, 42, 14) {

            @Override
            public void onClicked(int var1, int var2, int var3) {
                Converter converter = new Converter(
                    modelFile.text,
                    texFile.text,
                    colorPicker.color,
                    Integer.parseInt(maxSize.text),
                    Integer.parseInt(gridSizes.caption),
                    Float.parseFloat(minPrecision.text),
                    baseBlock.getSelected(),
                    useTex.value,
                    instance,
                    getPlayer()
                );
                
                Thread t = new Thread(converter);
                t.start();
            }
            
        };
        
        addControl(this.convertButton);
    }
}
