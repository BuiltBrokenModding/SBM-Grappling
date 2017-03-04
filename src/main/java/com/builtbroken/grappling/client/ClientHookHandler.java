package com.builtbroken.grappling.client;

import com.builtbroken.grappling.content.Hook;
import com.builtbroken.grappling.content.MovementHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class ClientHookHandler
{
    /** Client side instance of the player's grappling hook */
    public static Hook hook;

    public static void setHook(Hook hook)
    {
        ClientHookHandler.hook = hook;
        //Override movement controls to improve handling
        if (hook != null && !(Minecraft.getMinecraft().thePlayer.movementInput instanceof MovementInputOverride))
        {
            Minecraft.getMinecraft().thePlayer.movementInput = new MovementInputOverride(Minecraft.getMinecraft().thePlayer.movementInput);
        }
    }

    @SubscribeEvent
    public void playerTickEvent(TickEvent.PlayerTickEvent event)
    {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (hook != null && event.player == Minecraft.getMinecraft().thePlayer)
        {
            double distance = MovementHandler.getDistanceToHook(hook, player);
            double xDifference = hook.x - player.posX;
            double yDifference = hook.y - player.posY;
            double zDifference = hook.z - player.posZ;

            if(hook.movement != 0)
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
            if (!player.onGround && hook.movement == 0 && distance > 2)
            {
                pullTowardsLowestPoint(player, hook);
            }

            if (event.phase == TickEvent.Phase.END)
            {
                FxRope rope = new FxRope(event.player.worldObj,
                        player.posX,
                        player.posY - 0.8,
                        player.posZ,
                        hook.x, hook.y, hook.z, 1);
                FMLClientHandler.instance().getClient().effectRenderer.addEffect(rope);
            }
        }
    }

    public void pullTowardsLowestPoint(EntityPlayer player, Hook hook)
    {
        final Vec3 desiredPosition = Vec3.createVectorHelper(hook.x, hook.y - hook.distance, hook.z);

        //Get difference in positions
        double xDifference = desiredPosition.xCoord - player.posX;
        double yDifference = desiredPosition.yCoord - player.posY;
        double zDifference = desiredPosition.zCoord - player.posZ;

        //Get distance
        double distance = Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);

        //Normalize
        double xNormalized = xDifference / distance;
        double yNormalized = yDifference / distance;
        double zNormalized = zDifference / distance;

        //Create pull
        Vec3 pull = Vec3.createVectorHelper(xNormalized * 0.05, yNormalized * 0.05, zNormalized * 0.05);

        //Apply acceleration to player
        player.motionX += pull.xCoord;
        player.motionY += pull.yCoord;
        player.motionZ += pull.zCoord;
    }
}
