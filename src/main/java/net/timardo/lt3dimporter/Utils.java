package net.timardo.lt3dimporter;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class Utils {
    
    public static void postMessage(ICommandSender receiver, String msg) {
        receiver.sendMessage(new TextComponentString(msg));
    }
}
