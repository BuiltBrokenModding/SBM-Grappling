package com.builtbroken.grappling.client;

import com.builtbroken.grappling.content.Hook;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;

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
        if (hook != null)
        {
            //MovementHandler.handleMotionLimits(Minecraft.getMinecraft().thePlayer, hook);
            event.player.motionX = 0;
            event.player.motionY = 0;
            event.player.motionZ = 0;
        }
    }
}
