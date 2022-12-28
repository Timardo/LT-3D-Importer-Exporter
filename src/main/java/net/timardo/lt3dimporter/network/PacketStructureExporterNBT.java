package net.timardo.lt3dimporter.network;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.timardo.lt3dimporter.LT3DImporter;
import net.timardo.lt3dimporter.littlestructure.ModelExporter;

public class PacketStructureExporterNBT extends CreativeCorePacket {
    
    private NBTTagCompound nbt;
    
    public PacketStructureExporterNBT() {}
    
    public void setNBT(NBTTagCompound nbt) {
        this.nbt = nbt;
    }
    
    @Override
    public void writeBytes(ByteBuf b) {
        writeNBT(b, this.nbt);
    }

    @Override
    public void readBytes(ByteBuf b) {
        this.nbt = readNBT(b);
    }

    @Override
    public void executeClient(EntityPlayer player) {
        
    }

    @Override
    public void executeServer(EntityPlayer player) {
        if (this.nbt.hasNoTags()) return;
        
        ModelExporter structure = null;
        StructureLocation loc = new StructureLocation((NBTTagCompound) this.nbt.getTag("loc"));
        
        try {
            structure = (ModelExporter) loc.find(player.world);
        } catch (LittleActionException lae) {
            LT3DImporter.logger.error("Failed to get structure from packet");
            return;
        }
        
        structure.outputFolder = this.nbt.getString("exportfolder");
        structure.outputFile = this.nbt.getString("exportfile");
        structure.createGroups = this.nbt.getBoolean("groups");
        structure.additionalFacePostProcessing = this.nbt.getBoolean("faces");
        structure.exportTextureSprite = this.nbt.getBoolean("sprite");
        structure.exportColorAsMaterialParam = this.nbt.getBoolean("mtlcolor");
    }
}
