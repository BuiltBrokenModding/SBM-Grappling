package com.builtbroken.grappling.client;

import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.network.packets.PacketMouseClick;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.input.Keyboard;

/**
 * Handles overriding key bindings to control movement while using a hook
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
public class ClientKeyHandler
{
    @SubscribeEvent
    public void mouseHandler(MouseEvent e)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack != null)
        {
            final Item item = stack.getItem();
            if (item == GrapplingHookMod.itemHook)
            {
                if (e.button == 1 || e.button == 0 || player.isSneaking())
                {
                    GrapplingHookMod.packetHandler.sendToServer(new PacketMouseClick(player.inventory.currentItem, e.button, e.buttonstate, e.dwheel));
                    GrapplingHookMod.proxy.handleMouseInput(player, e.button, e.buttonstate, e.dwheel);
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void keyHandler(InputEvent.KeyInputEvent e)
    {
        int key = Keyboard.getEventKey();
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        player.addChatComponentMessage(new ChatComponentText("Key: " + key));
    }
}
