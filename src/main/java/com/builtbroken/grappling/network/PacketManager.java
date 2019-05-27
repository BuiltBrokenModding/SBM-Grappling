package com.builtbroken.grappling.network;

import java.util.EnumMap;

import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.network.packets.Packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author tgame14
 * @since 26/05/14
 */
public class PacketManager
{
	public final String channel;
	public EnumMap<Side, FMLEmbeddedChannel> channelEnumMap;

	public PacketManager(String channel)
	{
		this.channel = channel;
		channelEnumMap = NetworkRegistry.INSTANCE.newChannel(channel, new ChannelHandler(), new PacketHandler());
	}

	public net.minecraft.network.Packet<?> toMCPacket(Packet packet)
	{
		return channelEnumMap.get(FMLCommonHandler.instance().getEffectiveSide()).generatePacketFrom(packet);
	}

	/**
	 * @param packet the packet to send to the player
	 * @param player the player MP object
	 */
	public void sendToPlayer(Packet packet, EntityPlayerMP player)
	{
		//Null check is for JUnit
		if (channelEnumMap != null)
		{
			this.channelEnumMap.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
			this.channelEnumMap.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
			this.channelEnumMap.get(Side.SERVER).writeAndFlush(packet);
		}
		else
		{
			GrapplingHookMod.logger.error("Packet sent to player[" + player + "]");
		}
	}

	/**
	 * @param packet the packet to send to the players in the dimension
	 * @param dimId  the dimension ID to send to.
	 */
	public void sendToAllInDimension(Packet packet, int dimId)
	{
		//Null check is for JUnit
		if (channelEnumMap != null)
		{
			this.channelEnumMap.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
			this.channelEnumMap.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimId);
			this.channelEnumMap.get(Side.SERVER).writeAndFlush(packet);
		}
		else
		{
			GrapplingHookMod.logger.error("Packet sent to dim[" + dimId + "]");
		}
	}

	public void sendToAllInDimension(Packet packet, World world)
	{
		sendToAllInDimension(packet, world.provider.getDimension());
	}

	/**
	 * sends to all clients connected to the server
	 *
	 * @param packet the packet to send.
	 */
	public void sendToAll(Packet packet)
	{
		//Null check is for JUnit
		if (channelEnumMap != null)
		{
			this.channelEnumMap.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
			this.channelEnumMap.get(Side.SERVER).writeAndFlush(packet);
		}
		else
		{
			GrapplingHookMod.logger.error("Packet sent to all");
		}
	}

	public void sendToAllAround(Packet message, NetworkRegistry.TargetPoint point)
	{
		//Null check is for JUnit
		if (channelEnumMap != null)
		{
			this.channelEnumMap.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
			this.channelEnumMap.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
			this.channelEnumMap.get(Side.SERVER).writeAndFlush(message);
		}
		else
		{
			GrapplingHookMod.logger.error("Packet sent to target point: " + point);
		}
	}

	public void sendToAllAround(Packet message, World world, double x, double y, double z, double range)
	{
		if (world != null)
			sendToAllAround(message, new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, range));
	}

	@SideOnly(Side.CLIENT)
	public void sendToServer(Packet packet)
	{
		//Null check is for JUnit
		if (channelEnumMap != null)
		{
			this.channelEnumMap.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
			this.channelEnumMap.get(Side.CLIENT).writeAndFlush(packet);
		}
		else
		{
			GrapplingHookMod.logger.error("Packet sent to server");
		}
	}
}


