package com.builtbroken.grappling;

import com.builtbroken.grappling.content.MovementHandler;
import com.builtbroken.grappling.content.entity.EntityHook;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2017.
 */
public class CommonProxy
{
    /**
     * Called client side to update the position of the player
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPlayerPosition(double x, double y, double z)
    {

    }

    public void handleMouseInput(EntityPlayer player, int button, boolean state, int dwheel)
    {
        if (button == 0)
        {
            mouseLeftClick(player, state);
        }
        else if (button == 1)
        {
            mouseRightClick(player, state);
        }
        else
        {
            mouseScroll(player, dwheel);
        }
    }

    protected void mouseScroll(EntityPlayer player, int dwheel)
    {
        pullHook(player, dwheel);
    }

    protected void mouseLeftClick(EntityPlayer player, boolean state)
    {
        if (MovementHandler.hasHook(player))
        {
            if (state)
            {
                pullHook(player, 120);
            }
            else if (!player.worldObj.isRemote)
            {
                pullHook(player, 0);
            }
        }
        else if (!player.worldObj.isRemote && !state)
        {
            MovementHandler.createHook(player);
        }
    }

    protected void pullHook(EntityPlayer player, int movement)
    {
        if (!player.worldObj.isRemote)
        {
            MovementHandler.pullHook(player, movement);
        }
    }

    protected void mouseRightClick(EntityPlayer player, boolean state)
    {
        if (!player.worldObj.isRemote)
        {
            if (MovementHandler.hasHook(player) && !state)
            {
                MovementHandler.clearHook(player);
            }
        }
    }

    public void renderRope(EntityHook entityHook)
    {

    }

    public void preInit()
    {

    }

    public void init()
    {

    }

    public void postInit()
    {

    }
}
