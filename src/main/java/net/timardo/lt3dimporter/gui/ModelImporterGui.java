package net.timardo.lt3dimporter.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.littletiles.common.item.ItemMultiTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

public class ModelImporterGui extends SubGui {
    
    public GuiTextfield modelFile;
    
    public ModelImporterGui() {
        super(200, 200);
    }

    @Override
    public void createControls() {
        this.modelFile = new GuiTextfield("modelFile", "", 20, 20, 100, 25);
        this.modelFile.maxLength = 32768; // people exceeding this are insane
        GuiComboBox gridSizes = new GuiComboBox("gridSizes", 120, 0, 15, LittleGridContext.getNames());
        gridSizes.select(ItemMultiTiles.currentContext.size);
        controls.add(gridSizes);
    }
    
}
