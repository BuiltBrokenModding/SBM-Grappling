package com.builtbroken.grappling.content.entity;

import org.lwjgl.opengl.GL11;

import com.builtbroken.grappling.GrapplingHookMod;
import com.builtbroken.grappling.content.Hook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/4/2017.
 */
public class RenderHook extends RenderEntity
{
	public RenderHook(RenderManager renderManager)
	{
		super(renderManager);
	}

	public static final ResourceLocation TEXTURE = new ResourceLocation(GrapplingHookMod.MODID, "textures/grapple.png");
	public IBakedModel model;

	public RenderHook()
	{
		super(Minecraft.getMinecraft().getRenderManager());

		try
		{
			IModel unbakedModel =  OBJLoader.INSTANCE.loadModel(new ResourceLocation(GrapplingHookMod.MODID, "models/hook.obj"));

			model = unbakedModel.bake(unbakedModel.getDefaultState(), DefaultVertexFormats.POSITION_TEX, rl -> GrapplingHookMod.proxy.getHookSprite());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float p_76986_8_, float p_76986_9_)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();

		final float scale = 0.0625f / 3;
		GlStateManager.scale(scale, scale, scale);

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(getEntityTexture(entity));
		if (entity instanceof EntityHook)
		{
			final Hook hook = ((EntityHook) entity).hook;

			if (hook != null)
			{
				switch (hook.side.ordinal())
				{
					//Bottom
					case 0:
						GlStateManager.rotate(-90, 1, 0, 0);
						break;
						//Top
					case 1:
						GlStateManager.rotate(90, 1, 0, 0);
						break;
						//North
					case 2:
						//Default rotation
						break;
						//South
					case 3:
						GlStateManager.rotate(180, 0, 1, 0);
						break;
					case 4:
						GlStateManager.rotate(90, 0, 1, 0);
						break;
					case 5:
						GlStateManager.rotate(-90, 0, 1, 0);
						break;
				}
			}
		}

		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();

		b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		for(BakedQuad quad : model.getQuads(null, null, 0))
		{
			b.addVertexData(quad.getVertexData());
		}

		t.draw();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return TEXTURE;
	}
}
