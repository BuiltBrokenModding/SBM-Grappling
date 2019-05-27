package com.builtbroken.grappling.client;

import org.lwjgl.input.Keyboard;

import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.client.fx.FxRope2;
import com.builtbroken.grappling.network.packets.PacketMouseClick;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Handles overriding key bindings to control movement while using a hook
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */

@EventBusSubscriber(modid=GrapplingHookMod.MODID, value=Side.CLIENT)
public class ClientKeyHandler
{
	static long lastDebugKeyHit = 0;

	@SubscribeEvent
	public static void mouseHandler(MouseEvent e)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;
		ItemStack stack = player.getActiveItemStack();
		if (!stack.isEmpty())
		{
			final Item item = stack.getItem();
			if (item == GrapplingHookMod.itemHook)
			{
				if (e.getButton() == 1 || e.getButton() == 0 || player.isSneaking())
				{
					GrapplingHookMod.packetHandler.sendToServer(new PacketMouseClick(player.inventory.currentItem, e.getButton(), e.isButtonstate(), e.getDwheel()));
					GrapplingHookMod.proxy.handleMouseInput(player, e.getButton(), e.isButtonstate(), e.getDwheel());
					e.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void keyHandler(InputEvent.KeyInputEvent e)
	{
		final int key = Keyboard.getEventKey();
		final long time = System.currentTimeMillis();
		if (ClientHookHandler.hook != null && key == Keyboard.KEY_GRAVE && (time - lastDebugKeyHit) > 1000)
		{
			lastDebugKeyHit = time;
			FxRope2.renderDebugData = !FxRope2.renderDebugData;
		}
	}
}
