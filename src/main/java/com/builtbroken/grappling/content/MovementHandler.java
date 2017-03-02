package com.builtbroken.grappling.content;

import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.content.entity.EntityHook;
import com.builtbroken.grappling.network.packets.PacketHookSync;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            //Create hook entity
            Hook hook = new Hook();
            hook.side = movingobjectposition.sideHit;
            hook.x = movingobjectposition.hitVec.xCoord;
            hook.y = movingobjectposition.hitVec.yCoord;
            hook.z = movingobjectposition.hitVec.zCoord;
            hook.distance = getDistanceToHook(hook, player);
            playerToHook.put(player, hook);

            //Output that a hook has been created
            player.addChatComponentMessage(new ChatComponentText("Hook created"));

            //Generate hook render entity
            EntityHook entityHook = new EntityHook(player.worldObj);
            entityHook.owner = player;
            entityHook.hook = hook;
            entityHook.setPosition(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            player.worldObj.spawnEntityInWorld(entityHook);
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
        final float rotationHand = MathHelper.wrapAngleTo180_float(player.renderYawOffset + 90);
        final double r = Math.toRadians(rotationHand);
        final Vec3 hand = Vec3.createVectorHelper(
                (Math.cos(r) - Math.sin(r)) * 0.5,
                0,
                (Math.sin(r) + Math.cos(r)) * 0.5
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
    public void tickEvent(TickEvent.ServerTickEvent event)
    {
        //TODO check if player still has hook on hotbar
        if (event.phase == TickEvent.Phase.START)
        {
            List<EntityPlayer> removeList = new ArrayList();
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
                            Vec3 pull = getPullDirection(hook, player);
                            player.moveEntity(pull.xCoord * GrapplingHookMod.HOOK_PULL_PERCENT, pull.yCoord * GrapplingHookMod.HOOK_PULL_PERCENT, pull.zCoord * GrapplingHookMod.HOOK_PULL_PERCENT);
                            player.setPositionAndUpdate(entry.getKey().posX, entry.getKey().posY, entry.getKey().posZ);

                            //Update rope distance
                            hook.distance = getDistanceToHook(hook, player);
                        }
                    }
                    else if (entry.getValue().movement < 0)
                    {
                        hook.distance += 1.0 / 20.0;
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
            //Update motion limits
            for (Map.Entry<EntityPlayer, Hook> entry : playerToHook.entrySet())
            {
                if (entry.getKey() instanceof EntityPlayerMP)
                {
                    Hook hook = entry.getValue();
                    EntityPlayerMP player = (EntityPlayerMP) entry.getKey();
                    handleMotionLimits(player, hook);
                }
            }

            //Loop all players (this is an O(n^2) operation)
            for (Object object : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
            {
                if (object instanceof EntityPlayerMP)
                {
                    EntityPlayer player = (EntityPlayer) object;
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
     * Called to handle the movement limits
     *
     * @param player
     * @param hook
     */
    public static void handleMotionLimits(EntityPlayer player, Hook hook)
    {
        player.addChatComponentMessage(new ChatComponentText("Distance: " + hook.distance + "  Server: " + (player.worldObj instanceof WorldServer)));

        //Check to see if we are outside the max distance
        double distance = Math.max(getDistanceToHook(hook, player), 2);
        double delta = Math.abs(distance - hook.distance);
        float percent = (float) Math.abs(delta / hook.distance);
        if (percent > .95 && distance > 3)
        {
            double backupX = player.posX;
            double backupY = player.posY;
            double backupZ = player.posZ;
            try
            {
                //If we are outside the max reset position
                Vec3 pull = getPullDirection(hook, player);
                pull = Vec3.createVectorHelper(pull.xCoord * percent, pull.yCoord * percent, pull.zCoord * percent);
                player.moveEntity(pull.xCoord, pull.yCoord, pull.zCoord);
                player.setPositionAndUpdate(player.posX, player.posY, player.posZ);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                player.setPositionAndUpdate(backupX, backupY, backupZ);
            }
        }

        //Get distance from center point in each directon
        double xDifference = hook.x - player.posX;
        double yDifference = hook.y - player.posY;
        double zDifference = hook.z - player.posZ;

        //Get percentage distance
        double percentX = Math.abs(xDifference) / distance;
        double percentY = Math.abs(yDifference) / distance;
        double percentZ = Math.abs(zDifference) / distance;

        if (percentY > 0.95)
        {
            if (yDifference > 0)
            {
                //Turn off gravity
                if (player.motionY < 0.15)
                {
                    player.motionY = 0.15D;
                }
            }
            //Disable upward motion when at limit
            else if (player.motionY > 0)
            {
                //TODO redirect energy if its great than a pre set value
                player.motionY = 0;
            }
        }
        if (percentX > 0.95)
        {
            if (xDifference > 0)
            {
                player.addChatComponentMessage(new ChatComponentText("At x position limit"));
            }
            else
            {
                player.addChatComponentMessage(new ChatComponentText("At x negative limit"));
            }
        }

        if (percentZ > 0.95)
        {
            if (zDifference > 0)
            {
                player.addChatComponentMessage(new ChatComponentText("At z position limit"));
            }
            else
            {
                player.addChatComponentMessage(new ChatComponentText("At z negative limit"));
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
