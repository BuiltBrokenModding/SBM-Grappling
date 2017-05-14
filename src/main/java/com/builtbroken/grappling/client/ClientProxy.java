package com.builtbroken.grappling.client;

import com.builtbroken.grappling.CommonProxy;
import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.client.fx.FxRope2;
import com.builtbroken.grappling.content.entity.EntityHook;
import com.builtbroken.grappling.content.entity.RenderHook;
import com.builtbroken.grappling.content.item.ItemHookRenderer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
public class ClientProxy extends CommonProxy
{
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
    public void renderRope(EntityHook entityHook)
    {
        Entity entity = entityHook.worldObj.getEntityByID(entityHook.hook.playerEntityID);
        if (entity != null)
        {
            ForgeDirection dir = ForgeDirection.getOrientation(entityHook.hook.side);
            double x = dir.offsetX * 0.4;
            double y = dir.offsetY * 0.4;
            double z = dir.offsetZ * 0.4;

            FxRope2 rope = new FxRope2(entityHook.worldObj,
                    entityHook.hook.x + x,
                    entityHook.hook.y + y,
                    entityHook.hook.z + z,
                    entity, 1);
            FMLClientHandler.instance().getClient().effectRenderer.addEffect(rope);
        }
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
