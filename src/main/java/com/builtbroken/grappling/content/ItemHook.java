package com.builtbroken.grappling.content;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
public class ItemHook extends Item
{
    public ItemHook()
    {
        setUnlocalizedName("smbgrapplinghook:hook");
        setTextureName("smbgrapplinghook:hook");
        setHasSubtypes(true);
        setMaxStackSize(1);

        setCreativeTab(CreativeTabs.tabTools);
    }
}
