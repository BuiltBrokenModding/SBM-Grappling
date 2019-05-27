package com.builtbroken.grappling.network;

import com.builtbroken.grappling.network.packets.Packet;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * @author tgame14
 * @since 31/05/14
 */
@ChannelHandler.Sharable
public class PacketHandler extends SimpleChannelInboundHandler<Packet>
{
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception
	{
		INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();

		switch (FMLCommonHandler.instance().getEffectiveSide())
		{
			case CLIENT:
				packet.handleClientSide();
				break;
			case SERVER:
				packet.handleServerSide(((NetHandlerPlayServer) netHandler).player);
				break;
			default:
				break;
		}

	}

}
