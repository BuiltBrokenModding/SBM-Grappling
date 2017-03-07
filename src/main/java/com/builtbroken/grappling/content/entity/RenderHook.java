package com.builtbroken.grappling.content.entity;

import com.builtbroken.grappling.content.Hook;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/4/2017.
 */
public class RenderHook extends RenderEntity
{
    public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(new ResourceLocation("smbgrapplinghook", "models/hook.obj"));
    public static final ResourceLocation TEXTURE = new ResourceLocation("smbgrapplinghook", "models/grapple.png");

    @Override
    public void doRender(Entity entity, double x, double y, double z, float p_76986_8_, float p_76986_9_)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);

        final float scale = 0.0625f / 3;
        GL11.glScalef(scale, scale, scale);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(getEntityTexture(entity));
        if (entity instanceof EntityHook)
        {
            final Hook hook = ((EntityHook) entity).hook;

            if (hook != null)
            {
                switch (hook.side)
                {
                    //Bottom
                    case 0:
                        GL11.glRotatef(-90, 1, 0, 0);
                        break;
                    //Top
                    case 1:
                        GL11.glRotatef(90, 1, 0, 0);
                        break;
                    //North
                    case 2:
                        //Default rotation
                        break;
                    //South
                    case 3:
                        GL11.glRotatef(180, 0, 1, 0);
                        break;
                    case 4:
                        GL11.glRotatef(90, 0, 1, 0);
                        break;
                    case 5:
                        GL11.glRotatef(-90, 0, 1, 0);
                        break;
                }
            }
        }
        MODEL.renderAll();

        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return TEXTURE;
    }
}
