package net.timardo.lt3dimporter;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.timardo.lt3dimporter.converter.ConvertUtil;

public class TestCommand implements ICommand {
    
    private final List<String> aliases = new ArrayList<String>();
    
    public TestCommand() {
        this.aliases.add("3d");
    }

    @Override
    public int compareTo(ICommand arg0) {
        return 0;
    }

    @Override
    public String getName() {
        return "3dconvert";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/3dconvert <3dmodel_file> <max_size> <grid_size> <precision> [texture_file] [color]";
    }

    @Override
    public List<String> getAliases() {
        return this.aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 4) {
            sender.sendMessage(new TextComponentString(this.getUsage(sender)));
            return;
        }
        
        ((EntityPlayer) sender).addItemStackToInventory(ConvertUtil.convertModelToRecipe(args[0], args.length > 4  && args[4].contains(".") ? args[4] : null, args.length > 4 && !(args[4].contains(".")) ? new String[] { args[4], args[5], args[6] } : null, args[1], args[2], args[3]));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

}
