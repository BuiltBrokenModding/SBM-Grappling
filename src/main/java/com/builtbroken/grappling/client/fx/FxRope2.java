package com.builtbroken.grappling.client.fx;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.builtbroken.grappling.GrapplingHookMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copy of the FXBeam from Voltz Engine modified to look like a rope
 */
@SideOnly(Side.CLIENT)
public class FxRope2 extends Particle
{
	public static final ResourceLocation PARTICLE_RESOURCE = new ResourceLocation("textures/particle/particles.png");
	public static final ResourceLocation TEXTURE = new ResourceLocation("smbgrapplinghook", "textures/rope.png");

	private float length = 0.0F;
	private float rotYaw = 0.0F;
	private float rotPitch = 0.0F;
	private float prevYaw = 0.0F;
	private float prevPitch = 0.0F;
	private Entity target;

	/** If true, debug data will be rendered near the rope */
	public static boolean renderDebugData = false;

	public FxRope2(World par1World, double x, double y, double z, Entity entity, int age)
	{
		super(par1World, x, y, z, 0.0D, 0.0D, 0.0D);

		this.setSize(0.02F, 0.02F);
		this.canCollide = true;
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.target = entity;
		float xd = (float) (this.posX - target.posX);
		float yd = (float) (this.posY - target.posY + 0.4); //TODO add method to get height offset
		float zd = (float) (this.posZ - target.posZ);
		this.length = MathHelper.sqrt(xd * xd + yd * yd + zd * zd);
		double var7 = MathHelper.sqrt(xd * xd + zd * zd);
		this.rotYaw = ((float) (Math.atan2(xd, zd) * 180.0D / 3.141592653589793D));
		this.rotPitch = ((float) (Math.atan2(yd, var7) * 180.0D / 3.141592653589793D));
		this.prevYaw = this.rotYaw;
		this.prevPitch = this.rotPitch;

		this.particleMaxAge = age;

		/** Sets the particle age based on distance. */
		Entity renderentity = Minecraft.getMinecraft().getRenderViewEntity();

		int visibleDistance = 50;

		if (!Minecraft.getMinecraft().gameSettings.fancyGraphics)
		{
			visibleDistance = 25;
		}
		if (renderentity.getDistance(this.posX, this.posY, this.posZ) > visibleDistance)
		{
			this.particleMaxAge = 0;
		}
	}

	@Override
	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.prevYaw = this.rotYaw;
		this.prevPitch = this.rotPitch;

		Vec3d delta = getDifferenceFromTarget();

		this.length = getDistanceFromTarget();

		this.rotYaw = ((float) (Math.atan2(delta.x, delta.z) * 180.0D / Math.PI));
		this.rotPitch = ((float) (Math.atan2(delta.y, getDistanceFromTargetFlat()) * 180.0D / Math.PI));

