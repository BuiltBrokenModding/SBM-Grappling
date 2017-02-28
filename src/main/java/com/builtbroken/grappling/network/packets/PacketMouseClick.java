package com.builtbroken.grappling.network.packets;

import com.builtbroken.grappling.content.MovementHandler;
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
        //player.addChatComponentMessage(new ChatComponentText("MouseButton: " + button + "  pressed: " + state + "  wheel: " + dwheel));
        if (button == 0)
        {
            if (MovementHandler.hasHook(player))
            {
                if (state)
                {
                    MovementHandler.pullHook(player, 120);
                }
                else
                {
                    MovementHandler.stopMovement(player);
                }
            }
            else if (!state)
            {
                MovementHandler.createHook(player);
            }
        }
        else if (button == 1)
        {
            if (MovementHandler.hasHook(player) && !state)
            {
                MovementHandler.clearHook(player);
            }
        }
        else
        {
            MovementHandler.pullHook(player, dwheel);
        }
    }
}