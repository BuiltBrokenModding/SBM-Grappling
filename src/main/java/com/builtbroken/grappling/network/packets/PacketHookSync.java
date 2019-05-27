package com.builtbroken.grappling.network.packets;

import java.util.HashMap;

import com.builtbroken.grappling.client.ClientHookHandler;
import com.builtbroken.grappling.content.Hook;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;

/**
 * Used to sync hook data to the client
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class PacketHookSync extends Packet
{
	public Hook playerHook = null;
	public HashMap<String, Vec3d> usernameToHookLocation = new HashMap<>();

	@Override
	public void write(ByteBuf buffer)
	{
		buffer.writeBoolean(playerHook != null);
		if(playerHook != null)
		{
			playerHook.write(buffer);
		}
	}

	@Override
	public void read(ByteBuf buffer)
	{
		if (buffer.readBoolean())
		{
			playerHook = Hook.read(buffer);
		}
		else
		{
			playerHook = null;
		}
	}

	@Override
	public void handleClientSide()
	{
		ClientHookHandler.setHook(playerHook);
	}
}
