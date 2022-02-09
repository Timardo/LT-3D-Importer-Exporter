package net.timardo.lt3dimporter;

import static net.timardo.lt3dimporter.Utils.postMessage;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class TestCommand extends CommandBase {
    
    private final List<String> aliases = Lists.newArrayList("exportTest");

    @Override
    public String getName() {
        return "testExport";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "testExport";
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
        // TODO: find out why this new thread blocks the chat thread
        postMessage(sender, "Exporting");
        Exporter exporter = new Exporter(server, sender);
        exporter.run();
        postMessage(sender, "Exported");
    }

}
