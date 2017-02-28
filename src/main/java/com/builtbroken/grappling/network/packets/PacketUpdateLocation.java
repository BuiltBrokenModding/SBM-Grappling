package com.builtbroken.grappling.network.packets;

import com.builtbroken.grappling.GrapplingHookMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Updates the player's position client side
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class PacketUpdateLocation extends Packet
{
    double x, y, z;

    public PacketUpdateLocation()
    {

    }

    public PacketUpdateLocation(EntityPlayer player)
    {
        x = player.posX;
        y = player.posY;
        z = player.posZ;
    }

    @Override
    public void write(ByteBuf buffer)
    {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    @Override
    public void read(ByteBuf buffer)
    {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }

    @Override
    public void handleClientSide()
    {
        GrapplingHookMod.proxy.setPlayerPosition(x, y, z);
    }
}
