package com.builtbroken.grappling.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * Copy of the FXBeam from Voltz Engine modified to look like a rope
 */
@SideOnly(Side.CLIENT)
public class FxRope extends EntityFX
{
    public static final ResourceLocation PARTICLE_RESOURCE = new ResourceLocation("textures/particle/particles.png");
    public static final ResourceLocation TEXTURE = new ResourceLocation("smbgrapplinghook", "textures/rope.png");

    private float length = 0.0F;
    private float rotYaw = 0.0F;
    private float rotPitch = 0.0F;
    private float prevYaw = 0.0F;
    private float prevPitch = 0.0F;
    private double target_x, target_y, target_z;
    private float endModifier = 1.0F;
    public boolean reverse = false;
    public boolean pulse = false;
    private int rotationSpeed = 20;
    private float prevSize = 0.0F;

    public FxRope(World par1World, double x, double y, double z, double target_x, double target_y, double target_z, int age)
    {
        super(par1World, x, y, z, 0.0D, 0.0D, 0.0D);

        this.setSize(0.02F, 0.02F);
        this.noClip = true;
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.target_x = target_x;
        this.target_y = target_y;
        this.target_z = target_z;
        float xd = (float) (this.posX - target_x);
        float yd = (float) (this.posY - target_y);
        float zd = (float) (this.posZ - target_z);
        this.length = MathHelper.sqrt_float(xd * xd + yd * yd + zd * zd);
        double var7 = MathHelper.sqrt_double(xd * xd + zd * zd);
        this.rotYaw = ((float) (Math.atan2(xd, zd) * 180.0D / 3.141592653589793D));
        this.rotPitch = ((float) (Math.atan2(yd, var7) * 180.0D / 3.141592653589793D));
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;

        this.particleMaxAge = age;

        /** Sets the particle age based on distance. */
        EntityLivingBase renderentity = Minecraft.getMinecraft().renderViewEntity;

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

        this.length = MathHelper.sqrt_float(xd * xd + yd * yd + zd * zd);

        double var7 = MathHelper.sqrt_double(xd * xd + zd * zd);

        this.rotYaw = ((float) (Math.atan2(xd, zd) * 180.0D / 3.141592653589793D));
        this.rotPitch = ((float) (Math.atan2(yd, var7) * 180.0D / 3.141592653589793D));

        if (this.particleAge++ >= this.particleMaxAge)
        {
            setDead();
        }
    }

    public void setRGB(float r, float g, float b)
    {
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
    }

