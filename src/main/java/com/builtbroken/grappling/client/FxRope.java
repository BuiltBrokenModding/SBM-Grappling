package com.builtbroken.grappling.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

        this.length = MathHelper.sqrt_float(xd * xd + yd * yd + zd * zd) + 1;

        double var7 = MathHelper.sqrt_double(xd * xd + zd * zd);

        this.rotYaw = ((float) (Math.atan2(xd, zd) * 180.0D / Math.PI));
        this.rotPitch = ((float) (Math.atan2(yd, var7) * 180.0D / Math.PI));

        if (this.particleAge++ >= this.particleMaxAge)
        {
            setDead();
        }
    }

    @Override
    public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5)
    {
        tessellator.draw();
        GL11.glPushMatrix();

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

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

        renderRope(tessellator, 0.04D);

        GL11.glPopMatrix();

        tessellator.startDrawingQuads();

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(PARTICLE_RESOURCE);
    }

    public void renderRope(Tessellator tessellator, double size)
    {
        int count = MathHelper.floor_double(this.length / size);

        double position = 0;
        tessellator.startDrawingQuads();
        for (int i = 0; i < count; i++)
        {
            if (i % 2 == 0)
            {
                position += size;
                tessellator.setColorOpaque_F(this.particleRed, this.particleGreen, this.particleBlue);
                //Bottom
                renderFace(0, position, 0, size, size, size, 2);
                renderFace(0, position, 0, size, size, size, 3);
                renderFace(0, position, 0, size, size, size, 4);
                renderFace(0, position, 0, size, size, size, 5);
            }
        }
        tessellator.draw();
    }

    public void renderFace(double x, double y, double z, double size_x, double size_y, double size_z, int side)
    {
        final Tessellator tessellator = Tessellator.instance;

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
            tessellator.addVertexWithUV(minX, minY, maxZ, 0, 1);
            tessellator.addVertexWithUV(minX, minY, minZ, 0, 0);
            tessellator.addVertexWithUV(maxX, minY, minZ, 1, 0);
            tessellator.addVertexWithUV(maxX, minY, maxZ, 1, 1);
        }
        //Up or top
        else if (side == 1)
        {
            //Y pos
            tessellator.addVertexWithUV(minX, maxY, maxZ,  1,  1);
            tessellator.addVertexWithUV(minX, maxY, minZ,  1,  0);
            tessellator.addVertexWithUV(maxX, maxY, minZ,  0,  0);
            tessellator.addVertexWithUV(maxX, maxY, maxZ,  0,  1);
        }
        //North
        else if (side == 2)
        {
            //Z neg
            tessellator.addVertexWithUV(minX, maxY, minZ,  1,  0);
            tessellator.addVertexWithUV(maxX, maxY, minZ,  0,  0);
            tessellator.addVertexWithUV(maxX, minY, minZ,  0,  1);
            tessellator.addVertexWithUV(minX, minY, minZ,  1,  1);
        }
        //South
        else if (side == 3)
        {
            // Z pos
            tessellator.addVertexWithUV(minX, maxY, maxZ,  0,  0);
            tessellator.addVertexWithUV(minX, minY, maxZ,  0,  1);
            tessellator.addVertexWithUV(maxX, minY, maxZ,  1,  1);
            tessellator.addVertexWithUV(maxX, maxY, maxZ,  1,  0);
        }

        //West
        else if (side == 4)
        {
            //X neg
            tessellator.addVertexWithUV(minX, maxY, maxZ,  1,  0);
            tessellator.addVertexWithUV(minX, maxY, minZ,  0,  0);
            tessellator.addVertexWithUV(minX, minY, minZ,  0,  1);
            tessellator.addVertexWithUV(minX, minY, maxZ,  1,  1);
        }
        //East
        else if (side == 5)
        {
            //X pos
            tessellator.addVertexWithUV(maxX, minY, maxZ,  0,  1);
            tessellator.addVertexWithUV(maxX, minY, minZ,  1,  1);
            tessellator.addVertexWithUV(maxX, maxY, minZ,  1,  0);
            tessellator.addVertexWithUV(maxX, maxY, maxZ,  0,  0);
        }
    }
}