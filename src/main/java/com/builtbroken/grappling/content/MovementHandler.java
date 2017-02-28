package com.builtbroken.grappling.content;

import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.network.PacketHookSync;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles client side movement syncing
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
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

    public static void stopMovement(EntityPlayer player)
    {
        if (hasHook(player))
        {
            playerToHook.get(player).movement = 0;
        }
    }

    /**
     * Called to create a new hook from the player's aim position
     *
     * @param player
     */
    public static void createHook(EntityPlayer player)
    {
        MovingObjectPosition movingobjectposition = _doRayTrace(player, player.rotationYawHead, player.rotationPitch);
        if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            Hook hook = new Hook();
            hook.x = movingobjectposition.hitVec.xCoord;
            hook.y = movingobjectposition.hitVec.yCoord;
            hook.z = movingobjectposition.hitVec.zCoord;
            hook.side = movingobjectposition.sideHit;
            hook.distance = getDistanceToHook(hook, player);

            playerToHook.put(player, hook);
            player.addChatComponentMessage(new ChatComponentText("Hook created"));
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
    protected static MovingObjectPosition _doRayTrace(EntityPlayer player, float yaw, float pitch)
    {
        //Figure out where the player is aiming
        final Vec3 aim = getAim(yaw, pitch);

        //Find our hand Vec3ition so to Vec3ition starting point near barrel of the gun
        float rotationHand = MathHelper.wrapAngleTo180_float(player.renderYawOffset + 90);
        final Vec3 hand = Vec3.createVectorHelper(
                (Math.cos(Math.toRadians(rotationHand)) - Math.sin(Math.toRadians(rotationHand))) * 0.5,
                0,
                (Math.sin(Math.toRadians(rotationHand)) + Math.cos(Math.toRadians(rotationHand)) * 0.5)
        );

        final Vec3 entityVec3 = Vec3.createVectorHelper(player.posX + hand.xCoord, player.posY + 1.1 + hand.yCoord, player.posZ + hand.zCoord);

        final Vec3 start = entityVec3;
        final Vec3 end = Vec3.createVectorHelper(
                aim.xCoord * GrapplingHookMod.HOOK_REACH_DISTANCE + start.xCoord,
                aim.yCoord * GrapplingHookMod.HOOK_REACH_DISTANCE + start.yCoord,
                aim.zCoord * GrapplingHookMod.HOOK_REACH_DISTANCE + start.zCoord);

        return player.worldObj.rayTraceBlocks(start, end);
    }

    /**
     * Gets the aim using the pitch and yaw
     *
     * @param yaw   - yaw
     * @param pitch - pitch
     * @return - aim as a vector
     */
    protected static Vec3 getAim(float yaw, float pitch)
    {
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        return Vec3.createVectorHelper((double) (f2 * f3), (double) f4, (double) (f1 * f3));
    }

    /**
     * Called to clear the current player's hook
     *
     * @param player
     */
    public static void clearHook(EntityPlayer player)
    {
        playerToHook.remove(player);
        player.addChatComponentMessage(new ChatComponentText("Hook removed"));
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
    public static class Hook
    {
        /** Hook location */
        public double x, y, z;
        /** Current rope distance */
        public double distance;
        /** Side of the block attached to */
        public int side;
        /** Direction and speed of movement */
        public int movement;

        public Vec3 toVec3()
        {
            return Vec3.createVectorHelper(x, y, z);
        }

        //TODO track rope distance so we can hold the player mid air

        @Override
        public String toString()
        {
            return "Hook[" + x + ", " + y + ", " + z + ", " + side + ", " + movement + "]@" + hashCode();
        }
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            //Loop all hooks to update player movement
            for (Map.Entry<EntityPlayer, Hook> entry : playerToHook.entrySet())
            {
                Hook hook = entry.getValue();
                EntityPlayer player = entry.getKey();

                //Only update if movement is above 0
                if (entry.getValue().movement > 0)
                {
                    //Update position
                    double distance = getDistanceToHook(hook, player);
                    if (distance > 1)
                    {
                        //Move entity
                        Vec3 pull = getPullDirection(hook, player);
                        player.moveEntity(pull.xCoord * GrapplingHookMod.HOOK_PULL_PERCENT, pull.yCoord * GrapplingHookMod.HOOK_PULL_PERCENT, pull.zCoord * GrapplingHookMod.HOOK_PULL_PERCENT);

                        //Update rope distance
                        double distance2 = getDistanceToHook(hook, player);
                        hook.distance -= distance - distance2;
                    }
                }

                //Update position to prevent getting out of rope distance
                double distance = getDistanceToHook(hook, player);
                double delta = distance - hook.distance;
                if (delta > 0.001)
                {
                    float percent = (float) (delta / hook.distance);
                    Vec3 pull = getPullDirection(hook, player);
                    pull = Vec3.createVectorHelper(pull.xCoord * percent, pull.yCoord * percent, pull.zCoord * percent);
                    player.moveEntity(pull.xCoord, pull.yCoord, pull.zCoord);
                }
                else
                {
                    //Assume if player gets closer hook auto retracts rope
                    hook.distance = distance;
                }

                //Send update packet to client so position syncs correctly
                player.setPositionAndUpdate(entry.getKey().posX, entry.getKey().posY, entry.getKey().posZ);
            }
            //Loop all players (this is an O(n^2) operation)
            for (EntityPlayer player : playerToHook.keySet())
            {
                if (player instanceof EntityPlayerMP)
                {
                    PacketHookSync packetHookSync = new PacketHookSync();
                    packetHookSync.playerHook = playerToHook.get(player);
                    for (Map.Entry<EntityPlayer, Hook> entry : playerToHook.entrySet())
                    {
                        if (entry.getKey() != player && getDistanceToHook(entry.getValue(), player) < 200)
                        {
                            packetHookSync.usernameToHookLocation.put(player.getCommandSenderName(), entry.getValue().toVec3());
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
    public static Vec3 getPullDirection(Hook hook, EntityPlayer player)
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

        return Vec3.createVectorHelper(xDifference, yDifference, zDifference);
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
