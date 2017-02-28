package com.builtbroken.grappling.content;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class EventHandler
{
    @SubscribeEvent
    public void changeDimEvent(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (MovementHandler.hasHook(event.player))
        {
            MovementHandler.clearHook(event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event)
    {
        if (event.entity instanceof EntityPlayer)
        {
            if (MovementHandler.hasHook((EntityPlayer) event.entity))
            {
                MovementHandler.clearHook((EntityPlayer) event.entity);
            }
        }
    }
}
