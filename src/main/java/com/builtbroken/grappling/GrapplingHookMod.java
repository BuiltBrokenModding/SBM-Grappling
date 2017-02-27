package com.builtbroken.grappling;

import com.builtbroken.grappling.client.ClientKeyHandler;
import com.builtbroken.grappling.content.ItemHook;
import com.builtbroken.grappling.content.MovementHandler;
import com.builtbroken.grappling.network.PacketManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main mod class
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
@cpw.mods.fml.common.Mod(modid = "smbgrapplinghook", name = "Grappling Hook", version = GrapplingHookMod.VERSION)
public class GrapplingHookMod
{
    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;

    public static ItemHook itemHook;

    /** Information output thing */
    public static final Logger logger = LogManager.getLogger("SBM-GrapplingHook");

    public static PacketManager packetHandler;

    /** How far can the hook be shot out to hit, -1 means max render */
    public static int HOOK_REACH_DISTANCE = 100;
    /** How long in ticks does the hook last before breaking, -1 means off */
    public static int HOOK_LIFE_TIME = -1;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new ClientKeyHandler());
        FMLCommonHandler.instance().bus().register(new MovementHandler());

        itemHook = new ItemHook();
        GameRegistry.registerItem(itemHook, "sbmGrapplingHook");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        packetHandler = new PacketManager("smbgrapplinghook");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        //TODO implement recipe
    }
}
