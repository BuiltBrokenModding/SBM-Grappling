package com.builtbroken.grappling.network.packets;

import com.builtbroken.grappling.GrapplingHookMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 11/18/2016.
 */
public class PacketMouseClick extends Packet
{
    int slot;
    int button;
    int dwheel;
    boolean state;

    public PacketMouseClick()
    {
        //Needed for forge to construct the packet
    }

    public PacketMouseClick(int slotId, int button, boolean state, int dwheel)
    {
        this.slot = slotId;
        this.button = button;
        this.state = state;
        this.dwheel = dwheel;
    }

    @Override
    public void write(ByteBuf buffer)
    {
        buffer.writeInt(slot);
        buffer.writeInt(button);
        buffer.writeInt(dwheel);
        buffer.writeBoolean(state);
    }

    @Override
    public void read(ByteBuf buffer)
    {
        slot = buffer.readInt();
        button = buffer.readInt();
        dwheel = buffer.readInt();
        state = buffer.readBoolean();
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        GrapplingHookMod.proxy.handleMouseInput(player, button, state, dwheel);
    }
}