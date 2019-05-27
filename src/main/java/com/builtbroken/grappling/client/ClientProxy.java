package com.builtbroken.grappling.client;

import com.builtbroken.grappling.CommonProxy;
import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.client.fx.FxRope2;
import com.builtbroken.grappling.content.entity.EntityHook;
import com.builtbroken.grappling.content.entity.RenderHook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
@EventBusSubscriber(modid=GrapplingHookMod.MODID, value=Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	private static TextureAtlasSprite hookSprite;

	@Override
	public void setPlayerPosition(double x, double y, double z)
	{
		Minecraft.getMinecraft().player.prevPosX = x;
		Minecraft.getMinecraft().player.prevPosY = y;
		Minecraft.getMinecraft().player.prevPosZ = z;
		Minecraft.getMinecraft().player.height = 0.0F;
		Minecraft.getMinecraft().player.setPosition(x, y, z);
	}

	@SubscribeEvent
	public static void onTextureStitchPre(TextureStitchEvent.Pre event)
	{
		hookSprite = event.getMap().registerSprite(new ResourceLocation(GrapplingHookMod.MODID, "hook"));
	}

	@Override
	public TextureAtlasSprite getHookSprite()
	{
		return hookSprite;
	}

	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event)
	{
		OBJLoader.INSTANCE.addDomain(GrapplingHookMod.MODID);
		ModelLoader.setCustomModelResourceLocation(GrapplingHookMod.itemHook, 0, new ModelResourceLocation(GrapplingHookMod.PREFIX + "grapple.obj", "inventory"));
		RenderingRegistry.registerEntityRenderingHandler(EntityHook.class, manager -> new RenderHook(manager));
	}

	@Override
	public void renderRope(EntityHook entityHook)
	{
		Entity entity = entityHook.world.getEntityByID(entityHook.hook.playerEntityID);
		if (entity != null)
		{
			double x = entityHook.hook.side.getXOffset() * 0.4;
			double y = entityHook.hook.side.getYOffset() * 0.4;
			double z = entityHook.hook.side.getZOffset() * 0.4;

			FxRope2 rope = new FxRope2(entityHook.world,
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