    @Override
    public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5)
    {
        tessellator.draw();
        GL11.glPushMatrix();

        float size = 1.0F;

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);


        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 1);
        GL11.glDepthMask(false);

        //Calculate start position from player for rendering
        float xx = (float) (this.prevPosX + (this.posX - this.prevPosX) * f - interpPosX);
        float yy = (float) (this.prevPosY + (this.posY - this.prevPosY) * f - interpPosY);
        float zz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * f - interpPosZ);
        GL11.glTranslated(xx, yy, zz);

        //Calculate angles
        float ry = this.prevYaw + (this.rotYaw - this.prevYaw) * f;
        float rp = this.prevPitch + (this.rotPitch - this.prevPitch) * f;
        GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(180.0F + ry, 0.0F, 0.0F, -1.0F);
        GL11.glRotatef(rp, 1.0F, 0.0F, 0.0F);

        renderRope(tessellator, size, 0);

        GL11.glPopMatrix();

        tessellator.startDrawingQuads();
        this.prevSize = size;

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(PARTICLE_RESOURCE);
    }

    public void renderRope(Tessellator tessellator, double size, float var12)
    {
        final double renderSize = 0.08D;

        double var44 = -renderSize * size;
        double var17 = renderSize * size;

        int count = MathHelper.floor_double(this.length / renderSize);

        double position = 0;

        for (int i = 0; i < count; i++)
        {
            if (i % 2 == 0)
            {
                double start = position;
                position += renderSize;

                //Bottom
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, -1.0F, 0.0F);
                tessellator.setColorOpaque_F(this.particleRed, this.particleGreen, this.particleBlue);
                tessellator.addVertexWithUV(var44, position, 0.0D, 1, 1);
                tessellator.addVertexWithUV(var44, start, 0.0D, 1, 0);
                tessellator.addVertexWithUV(var17, start, 0.0D, 0, 0);
                tessellator.addVertexWithUV(var17, position, 0.0D, 0, 1);
                tessellator.draw();

                //TOP
                tessellator.startDrawingQuads();

                tessellator.setColorOpaque_F(this.particleRed, this.particleGreen, this.particleBlue);
                tessellator.addVertexWithUV(var44, position, 0.0D, 0, 1);
                tessellator.addVertexWithUV(var44, start, 0.0D, 1, 0);
                tessellator.addVertexWithUV(var17, start, 0.0D, 0, 1);
                tessellator.addVertexWithUV(var17, position, 0.0D, 0, 1);
                tessellator.draw();
            }
        }
    }

    public void renderFace(double x, double y, double z, double width, double length, int side)
    {
        Tessellator tessellator = Tessellator.instance;

        double renderMinX = 0;
        double renderMaxX = 1;
        double renderMinZ = 0;
        double renderMaxZ = 1;

        double minX = x;
        double maxX = x;

        double minY = y;
        double maxY = y;

        double minZ = z;
        double maxZ = z;

        switch (side)
        {
            case 0:
                tessellator.setNormal(0.0F, -1.0F, 0.0F);
            case 1:
                tessellator.setNormal(0.0F, 1.0F, 0.0F);
            case 2:
            case 3:
            case 4:
            case 5:
        }

        tessellator.addVertexWithUV(minX, minY, maxZ, renderMinX, renderMaxZ);
        tessellator.addVertexWithUV(minX, minY, minZ, renderMinX, renderMinZ);
        tessellator.addVertexWithUV(maxX, minY, minZ, renderMaxX, renderMinZ);
        tessellator.addVertexWithUV(maxX, minY, maxZ, renderMaxX, renderMaxZ);
    }

    public void renderFaceYNeg(Block p_147768_1_, double p_147768_2_, double p_147768_4_, double p_147768_6_, double size_x, double size_y, double size_z)
    {
        Tessellator tessellator = Tessellator.instance;

        double d3 = 0;
        double d4 = 1;
        double d5 = 0;
        double d6 = 1;

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;

        double minX = p_147768_2_ - size_x;
        double maxX = p_147768_2_ + size_x;

        double minZ = p_147768_6_ - size_z;
        double maxZ = p_147768_6_ + size_z;

        double minY = p_147768_4_ - size_y;
        double maxY = p_147768_4_ + size_y;

        //Y Neg
        tessellator.addVertexWithUV(minX, minY, maxZ, d8, d10);
        tessellator.addVertexWithUV(minX, minY, minZ, d3, d5);
        tessellator.addVertexWithUV(maxX, minY, minZ, d7, d9);
        tessellator.addVertexWithUV(maxX, minY, maxZ, d4, d6);

        //Y pos
        tessellator.addVertexWithUV(minX, maxY, maxZ, d4, d6);
        tessellator.addVertexWithUV(minX, maxY, minZ, d7, d9);
        tessellator.addVertexWithUV(maxX, maxY, minZ, d3, d5);
        tessellator.addVertexWithUV(maxX, maxY, maxZ, d8, d10);

        //Z neg
        tessellator.addVertexWithUV(minX, maxY, minZ, d7, d9);
        tessellator.addVertexWithUV(maxX, maxY, minZ, d3, d5);
        tessellator.addVertexWithUV(maxX, minY, minZ, d8, d10);
        tessellator.addVertexWithUV(minX, minY, minZ, d4, d6);

        // Z pos
        tessellator.addVertexWithUV(minX, maxY, maxZ, d3, d5);
        tessellator.addVertexWithUV(minX, minY, maxZ, d8, d10);
        tessellator.addVertexWithUV(maxX, minY, maxZ, d4, d6);
        tessellator.addVertexWithUV(maxX, maxY, maxZ, d7, d9);

        //X pos
        tessellator.addVertexWithUV(maxX, minY, maxZ, d8, d10);
        tessellator.addVertexWithUV(maxX, minY, minZ, d4, d6);
        tessellator.addVertexWithUV(maxX, maxY, minZ, d7, d9);
        tessellator.addVertexWithUV(maxX, maxY, maxZ, d3, d5);

        //X neg
        tessellator.addVertexWithUV(minX, maxY, maxZ, d7, d9);
        tessellator.addVertexWithUV(minX, maxY, minZ, d3, d5);
        tessellator.addVertexWithUV(minX, minY, minZ, d8, d10);
        tessellator.addVertexWithUV(minX, minY, maxZ, d4, d6);

    }
}