		if (this.particleAge++ >= this.particleMaxAge)
		{
			setExpired();
		}
	}

	public double getHeightOffset()
	{
		return 0.4; //TODO base off of target entity
	}

	public Vec3d getDifferenceFromTarget()
	{
		float xd = (float) (this.posX - target.posX);
		float yd = (float) (this.posY - target.posY + getHeightOffset());
		float zd = (float) (this.posZ - target.posZ);
		return new Vec3d(xd, yd, zd);
	}

	/**
	 * Gets the exact distance from the render point to the target
	 *
	 * @return distance
	 */
	public float getDistanceFromTarget()
	{
		final Vec3d delta = getDifferenceFromTarget();
		return MathHelper.sqrt(delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
	}

	/**
	 * Gets the distance from the target in the x and z
	 *
	 * @return
	 */
	public float getDistanceFromTargetFlat()
	{
		final Vec3d delta = getDifferenceFromTarget();
		return MathHelper.sqrt(delta.x * delta.x + delta.z * delta.z);
	}

	@Override
	public void renderParticle(BufferBuilder b, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		//Render rop
		GlStateManager.pushMatrix();
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

		//Calculate start position from player for rendering
		float xx = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
		float yy = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
		float zz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
		GL11.glTranslated(xx, yy, zz);

		//Calculate angles
		float ry = this.prevYaw + (this.rotYaw - this.prevYaw) * partialTicks;
		float rp = this.prevPitch + (this.rotPitch - this.prevPitch) * partialTicks;
		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(180.0F + ry, 0.0F, 0.0F, -1.0F);
		GL11.glRotatef(rp, 1.0F, 0.0F, 0.0F);

		HashMap<Vec3d, Integer> debugData = null;
		//Call render iterator
		debugData = renderRope(b, 0.04D);
		Tessellator.getInstance().draw();

		GlStateManager.popMatrix();

		//Debug data for dev mode
		if (GrapplingHookMod.runningAsDev && debugData != null && renderDebugData)
		{
			for (Map.Entry<Vec3d, Integer> entry : debugData.entrySet())
			{
				//EntitySmokeFX smoke = new EntitySmokeFX(world, entry.getKey().xCoord + 0.5, entry.getKey().yCoord + 0.5, entry.getKey().zCoord + 0.5, 0, 0, 0);
				//FMLClientHandler.instance().getClient().effectRenderer.addEffect(smoke);
				xx = (float) (entry.getKey().x - interpPosX);
				yy = (float) (entry.getKey().y - interpPosY);
				zz = (float) (entry.getKey().z - interpPosZ);
				renderFloatingText(b, "" + entry.getValue(), xx, yy, zz, Color.RED.getRGB());
			}
		}

		//Reset
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(PARTICLE_RESOURCE);
	}

	/**
	 * Called to render the rope
	 *
	 * @param tessellator - tessellator to use
	 * @param size        - size of the rope
	 */
	public HashMap<Vec3d, Integer> renderRope(BufferBuilder b, double size)
	{
		final int maxLightValue = 200;
		final World world = Minecraft.getMinecraft().world;

		//TODO make option to render solid (no segments) to improve FPS
		int segments = MathHelper.floor(this.length / size);

		double position = 0;

		double rpitch = Math.toRadians(rotPitch);
		double ryaw = Math.toRadians(rotYaw);
		//Translation based on rotation, per segment
		double rx = -((Math.sin(ryaw) * Math.cos(rpitch)) * size);
		double ry = -Math.sin(rpitch) * size;
		double rz = -((Math.cos(ryaw) * Math.cos(rpitch)) * size);

		//Set base brightness based on time of day
		int timeOfDay = (int) world.getWorldInfo().getWorldTime();
		int baseBrightness = (int) (maxLightValue * (1 - (timeOfDay / 24000.0)));

		//Previous light value calculations
		int prev_x = 0, prev_y = 0, prev_z = 0;
		int brightness = baseBrightness;

		HashMap<Vec3d, Integer> debugData = new HashMap<>();

		//Loop per segment
		for (int i = 0; i < segments; i++)
		{
			//Get actual position in world
			final int pos_x = (int) Math.floor(posX + rx * i);
			final int pos_y = (int) Math.floor(posY + ry * i);
			final int pos_z = (int) Math.floor(posZ + rz * i);

			//Increase position offset
			position += size;

			//Set color
			b.color(this.particleRed, this.particleGreen, this.particleBlue, 255);

			//Try-catch to prevent edge cases that crash during lighting checks
			try
			{
				//If world exists do light value calculations if we are in a new tile
				if (world != null && i == 0 || pos_x != prev_x || pos_y != prev_y || pos_z != prev_z)
				{
					//Set values
					brightness = baseBrightness;
					prev_x = pos_x;
					prev_y = pos_y;
					prev_z = pos_z;

					//Only change the value if we can not see the sky
					if (!world.canBlockSeeSky(new BlockPos(pos_x, pos_y, pos_z)))
					{
						//Get light level based on position
						//TODO add option to turn off to improve FPS
						final Chunk chunk = world.getChunk(pos_x, pos_z);
						if (chunk != null)
						{
							final ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[pos_y >> 4];
							if (extendedblockstorage != null)
							{
								int cx = pos_x & 15;
								int cy = pos_y & 15;
								int cz = pos_z & 15;

								int skyLight = 0;
								if (world.provider == null || !world.provider.isNether())
								{
									skyLight = extendedblockstorage.getSkyLight(cx, cy, cz);
								}
								int blockLight = extendedblockstorage.getBlockLight(cx, cy, cz);

								float percentage = Math.min(blockLight, skyLight) / 15.0f;
								int baseLight = (int) (baseBrightness * 0.75);
								int value = (int) (baseLight + ((maxLightValue - baseLight) * percentage));
								brightness = (value + baseBrightness) / 2;
							}
						}
					}
					else
					{
						brightness = (brightness + baseBrightness) / 2;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			//Extra debug to help visually see data
			if (GrapplingHookMod.runningAsDev)
			{
				debugData.put(new Vec3d(pos_x, pos_y, pos_z), brightness);
			}


			//Render all 4 faces for this section
			renderFace(b, world, 0, position, 0, size, size, size, 2, brightness);
			renderFace(b, world, 0, position, 0, size, size, size, 3, brightness);
			renderFace(b, world, 0, position, 0, size, size, size, 4, brightness);
			renderFace(b, world, 0, position, 0, size, size, size, 5, brightness);

		}
		return debugData;
	}

	/**
	 * Called to render a standard minecraft block face
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param size_x
	 * @param size_y
	 * @param size_z
	 * @param side
	 */
	public void renderFace(BufferBuilder b, World world, double x, double y, double z, double size_x, double size_y, double size_z, int side, int brightness)
	{
		//Calculate sizes
		final double minX = x - size_x;
		final double maxX = x + size_x;
		final double minY = y - size_y;
		final double maxY = y + size_y;
		final double minZ = z - size_z;
		final double maxZ = z + size_z;

		b.putBrightness4(brightness, brightness, brightness, brightness);

		//Down or bottom
		if (side == 0)
		{
			//Y Neg
			b.pos(minX, minY, maxZ).tex(0, 1).endVertex();
			b.pos(minX, minY, minZ).tex(0, 0).endVertex();
			b.pos(maxX, minY, minZ).tex(1, 0).endVertex();
			b.pos(maxX, minY, maxZ).tex(1, 1).endVertex();
		}
		//Up or top
		else if (side == 1)
		{
			//Y pos
			b.pos(minX, maxY, maxZ).tex(1, 1).endVertex();
			b.pos(minX, maxY, minZ).tex(1, 0).endVertex();
			b.pos(maxX, maxY, minZ).tex(0, 0).endVertex();
			b.pos(maxX, maxY, maxZ).tex(0, 1).endVertex();
		}
		//North
		else if (side == 2)
		{
			//Z neg
			b.pos(minX, maxY, minZ).tex(1, 0).endVertex();
			b.pos(maxX, maxY, minZ).tex(0, 0).endVertex();
			b.pos(maxX, minY, minZ).tex(0, 1).endVertex();
			b.pos(minX, minY, minZ).tex(1, 1).endVertex();
		}
		//South
		else if (side == 3)
		{
			// Z pos
			b.pos(minX, maxY, maxZ).tex(0, 0).endVertex();
			b.pos(minX, minY, maxZ).tex(0, 1).endVertex();
			b.pos(maxX, minY, maxZ).tex(1, 1).endVertex();
			b.pos(maxX, maxY, maxZ).tex(1, 0).endVertex();
		}

		//West
		else if (side == 4)
		{
			//X neg
			b.pos(minX, maxY, maxZ).tex(1, 0).endVertex();
			b.pos(minX, maxY, minZ).tex(0, 0).endVertex();
			b.pos(minX, minY, minZ).tex(0, 1).endVertex();
			b.pos(minX, minY, maxZ).tex(1, 1).endVertex();
		}
		//East
		else if (side == 5)
		{
			//X pos
			b.pos(maxX, minY, maxZ).tex(0, 1).endVertex();
			b.pos(maxX, minY, minZ).tex(1, 1).endVertex();
			b.pos(maxX, maxY, minZ).tex(1, 0).endVertex();
			b.pos(maxX, maxY, maxZ).tex(0, 0).endVertex();
		}
	}

	public static void renderFloatingText(BufferBuilder b, String text, double x, double y, double z, int color)
	{
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		FontRenderer fontRenderer = renderManager.getFontRenderer();
		float scale = 0.027f;
		GlStateManager.color(1f, 1f, 1f, 0.5f);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-scale, -scale, scale);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.getInstance();
		int yOffset = 0;

		GlStateManager.disableTexture2D();

		b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		int stringMiddle = fontRenderer.getStringWidth(text) / 2;

		b.color(0.0F, 0.0F, 0.0F, 0.5F);
		b.pos(-stringMiddle - 1, -1 + yOffset, 0.0D).endVertex();
		b.pos(-stringMiddle - 1, 8 + yOffset, 0.0D).endVertex();
		b.pos(stringMiddle + 1, 8 + yOffset, 0.0D).endVertex();
		b.pos(stringMiddle + 1, -1 + yOffset, 0.0D).endVertex();

		tessellator.draw();
		GlStateManager.enableTexture2D();

		GlStateManager.color(1f, 1f, 1f, 0.5f);
		fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, yOffset, color);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, yOffset, color);

		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}
}