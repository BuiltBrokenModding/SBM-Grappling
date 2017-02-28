package com.builtbroken.grappling;

import com.builtbroken.grappling.client.ClientHookHandler;
import com.builtbroken.grappling.client.ClientKeyHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

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
    }

    private void registerOnBus(Object object)
    {
        MinecraftForge.EVENT_BUS.register(object);
        FMLCommonHandler.instance().bus().register(object);
    }
}
