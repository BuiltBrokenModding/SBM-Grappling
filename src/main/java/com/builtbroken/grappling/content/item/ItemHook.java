package com.builtbroken.grappling.content.item;

import java.util.List;

import com.builtbroken.grappling.GrapplingHookMod;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
public class ItemHook extends Item
{
	public ItemHook()
	{
		setTranslationKey(GrapplingHookMod.MODID + "hook");
		setHasSubtypes(true);
		setMaxStackSize(1);

		setCreativeTab(CreativeTabs.TOOLS);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag flagIn)
	{
		lines.add(getTranslation(getTranslationKey() + ".info.leftClick"));
		lines.add(getTranslation(getTranslationKey() + ".info.rightClick"));
		lines.add(getTranslation(getTranslationKey() + ".info.scrollWheel"));
		lines.add(getTranslation(getTranslationKey() + ".info.leftClick2"));
		lines.add(getTranslation(getTranslationKey() + ".info.leftClick3"));
	}

	@SideOnly(Side.CLIENT)
	private String getTranslation(String key)
	{
		String translation = I18n.format(key);
		if(translation != null && !translation.isEmpty())
		{
			return translation;
		}
		return key;
	}
}
