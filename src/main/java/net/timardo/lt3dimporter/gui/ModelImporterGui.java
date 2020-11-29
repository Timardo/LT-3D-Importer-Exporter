package net.timardo.lt3dimporter.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;

public class ModelImporterGui extends SubGui {
    
    public GuiTextfield modelFile;
    
    public ModelImporterGui() {
        super(200, 200);
    }

    @Override
    public void createControls() {
        this.modelFile = new GuiTextfield("modelFile", "", 20, 20, 100, 25);
        this.modelFile.maxLength = 32768; // people exceeding this are insane
    }
    
}
