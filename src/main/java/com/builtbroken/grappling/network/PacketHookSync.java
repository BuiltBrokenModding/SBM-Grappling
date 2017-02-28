package com.builtbroken.grappling.network;

import com.builtbroken.grappling.content.MovementHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.Vec3;

import java.util.HashMap;

/**
 * Used to sync hook data to the client
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class PacketHookSync extends Packet
{
    public MovementHandler.Hook playerHook;
    public HashMap<String, Vec3> usernameToHookLocation = new HashMap();

    @Override
    public void write(ByteBuf buffer)
    {

    }

    @Override
    public void read(ByteBuf buffer)
    {

    }

    @Override
    public void handleClientSide()
    {

    }
}
