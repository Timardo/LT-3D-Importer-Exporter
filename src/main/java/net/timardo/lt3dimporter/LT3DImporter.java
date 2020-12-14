package net.timardo.lt3dimporter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.timardo.lt3dimporter.container.ModelImporterContainer;
import net.timardo.lt3dimporter.gui.ModelImporterGui;
import net.timardo.lt3dimporter.littlestructure.ModelImporter;

import org.apache.logging.log4j.Logger;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;

@Mod(modid = LT3DImporter.MOD_ID, name = LT3DImporter.NAME, version = LT3DImporter.VERSION)
public class LT3DImporter {
    public static final String MOD_ID = "lt3dimporter";
    public static final String NAME = "LT3DImporter";
    public static final String VERSION = "0.2";
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        // GUI WIP 
        GuiHandler.registerGuiHandler("modelimporter", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer p, NBTTagCompound nbt, LittleStructure s) {
                return new ModelImporterGui();
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer p, NBTTagCompound nbt, LittleStructure s) {
                return new ModelImporterContainer(p);
            }
        });
    }
    
    @EventHandler
    public void Init(FMLInitializationEvent event) {
        LittleStructurePremade.registerPremadeStructureType("modelimporter", MOD_ID, ModelImporter.class);
    }
    
    @EventHandler
    public void fml(FMLServerStartingEvent e) {
        e.registerServerCommand(new TestCommand());
    }
}
