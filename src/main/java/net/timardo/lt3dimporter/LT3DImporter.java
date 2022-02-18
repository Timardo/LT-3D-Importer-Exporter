package net.timardo.lt3dimporter;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.timardo.lt3dimporter.littlestructure.ModelImporter;
import net.timardo.lt3dimporter.littlestructure.ModelImporterContainer;
import net.timardo.lt3dimporter.littlestructure.ModelImporterGui;
import net.timardo.lt3dimporter.network.PacketStructureNBT;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.Logger;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;

@Mod.EventBusSubscriber
@Mod(modid = LT3DImporter.MOD_ID, name = LT3DImporter.NAME, version = LT3DImporter.VERSION, dependencies = "required-after:creativecore;required-after:littletiles")
public class LT3DImporter {
    public static final String MOD_ID = "lt3dimporter";
    public static final String NAME = "Little Tiles 3D Importer";
    public static final String VERSION = "0.7";
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();

        GuiHandler.registerGuiHandler("modelimporter", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer p, NBTTagCompound nbt, LittleStructure s) {
                return new ModelImporterGui((ModelImporter) s);
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer p, NBTTagCompound nbt, LittleStructure s) {
                return new ModelImporterContainer(p, (ModelImporter) s);
            }
        });
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        LittleStructurePremade.registerPremadeStructureType("modelimporter", LT3DImporter.MOD_ID, ModelImporter.class);
        CreativeCorePacket.registerPacket(PacketStructureNBT.class);
    }
    
    @EventHandler
    public void serverInit(FMLServerStartingEvent event) {
        event.registerServerCommand(new TestCommand());
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        //event.getRegistry().register(new TestBlock());
    }
    
    @GameRegistry.ObjectHolder("lt3dimporter:modelblock")
    public static TestBlock testBlock;
    
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        //testBlock.initModel();
    }
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        //event.getRegistry().register(new ItemBlock(testBlock).setRegistryName(testBlock.getRegistryName()));
    }
}
