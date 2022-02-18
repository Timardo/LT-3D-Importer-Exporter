package net.timardo.lt3dimporter;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

import static com.creativemd.creativecore.common.utils.mc.ColorUtils.*;

public class Utils {
    
    public static void postMessage(ICommandSender receiver, String msg) {
        receiver.sendMessage(new TextComponentString(msg));
    }
    
    public static int multiplyColor(int color1, int color2) {
        return RGBAToInt(getRedDecimal(color1) * getRedDecimal(color2), getGreenDecimal(color1) * getGreenDecimal(color2), getBlueDecimal(color1) * getBlueDecimal(color2), getAlphaDecimal(color1) * getAlphaDecimal(color2));
    }
}
