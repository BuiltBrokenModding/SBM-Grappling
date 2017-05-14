package com.builtbroken.grappling.client;

import com.builtbroken.grappling.CommonProxy;
import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.client.ClientHookHandler;
import com.builtbroken.grappling.client.ClientKeyHandler;
import com.builtbroken.grappling.content.entity.EntityHookClient;
import com.builtbroken.grappling.content.entity.RenderHook;
import com.builtbroken.grappling.content.item.ItemHookRenderer;
import com.builtbroken.grappling.content.entity.EntityHook;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        super.init();
        EntityRegistry.registerModEntity(EntityHookClient.class, "smbEntityHook", 50, this, 100, 15, true);
    }

    @Override
    public void setPlayerPosition(double x, double y, double z)
    {
        Minecraft.getMinecraft().thePlayer.prevPosX = x;
        Minecraft.getMinecraft().thePlayer.prevPosY = y;
        Minecraft.getMinecraft().thePlayer.prevPosZ = z;
        Minecraft.getMinecraft().thePlayer.ySize = 0.0F;
        Minecraft.getMinecraft().thePlayer.setPosition(x, y, z);
    }

    @Override
    public void preInit()
    {
        registerOnBus(new ClientKeyHandler());
        registerOnBus(new ClientHookHandler());
        MinecraftForgeClient.registerItemRenderer(GrapplingHookMod.itemHook, new ItemHookRenderer());
        RenderingRegistry.registerEntityRenderingHandler(EntityHook.class, new RenderHook());
    }

    private void registerOnBus(Object object)
    {
        MinecraftForge.EVENT_BUS.register(object);
        FMLCommonHandler.instance().bus().register(object);
    }

    @Override
    protected void pullHook(EntityPlayer player, int movement)
    {
        super.pullHook(player, movement);
        if (ClientHookHandler.hook != null)
        {
            ClientHookHandler.hook.movement = movement;
        }
    }
}
