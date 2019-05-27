package com.builtbroken.grappling.client.fx;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copy of the FXBeam from Voltz Engine modified to look like a rope
 */
@SideOnly(Side.CLIENT)
public class FxRope extends Particle
{
	public static final ResourceLocation PARTICLE_RESOURCE = new ResourceLocation("textures/particle/particles.png");
	public static final ResourceLocation TEXTURE = new ResourceLocation("smbgrapplinghook", "textures/rope.png");

	private float length = 0.0F;
	private float rotYaw = 0.0F;
	private float rotPitch = 0.0F;
	private float prevYaw = 0.0F;
	private float prevPitch = 0.0F;
	private double target_x, target_y, target_z;

	public FxRope(World par1World, double x, double y, double z, double target_x, double target_y, double target_z, int age)
	{
		super(par1World, x, y, z, 0.0D, 0.0D, 0.0D);

		this.setSize(0.02F, 0.02F);
		this.canCollide = true;
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.target_x = target_x;
		this.target_y = target_y;
		this.target_z = target_z;
		float xd = (float) (this.posX - target_x);
		float yd = (float) (this.posY - target_y);
		float zd = (float) (this.posZ - target_z);
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

		float xd = (float) (this.posX - target_x);
		float yd = (float) (this.posY - target_y);
		float zd = (float) (this.posZ - target_z);

		this.length = MathHelper.sqrt(xd * xd + yd * yd + zd * zd);

		double var7 = MathHelper.sqrt(xd * xd + zd * zd);

		this.rotYaw = ((float) (Math.atan2(xd, zd) * 180.0D / Math.PI));
		this.rotPitch = ((float) (Math.atan2(yd, var7) * 180.0D / Math.PI));

		if (this.particleAge++ >= this.particleMaxAge)
		{
			setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder b, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		GlStateManager.pushMatrix();

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

		//Calculate start position from player for rendering
		float xx = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
		float yy = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
		float zz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
		GlStateManager.translate(xx, yy, zz);

		//Calculate angles
		float ry = this.prevYaw + (this.rotYaw - this.prevYaw) * partialTicks;
		float rp = this.prevPitch + (this.rotPitch - this.prevPitch) * partialTicks;
		GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(180.0F + ry, 0.0F, 0.0F, -1.0F);
		GlStateManager.rotate(rp, 1.0F, 0.0F, 0.0F);

		b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		renderRope(b, 0.04D);
		Tessellator.getInstance().draw();

		GlStateManager.popMatrix();


		FMLClientHandler.instance().getClient().renderEngine.bindTexture(PARTICLE_RESOURCE);
	}

	public void renderRope(BufferBuilder b, double size)
	{
		int count = MathHelper.floor(this.length / size);

		double position = 0;
		for (int i = 0; i < count; i++)
		{
			position += size;
			b.color(this.particleRed, this.particleGreen, this.particleBlue, 255);
			//Bottom
			renderFace(b, 0, position, 0, size, size, size, 2);
			renderFace(b, 0, position, 0, size, size, size, 3);
			renderFace(b, 0, position, 0, size, size, size, 4);
			renderFace(b, 0, position, 0, size, size, size, 5);
		}
	}

	public void renderFace(BufferBuilder b, double x, double y, double z, double size_x, double size_y, double size_z, int side)
	{

		final double minX = x - size_x;
		final double maxX = x + size_x;
		final double minY = y - size_y;
		final double maxY = y + size_y;
		final double minZ = z - size_z;
		final double maxZ = z + size_z;

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
}