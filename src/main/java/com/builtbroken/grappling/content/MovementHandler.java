package com.builtbroken.grappling.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.content.entity.EntityHook;
import com.builtbroken.grappling.network.packets.PacketHookSync;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.server.FMLServerHandler;

/**
 * Handles client side movement syncing
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
@EventBusSubscriber(modid=GrapplingHookMod.MODID)
public class MovementHandler
{
	public static HashMap<EntityPlayer, Hook> playerToHook = new HashMap<EntityPlayer, Hook>();

	/**
	 * Called to check if the player has an active hook placed in the world
	 *
	 * @param player
	 * @return
	 */
	public static boolean hasHook(EntityPlayer player)
	{
		return playerToHook.containsKey(player);
	}

	/**
	 * Checks if the player is currently set to move
	 *
	 * @param player
	 * @return
	 */
	public static boolean isMoving(EntityPlayer player)
	{
		return hasHook(player) && playerToHook.get(player).movement != 0;
	}

	/**
	 * Called to create a new hook from the player's aim position
	 *
	 * @param player
	 */
	public static void createHook(EntityPlayer player)
	{
		RayTraceResult movingobjectposition = _doRayTrace(player, player.rotationYawHead, player.rotationPitch);
		if (movingobjectposition != null && movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK)
		{
			//Create hook entity
			Hook hook = new Hook();
			hook.side = movingobjectposition.sideHit;
			hook.x = movingobjectposition.hitVec.x;
			hook.y = movingobjectposition.hitVec.y;
			hook.z = movingobjectposition.hitVec.z;
			hook.playerEntityID = player.getEntityId();
			hook.distance = getDistanceToHook(hook, player);
			playerToHook.put(player, hook);

			//Output that a hook has been created
			player.sendMessage(new TextComponentString("Hook created"));

			//Generate hook render entity
			EntityHook entityHook = new EntityHook(player.world);
			entityHook.owner = player;
			entityHook.hook = hook;
			entityHook.setPosition(movingobjectposition.hitVec.x, movingobjectposition.hitVec.y, movingobjectposition.hitVec.z);
			player.world.spawnEntity(entityHook);
		}
	}

	/**
	 * Called to run the ray trace
	 *
	 * @param player - player
	 * @param yaw    - rotation
	 * @param pitch  - pitch
	 * @return hit from raytrace
	 */
	protected static RayTraceResult _doRayTrace(EntityPlayer player, float yaw, float pitch)
	{
		//Figure out where the player is aiming
		final Vec3d aim = getAim(yaw, pitch);

		//Find our hand Vec3ition so to Vec3ition starting point near barrel of the gun
		final float rotationHand = MathHelper.wrapDegrees(player.renderYawOffset + 90);
		final double r = Math.toRadians(rotationHand);
		final Vec3d hand = new Vec3d(
				(Math.cos(r) - Math.sin(r)) * 0.5,
				0,
				(Math.sin(r) + Math.cos(r)) * 0.5
				);

		final Vec3d entityVec3 = new Vec3d(player.posX + hand.x, player.posY + 1.1 + hand.y, player.posZ + hand.z);

		final Vec3d start = entityVec3;
		final Vec3d end = new Vec3d(
				aim.x * GrapplingHookMod.HOOK_REACH_DISTANCE + start.x,
				aim.y * GrapplingHookMod.HOOK_REACH_DISTANCE + start.y,
				aim.z * GrapplingHookMod.HOOK_REACH_DISTANCE + start.z);

		return player.world.rayTraceBlocks(start, end);
	}

	/**
	 * Gets the aim using the pitch and yaw
	 *
	 * @param yaw   - yaw
	 * @param pitch - pitch
	 * @return - aim as a vector
	 */
	protected static Vec3d getAim(float yaw, float pitch)
	{
		float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f3 = -MathHelper.cos(-pitch * 0.017453292F);
		float f4 = MathHelper.sin(-pitch * 0.017453292F);
		return new Vec3d(f2 * f3, f4, f1 * f3);
	}

	/**
	 * Called to clear the current player's hook
	 *
	 * @param player
	 */
	public static void clearHook(EntityPlayer player)
	{
		playerToHook.remove(player);
		player.sendMessage(new TextComponentString("Hook removed"));
		player.fallDistance = 0;
	}

	/**
	 * Called to pull the player towards the hook position
	 *
	 * @param player
	 * @param amount - scroll wheel movement, if negative it means release
	 */
	public static void pullHook(EntityPlayer player, int amount)
	{
		if (hasHook(player))
		{
			playerToHook.get(player).movement = amount;
		}
	}

	/**
	 * Data object to store hook position
	 */


	@SubscribeEvent
	public static void tickEvent(ServerTickEvent event)
	{
		if(FMLServerHandler.instance().getServer() != null)
		{
			//TODO check if player still has hook on hotbar
			if (event.phase == TickEvent.Phase.START)
			{
				List<EntityPlayer> removeList = new ArrayList<>();
				//Loop all hooks to update player movement
				for (Map.Entry<EntityPlayer, Hook> entry : playerToHook.entrySet())
				{
					if (entry.getKey() instanceof EntityPlayerMP)
					{
						Hook hook = entry.getValue();
						EntityPlayerMP player = (EntityPlayerMP) entry.getKey();

						boolean hasHook = false;
						for (int i = 0; i < 9; i++)
						{
							ItemStack stack = player.inventory.getStackInSlot(i);
							if (stack != null && stack.getItem() == GrapplingHookMod.itemHook)
							{
								hasHook = true;
								break;
							}
						}

						double distance = getDistanceToHook(hook, player);

						if (!hasHook || distance >= GrapplingHookMod.HOOK_REACH_DISTANCE + 10)
						{
							removeList.add(player);
							continue;
						}

						//Only update if movement is above 0
						if (entry.getValue().movement > 0)
						{
							//Update position
							if (distance > 1)
							{
								//Move entity
								Vec3d pull = getPullDirection(hook, player);
								player.move(MoverType.PLAYER, pull.x * GrapplingHookMod.HOOK_PULL_PERCENT, pull.y * GrapplingHookMod.HOOK_PULL_PERCENT, pull.z * GrapplingHookMod.HOOK_PULL_PERCENT);
								player.setPositionAndUpdate(entry.getKey().posX, entry.getKey().posY, entry.getKey().posZ);

								//Update rope distance
								hook.distance = getDistanceToHook(hook, player);
							}
						}
						else if (entry.getValue().movement < 0)
						{
							hook.distance += 0.2D;
						}
					}
				}

				//Remove players from hook data who are in  the remove list
				for (EntityPlayer player : removeList)
				{
					clearHook(player);
				}
			}
			else
			{
				//Loop all players (this is an O(n^2) operation)
				for (EntityPlayer player : FMLServerHandler.instance().getServer().getPlayerList().getPlayers())
				{
					PacketHookSync packetHookSync = new PacketHookSync();
					packetHookSync.playerHook = playerToHook.get(player);
					for (Map.Entry<EntityPlayer, Hook> entry : playerToHook.entrySet())
					{
						if (entry.getKey() != player && getDistanceToHook(entry.getValue(), player) < 200)
						{
							packetHookSync.usernameToHookLocation.put(player.getName(), entry.getValue().toVec3());
						}
					}
					GrapplingHookMod.packetHandler.sendToPlayer(packetHookSync, (EntityPlayerMP) player);
				}
			}
		}
	}


	/**
	 * Gets the direction in which to pull the player towards the hook
	 *
	 * @param hook   - location of the hook
	 * @param player - player
	 * @return vector representing the direction
	 */

	public static Vec3d getPullDirection(Hook hook, EntityPlayer player)
	{
		//Get delta
		double xDifference = hook.x - player.posX;
		double yDifference = hook.y - player.posY;
		double zDifference = hook.z - player.posZ;
		//Get mag (flat distance)
		double mag = Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
		//Normalize
		xDifference = xDifference / mag;
		yDifference = yDifference / mag;
		zDifference = zDifference / mag;

		return new Vec3d(xDifference, yDifference, zDifference);
	}

	/**
	 * Gets the distance to the hook from the player
	 *
	 * @param hook
	 * @param player
	 * @return
	 */
	public static double getDistanceToHook(Hook hook, EntityPlayer player)
	{
		double xDifference = hook.x - player.posX;
		double yDifference = hook.y - player.posY;
		double zDifference = hook.z - player.posZ;
		return Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
	}
}
