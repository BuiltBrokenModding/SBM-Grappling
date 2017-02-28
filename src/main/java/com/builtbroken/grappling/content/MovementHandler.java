package com.builtbroken.grappling.content;

import com.builtbroken.grappling.GrapplingHookMod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
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
        public double x, y, z;
        public int side, movement;

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
            for (Map.Entry<EntityPlayer, Hook> entry : playerToHook.entrySet())
            {
                if (entry.getValue().movement > 0)
                {
                    //Get delta
                    double xDifference = entry.getValue().x - entry.getKey().posX;
                    double yDifference = entry.getValue().y - entry.getKey().posY;
                    double zDifference = entry.getValue().z - entry.getKey().posZ;
                    //Get mag
                    double mag = Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
                    //Normalize
                    xDifference = xDifference / mag;
                    yDifference = yDifference / mag;
                    zDifference = zDifference / mag;

                    //Update position
                    //entry.getKey().posX += xDifference;
                    //entry.getKey().posY += yDifference;
                    //entry.getKey().posZ += zDifference;
                    entry.getKey().moveEntity(xDifference, yDifference, zDifference);
                    entry.getKey().setPositionAndUpdate(entry.getKey().posX, entry.getKey().posY, entry.getKey().posZ);
                }
            }
            //TODO sync hooks to client
            //TODO ensure to sync hook locations that are near player only or player who owns the hook is near
        }
    }
}
