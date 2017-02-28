package com.builtbroken.grappling;

import com.builtbroken.grappling.content.EventHandler;
import com.builtbroken.grappling.content.MovementHandler;
import com.builtbroken.grappling.content.entity.EntityHook;
import com.builtbroken.grappling.content.item.ItemHook;
import com.builtbroken.grappling.network.PacketManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
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

    @Mod.Instance("smbgrapplinghook")
    public static GrapplingHookMod INSTANCE;

    @SidedProxy(clientSide = "com.builtbroken.grappling.ClientProxy", serverSide = "com.builtbroken.grappling.CommonProxy")
    public static CommonProxy proxy;

    /** How far can the hook be shot out to hit, -1 means max render */
    public static int HOOK_REACH_DISTANCE = 100;
    /** How long in ticks does the hook last before breaking, -1 means off */
    public static int HOOK_LIFE_TIME = -1;
    /** Percentage of pull speed to use when changing motion, 0.0-1.0 where 0 = 0%, 1=100% */
    public static float HOOK_PULL_PERCENT = 1;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        registerOnBus(new EventHandler());
        registerOnBus(new MovementHandler());

        itemHook = new ItemHook();
        GameRegistry.registerItem(itemHook, "sbmGrapplingHook");

        proxy.preInit();
    }

    private void registerOnBus(Object object)
    {
        MinecraftForge.EVENT_BUS.register(object);
        FMLCommonHandler.instance().bus().register(object);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        packetHandler = new PacketManager("smbgrapplinghook");

        EntityRegistry.registerModEntity(EntityHook.class, "smbEntityHook", 50, this, 100, 15, true);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        //TODO implement recipe
    }
}
