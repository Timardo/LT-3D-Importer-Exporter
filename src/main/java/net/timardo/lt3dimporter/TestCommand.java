package net.timardo.lt3dimporter;

import static net.timardo.lt3dimporter.Utils.postMessage;

import java.util.List;

import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;
import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class TestCommand extends CommandBase {
    
    private final List<String> aliases = Lists.newArrayList("exportTest");

    @Override
    public String getName() {
        return "testExport";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "testExport [file_name] [folder_path]";
    }
    
    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String fileName = args.length < 1 ? "exported" : args[0];
        String folderName = args.length < 2 ? "" : args[1];
        // TODO: find out why this new thread blocks the chat thread
        ItemStack item = ((EntityPlayer)sender).getHeldItemMainhand();
        
        if (item == ItemStack.EMPTY || !(item.getItem() instanceof ItemLittleRecipeAdvanced)) {
            postMessage(sender, TextFormatting.RED + "Empty hand or not a blueprint!");
            return;
        }
        postMessage(sender, "Exporting");
        Exporter exporter = new Exporter((EntityPlayer)sender, item, folderName, fileName);
        Thread thread = new Thread(exporter);
        thread.run();
    }
}
