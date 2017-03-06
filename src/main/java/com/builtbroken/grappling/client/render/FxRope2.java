package com.builtbroken.grappling.client.render;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.lwjgl.opengl.GL11;

/**
 * Copy of the FXBeam from Voltz Engine modified to look like a rope
 */
@SideOnly(Side.CLIENT)
public class FxRope2 extends EntityFX
{
    public static final ResourceLocation PARTICLE_RESOURCE = new ResourceLocation("textures/particle/particles.png");
    public static final ResourceLocation TEXTURE = new ResourceLocation("smbgrapplinghook", "textures/rope.png");

    private float length = 0.0F;
    private float rotYaw = 0.0F;
    private float rotPitch = 0.0F;
    private float prevYaw = 0.0F;
    private float prevPitch = 0.0F;
    private Entity target;

    public FxRope2(World par1World, double x, double y, double z, Entity entity, int age)
    {
        super(par1World, x, y, z, 0.0D, 0.0D, 0.0D);

        this.setSize(0.02F, 0.02F);
        this.noClip = true;
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.target = entity;
        float xd = (float) (this.posX - target.posX);
        float yd = (float) (this.posY - target.posY + 0.4); //TODO add method to get height offset
        float zd = (float) (this.posZ - target.posZ);
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

        float xd = (float) (this.posX - target.posX);
        float yd = (float) (this.posY - target.posY + 0.4); //TODO add method to get height offset
        float zd = (float) (this.posZ - target.posZ);

        this.length = MathHelper.sqrt_float(xd * xd + yd * yd + zd * zd);

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
        //Set
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


        //Call render iterator
        renderRope(tessellator, 0.04D);


        //Reset
        GL11.glPopMatrix();
        tessellator.startDrawingQuads();
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(PARTICLE_RESOURCE);
    }

    /**
     * Called to render the rope
     *
     * @param tessellator - tessellator to use
     * @param size        - size of the rope
     */
    public void renderRope(Tessellator tessellator, double size)
    {
        //TODO make option to render solid (no segments) to improve FPS
        int count = MathHelper.floor_double(this.length / size);

        double position = 0;
        tessellator.startDrawingQuads();
        for (int i = 0; i < count; i++)
        {
            position += size;
            tessellator.setColorOpaque_F(this.particleRed, this.particleGreen, this.particleBlue);
            //Bottom
            renderFace(0, position, 0, size, size, size, 2);
            renderFace(0, position, 0, size, size, size, 3);
            renderFace(0, position, 0, size, size, size, 4);
            renderFace(0, position, 0, size, size, size, 5);
        }
        tessellator.draw();
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
    public void renderFace(double x, double y, double z, double size_x, double size_y, double size_z, int side)
    {
        final Tessellator tessellator = Tessellator.instance;

        final double minX = x - size_x;
        final double maxX = x + size_x;
        final double minY = y - size_y;
        final double maxY = y + size_y;
        final double minZ = z - size_z;
        final double maxZ = z + size_z;

        //Adjust brightness to world time //TODO adjust by light level at location
        int t = (int) Minecraft.getMinecraft().theWorld.getWorldInfo().getWorldTime();
        tessellator.setBrightness((int) (200 * (1 - (t / 24000.0))));

        double lx = posX + x;
        double ly = posY + y;
        double lz = posZ + z;

        //TODO add option to turn off to improve FPS
        Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromBlockCoords((int) Math.floor(lx), (int) Math.floor(lz));
        if (chunk != null)
        {
            ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[(int) Math.floor(ly) >> 4];
            int i1 = this.worldObj.provider.hasNoSky ? 0 : extendedblockstorage.getExtSkylightValue((int) Math.floor(lx), (int) Math.floor(ly) & 15, (int) Math.floor(lz));
            int j1 = extendedblockstorage.getExtBlocklightValue((int) Math.floor(lx), (int) Math.floor(ly) & 15, (int) Math.floor(lz));
            //System.out.println(i1 + "  " + j1);

            float p = Math.min(j1, i1) / 15.0f;
            tessellator.setBrightness(100 + (int) (100 * p));
        }

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
            tessellator.addVertexWithUV(minX, maxY, maxZ, 1, 1);
            tessellator.addVertexWithUV(minX, maxY, minZ, 1, 0);
            tessellator.addVertexWithUV(maxX, maxY, minZ, 0, 0);
            tessellator.addVertexWithUV(maxX, maxY, maxZ, 0, 1);
        }
        //North
        else if (side == 2)
        {
            //Z neg
            tessellator.addVertexWithUV(minX, maxY, minZ, 1, 0);
            tessellator.addVertexWithUV(maxX, maxY, minZ, 0, 0);
            tessellator.addVertexWithUV(maxX, minY, minZ, 0, 1);
            tessellator.addVertexWithUV(minX, minY, minZ, 1, 1);
        }
        //South
        else if (side == 3)
        {
            // Z pos
            tessellator.addVertexWithUV(minX, maxY, maxZ, 0, 0);
            tessellator.addVertexWithUV(minX, minY, maxZ, 0, 1);
            tessellator.addVertexWithUV(maxX, minY, maxZ, 1, 1);
            tessellator.addVertexWithUV(maxX, maxY, maxZ, 1, 0);
        }

        //West
        else if (side == 4)
        {
            //X neg
            tessellator.addVertexWithUV(minX, maxY, maxZ, 1, 0);
            tessellator.addVertexWithUV(minX, maxY, minZ, 0, 0);
            tessellator.addVertexWithUV(minX, minY, minZ, 0, 1);
            tessellator.addVertexWithUV(minX, minY, maxZ, 1, 1);
        }
        //East
        else if (side == 5)
        {
            //X pos
            tessellator.addVertexWithUV(maxX, minY, maxZ, 0, 1);
            tessellator.addVertexWithUV(maxX, minY, minZ, 1, 1);
            tessellator.addVertexWithUV(maxX, maxY, minZ, 1, 0);
            tessellator.addVertexWithUV(maxX, maxY, maxZ, 0, 0);
        }
    }
}