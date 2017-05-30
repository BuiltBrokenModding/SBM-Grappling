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
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Main mod class
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
@cpw.mods.fml.common.Mod(modid = "sbmgrapplinghook", name = "Grappling Hook", version = GrapplingHookMod.VERSION)
public class GrapplingHookMod
{
    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;

    public static ItemHook itemHook;

    /** Information output thing */
    public static final Logger logger = LogManager.getLogger("SBM-GrapplingHook");

    public static PacketManager packetHandler;

    @Mod.Instance("sbmgrapplinghook")
    public static GrapplingHookMod INSTANCE;

    @SidedProxy(clientSide = "com.builtbroken.grappling.client.ClientProxy", serverSide = "com.builtbroken.grappling.ServerProxy")
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

        Configuration configuration = new Configuration(new File(event.getModConfigurationDirectory(), "bbm/Grappling_Hook.cfg"));
        configuration.load();
        HOOK_REACH_DISTANCE = configuration.getInt("reach_distance", Configuration.CATEGORY_GENERAL, HOOK_REACH_DISTANCE, 10, 300, "Max distance that the grappling hook can fire.");
        HOOK_LIFE_TIME = configuration.getInt("life_timer", Configuration.CATEGORY_GENERAL, HOOK_LIFE_TIME, -1, 90000, "Time in ticks that the hook can be activated for, 20 ticks = 1 second if server is running at 50ms a tick.");
        HOOK_PULL_PERCENT = configuration.getFloat("speed", Configuration.CATEGORY_GENERAL, HOOK_PULL_PERCENT, 0, 4, "Percentage of speed to modify the default hook speed, this is a min and max cap on the speed.");
        configuration.save();
    }

    private void registerOnBus(Object object)
    {
        MinecraftForge.EVENT_BUS.register(object);
        FMLCommonHandler.instance().bus().register(object);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        EntityRegistry.registerModEntity(EntityHook.class, "sbmEntityHook", 50, GrapplingHookMod.INSTANCE, 100, 15, true);
        proxy.init();
        packetHandler = new PacketManager("sbmgrapplinghook");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit();
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemHook, 1, 0), " B ", "ISI", "IRI", 'B', Blocks.iron_bars, 'I', Items.iron_ingot, 'S', Items.string, 'R', Items.redstone));
    }

    @Mod.EventHandler
    public void missingMappingEvent(FMLMissingMappingsEvent event)
    {
        for (FMLMissingMappingsEvent.MissingMapping missingMapping : event.getAll())
        {
            final String name = missingMapping.name;
            final String key = "sbmgrapplinghook:sbmGrapplingHook";
            if (name.equals("smbgrapplinghook:sbmGrapplingHook")) //Fixes mod ID switch
            {
                logger.info("Fixing missing mapping for '" + name + "' replacing with '" + key + "'");
                Object object = missingMapping.type.getRegistry().getObject(key);
                if (object != Blocks.air && object != null)
                {
                    missingMapping.remap((Item) object);
                }
            }
        }
    }
}
