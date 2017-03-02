package com.builtbroken.grappling.client;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
public class ItemHookRenderer implements IItemRenderer
{
    public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(new ResourceLocation("smbgrapplinghook", "models/grapple.obj"));
    public static final ResourceLocation TEXTURE = new ResourceLocation("smbgrapplinghook", "models/grapple.png");


    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return type != ItemRenderType.INVENTORY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return type != ItemRenderType.INVENTORY;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        GL11.glPushMatrix();

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

        if (type == ItemRenderType.EQUIPPED)
        {
            //TODO have rotation match aiming point
            GL11.glRotatef(-130, 0, 1, 0);
            //GL11.glRotatef(13, 1, 0, 0);
            GL11.glTranslatef(0.1f, 0.4f, -1.2f);

            final float scale = 0.0625f / 3;
            GL11.glScalef(scale, scale, scale);
        }
        else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
        {
            GL11.glRotatef(140, 0, 1, 0);
            GL11.glRotatef(-13, 1, 0, 0);
            GL11.glTranslatef(-0.2f,0.6f,-0.5f);

            final float scale = 0.0625f / 3;
            GL11.glScalef(scale, scale, scale);
        }
        else if (type == ItemRenderType.ENTITY)
        {
            GL11.glTranslatef(0f, 0, -0.9f);

            final float scale = 0.0625f / 3;
            GL11.glScalef(scale, scale, scale);
        }

        MODEL.renderAll();

        GL11.glPopMatrix();
    }
}
