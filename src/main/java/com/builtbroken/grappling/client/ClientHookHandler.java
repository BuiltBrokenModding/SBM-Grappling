package com.builtbroken.grappling.client;

import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.content.Hook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
@EventBusSubscriber(modid=GrapplingHookMod.MODID, value=Side.CLIENT)
public class ClientHookHandler
{
	/** Client side instance of the player's grappling hook */
	public static Hook hook;

	public static void setHook(Hook hook)
	{
		ClientHookHandler.hook = hook;
		//Override movement controls to improve handling
		if (hook != null && !(Minecraft.getMinecraft().player.movementInput instanceof MovementInputOverride))
		{
			Minecraft.getMinecraft().player.movementInput = new MovementInputOverride(Minecraft.getMinecraft().player.movementInput);
		}
	}

	@SubscribeEvent
	public static void playerTickEvent(TickEvent.PlayerTickEvent event)
	{
		EntityPlayerSP player = Minecraft.getMinecraft().player;

		if (hook != null && event.player == Minecraft.getMinecraft().player)
		{
			double xDifference = hook.x - player.posX;
			double yDifference = hook.y - player.posY;
			double zDifference = hook.z - player.posZ;

			if (hook.movement != 0)
			{
				player.motionX = 0;
				player.motionY = 0;
				player.motionZ = 0;
			}
			else
			{
				if (Math.abs(yDifference) >= hook.distance)
				{
					player.motionY = 0;
				}

				if (Math.abs(xDifference) >= hook.distance)
				{
					player.motionX = 0;
				}

				if (Math.abs(zDifference) >= hook.distance)
				{
					player.motionZ = 0;
				}
			}

			//Reset motion so we fall / move
			if (!player.onGround && hook.movement == 0 && (Math.abs(xDifference) > .1 || Math.abs(zDifference) > .1))
			{
				pullTowardsLowestPoint(player, hook);
			}
		}
	}

	private static void pullTowardsLowestPoint(EntityPlayer player, Hook hook)
	{
		final Vec3d desiredPosition = new Vec3d(hook.x, hook.y - hook.distance, hook.z);

		//Get difference in positions
		double xDifference = desiredPosition.x - player.posX;
		double yDifference = desiredPosition.y - player.posY;
		double zDifference = desiredPosition.z - player.posZ;

		//Get distance
		double distance = Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);

		//Normalize
		double xNormalized = xDifference / distance;
		double yNormalized = yDifference / distance;
		double zNormalized = zDifference / distance;

		//Create pull
		Vec3d pull = new Vec3d(xNormalized * 0.02, yNormalized * 0.02, zNormalized * 0.02);

		//Apply acceleration to player
		player.motionX += pull.x;
		player.motionY += pull.y;
		player.motionZ += pull.z;
	}
}
