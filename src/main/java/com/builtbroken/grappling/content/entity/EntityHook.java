package com.builtbroken.grappling.content.entity;

import com.builtbroken.grappling.content.Hook;
import com.builtbroken.grappling.content.MovementHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class EntityHook extends Entity
{
    public EntityPlayer owner;
    public Hook hook;

    public EntityHook(World world)
    {
        super(world);
        onEntityUpdate();
        setSize(0.3f, 0.3f);
    }

    @Override
    public void onUpdate()
    {
        if (!worldObj.isRemote)
        {
            if (owner == null || !MovementHandler.hasHook(owner))
            {
                setDead();
            }
            else
            {
                setPosition(hook.x, hook.y, hook.z);
            }
        }
    }

    @Override
    public void onEntityUpdate()
    {
        //Overriding base code
    }

    @Override
    protected void entityInit()
    {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {

    }
}
