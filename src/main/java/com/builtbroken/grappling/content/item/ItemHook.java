package com.builtbroken.grappling.content.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
public class ItemHook extends Item
{
    public ItemHook()
    {
        setUnlocalizedName("smbgrapplinghook:hook");
        setTextureName("smbgrapplinghook:grapple");
        setHasSubtypes(true);
        setMaxStackSize(1);

        setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean b)
    {
        lines.add(getTranslation(getUnlocalizedName() + ".info.leftClick"));
        lines.add(getTranslation(getUnlocalizedName() + ".info.rightClick"));
        lines.add(getTranslation(getUnlocalizedName() + ".info.scrollWheel"));
        lines.add(getTranslation(getUnlocalizedName() + ".info.leftClick2"));
        lines.add(getTranslation(getUnlocalizedName() + ".info.leftClick3"));
    }

    private String getTranslation(String key)
    {
        String translation = StatCollector.translateToLocal(key);
        if(translation != null && !translation.isEmpty())
        {
            return translation;
        }
        return key;
    }
}
