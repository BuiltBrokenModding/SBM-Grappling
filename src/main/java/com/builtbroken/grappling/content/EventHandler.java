package com.builtbroken.grappling.content;

import com.builtbroken.grappling.GrapplingHookMod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
@EventBusSubscriber(modid=GrapplingHookMod.MODID)
public class EventHandler
{
	@SubscribeEvent
	public static void changeDimEvent(PlayerChangedDimensionEvent event)
	{
		if (MovementHandler.hasHook(event.player))
		{
			MovementHandler.clearHook(event.player);
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event)
	{
		if (event.getEntity() instanceof EntityPlayer)
		{
			if (MovementHandler.hasHook((EntityPlayer) event.getEntity()))
			{
				MovementHandler.clearHook((EntityPlayer) event.getEntity());
			}
		}
	}
}
