package com.builtbroken.grappling.content.entity;

import com.builtbroken.grappling.content.Hook;
import com.builtbroken.grappling.content.MovementHandler;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class EntityHook extends Entity implements IEntityAdditionalSpawnData
{
    public EntityPlayer owner;
    public Hook hook;

    public EntityHook(World world)
    {
        super(world);
        this.isImmuneToFire = true;
        this.noClip = true;
        onEntityUpdate();
        setSize(0.3f, 0.3f);
    }

    @Override
    public void onUpdate()
    {
        if (hook != null)
        {
            setPosition(hook.x, hook.y, hook.z);
        }
        if (!worldObj.isRemote)
        {
            if (owner == null || !MovementHandler.hasHook(owner))
            {
                setDead();
            }
        }
        onEntityUpdate();
    }

    @Override
    public void onEntityUpdate()
    {
        this.prevDistanceWalkedModified = this.distanceWalkedModified;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
    }

    @Override
    public void moveEntity(double p_70091_1_, double p_70091_3_, double p_70091_5_)
    {

    }

    @Override
    public void moveFlying(float p_70060_1_, float p_70060_2_, float p_70060_3_)
    {

    }

    @Override
    public void applyEntityCollision(Entity p_70108_1_)
    {

    }

    @Override
    public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
    {
        //TODO allow attack to break hook
        return false;
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt p_70077_1_)
    {
        //TODO destroy hook
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

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        buffer.writeBoolean(hook != null);
        if (hook != null)
        {
            hook.write(buffer);
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData)
    {
        if (additionalData.readBoolean())
        {
            hook = Hook.read(additionalData);
        }
        else
        {
            hook = null;
        }
    }
}
