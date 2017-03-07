package com.builtbroken.grappling.client.fx;

import com.builtbroken.grappling.GrapplingHookMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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

    /** If true, debug data will be rendered near the rope */
    public static boolean renderDebugData = false;

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

        Vec3 delta = getDifferenceFromTarget();

        this.length = getDistanceFromTarget();

        this.rotYaw = ((float) (Math.atan2(delta.xCoord, delta.zCoord) * 180.0D / Math.PI));
        this.rotPitch = ((float) (Math.atan2(delta.yCoord, getDistanceFromTargetFlat()) * 180.0D / Math.PI));

        if (this.particleAge++ >= this.particleMaxAge)
        {
            setDead();
        }
    }

    public double getHeightOffset()
    {
        return 0.4; //TODO base off of target entity
    }

    public Vec3 getDifferenceFromTarget()
    {
        float xd = (float) (this.posX - target.posX);
        float yd = (float) (this.posY - target.posY + getHeightOffset());
        float zd = (float) (this.posZ - target.posZ);
        return Vec3.createVectorHelper(xd, yd, zd);
    }

    /**
     * Gets the exact distance from the render point to the target
     *
     * @return distance
     */
    public float getDistanceFromTarget()
    {
        final Vec3 delta = getDifferenceFromTarget();
        return MathHelper.sqrt_double(delta.xCoord * delta.xCoord + delta.yCoord * delta.yCoord + delta.zCoord * delta.zCoord);
    }

    /**
     * Gets the distance from the target in the x and z
     *
     * @return
     */
    public float getDistanceFromTargetFlat()
    {
        final Vec3 delta = getDifferenceFromTarget();
        return MathHelper.sqrt_double(delta.xCoord * delta.xCoord + delta.zCoord * delta.zCoord);
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

        HashMap<Vec3, Integer> debugData = null;
        try
        {
            //Call render iterator
            debugData = renderRope(tessellator, 0.04D);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        GL11.glPopMatrix();

        //Debug data for dev mode
        if (GrapplingHookMod.runningAsDev && debugData != null && renderDebugData)
        {
            for (Map.Entry<Vec3, Integer> entry : debugData.entrySet())
            {
                //EntitySmokeFX smoke = new EntitySmokeFX(world, entry.getKey().xCoord + 0.5, entry.getKey().yCoord + 0.5, entry.getKey().zCoord + 0.5, 0, 0, 0);
                //FMLClientHandler.instance().getClient().effectRenderer.addEffect(smoke);
                xx = (float) (entry.getKey().xCoord - interpPosX);
                yy = (float) (entry.getKey().yCoord - interpPosY);
                zz = (float) (entry.getKey().zCoord - interpPosZ);
                renderFloatingText("" + entry.getValue(), xx, yy, zz, Color.RED.getRGB());
            }
        }

        //Reset
        tessellator.startDrawingQuads();
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(PARTICLE_RESOURCE);
    }

    /**
     * Called to render the rope
     *
     * @param tessellator - tessellator to use
     * @param size        - size of the rope
     */
    public HashMap<Vec3, Integer> renderRope(Tessellator tessellator, double size)
    {
        final int maxLightValue = 200;
        final World world = Minecraft.getMinecraft().theWorld;

        //TODO make option to render solid (no segments) to improve FPS
        int segments = MathHelper.floor_double(this.length / size);

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

        HashMap<Vec3, Integer> debugData = new HashMap();

        //Loop per segment
        tessellator.startDrawingQuads();
        for (int i = 0; i < segments; i++)
        {
            //Get actual position in world
            final int pos_x = (int) Math.floor(posX + rx * i);
            final int pos_y = (int) Math.floor(posY + ry * i);
            final int pos_z = (int) Math.floor(posZ + rz * i);

            //Increase position offset
            position += size;

            //Set color
            tessellator.setColorOpaque_F(this.particleRed, this.particleGreen, this.particleBlue);

            //If world exists do light value calculations if we are in a new tile
            if (world != null && i == 0 || pos_x != prev_x || pos_y != prev_y || pos_z != prev_z)
            {
                //Set values
                brightness = baseBrightness;
                prev_x = pos_x;
                prev_y = pos_y;
                prev_z = pos_z;

                //Only change the value if we can not see the sky
                if (!world.canBlockSeeTheSky(pos_x, pos_y, pos_z))
                {
                    //Get light level based on position
                    //TODO add option to turn off to improve FPS
                    final Chunk chunk = world.getChunkFromBlockCoords(pos_x, pos_z);
                    if (chunk != null)
                    {
                        final ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[pos_y >> 4];
                        if (extendedblockstorage != null)
                        {
                            int skyLight = world.provider != null && world.provider.hasNoSky ? 0 : extendedblockstorage.getExtSkylightValue(pos_x, pos_y & 15, pos_z);
                            int blockLight = extendedblockstorage.getExtBlocklightValue(pos_x, pos_y & 15, pos_z);

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

                //Extra debug to help visually see data
                if (GrapplingHookMod.runningAsDev)
                {
                    debugData.put(Vec3.createVectorHelper(pos_x, pos_y, pos_z), brightness);

                }
            }

            //Render all 4 faces for this section
            renderFace(world, 0, position, 0, size, size, size, 2, brightness);
            renderFace(world, 0, position, 0, size, size, size, 3, brightness);
            renderFace(world, 0, position, 0, size, size, size, 4, brightness);
            renderFace(world, 0, position, 0, size, size, size, 5, brightness);

        }
        tessellator.draw();
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
    public void renderFace(World world, double x, double y, double z, double size_x, double size_y, double size_z, int side, int brightness)
    {
        //Constants
        final Tessellator tessellator = Tessellator.instance;

        //Calculate sizes
        final double minX = x - size_x;
        final double maxX = x + size_x;
        final double minY = y - size_y;
        final double maxY = y + size_y;
        final double minZ = z - size_z;
        final double maxZ = z + size_z;

        tessellator.setBrightness(brightness);

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

    public static void renderFloatingText(String text, double x, double y, double z, int color)
    {
        RenderManager renderManager = RenderManager.instance;
        FontRenderer fontRenderer = renderManager.getFontRenderer();
        float scale = 0.027f;
        GL11.glColor4f(1f, 1f, 1f, 0.5f);
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.instance;
        int yOffset = 0;

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        tessellator.startDrawingQuads();

        int stringMiddle = fontRenderer.getStringWidth(text) / 2;

        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.5F);
        tessellator.addVertex(-stringMiddle - 1, -1 + yOffset, 0.0D);
        tessellator.addVertex(-stringMiddle - 1, 8 + yOffset, 0.0D);
        tessellator.addVertex(stringMiddle + 1, 8 + yOffset, 0.0D);
        tessellator.addVertex(stringMiddle + 1, -1 + yOffset, 0.0D);

        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1f, 1f, 1f, 0.5f);
        fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, yOffset, color);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, yOffset, color);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}