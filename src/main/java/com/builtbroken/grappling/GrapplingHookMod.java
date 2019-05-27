package com.builtbroken.grappling;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.builtbroken.grappling.content.entity.EntityHook;
import com.builtbroken.grappling.content.item.ItemHook;
import com.builtbroken.grappling.network.PacketManager;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * Main mod class
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
@Mod(modid = GrapplingHookMod.MODID, name = "Grappling Hook", version = GrapplingHookMod.VERSION)
@EventBusSubscriber
public class GrapplingHookMod
{
	public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

	public static final String MAJOR_VERSION = "@MAJOR@";
	public static final String MINOR_VERSION = "@MINOR@";
	public static final String REVISION_VERSION = "@REVIS@";
	public static final String BUILD_VERSION = "@BUILD@";
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;

	public static final String MODID = "sbmgrapplinghook";
	public static final String PREFIX = MODID + ":";

	@ObjectHolder(PREFIX + "grappling_hook")
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
		Configuration configuration = new Configuration(new File(event.getModConfigurationDirectory(), "bbm/Grappling_Hook.cfg"));
		configuration.load();
		HOOK_REACH_DISTANCE = configuration.getInt("reach_distance", Configuration.CATEGORY_GENERAL, HOOK_REACH_DISTANCE, 10, 300, "Max distance that the grappling hook can fire.");
		HOOK_LIFE_TIME = configuration.getInt("life_timer", Configuration.CATEGORY_GENERAL, HOOK_LIFE_TIME, -1, 90000, "Time in ticks that the hook can be activated for, 20 ticks = 1 second if server is running at 50ms a tick.");
		HOOK_PULL_PERCENT = configuration.getFloat("speed", Configuration.CATEGORY_GENERAL, HOOK_PULL_PERCENT, 0, 4, "Percentage of speed to modify the default hook speed, this is a min and max cap on the speed.");
		configuration.save();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().register(new ItemHook().setRegistryName(new ResourceLocation(MODID, "grappling_hook")));
	}

	@SubscribeEvent
	public static void registerEntities(RegistryEvent.Register<EntityEntry> event)
	{
		event.getRegistry().register(EntityEntryBuilder.create()
				.id(new ResourceLocation(MODID, "grappling_hook"), 0)
				.entity(EntityHook.class)
				.name("SBMGrapplingHook")
				.tracker(100, 15, true).build());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		packetHandler = new PacketManager("sbmgrapplinghook");
	}
}
