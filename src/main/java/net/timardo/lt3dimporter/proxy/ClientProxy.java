package net.timardo.lt3dimporter.proxy;

import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import static net.timardo.lt3dimporter.LT3DImporter.*;

public class ClientProxy extends CommonProxy {
    
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        OBJLoader.INSTANCE.addDomain(MOD_ID);
    }
}
