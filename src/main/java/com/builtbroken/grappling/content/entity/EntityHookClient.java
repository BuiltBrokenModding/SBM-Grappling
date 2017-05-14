package com.builtbroken.grappling.content.entity;

import com.builtbroken.grappling.client.fx.FxRope2;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class EntityHookClient extends EntityHook
{
    public EntityHookClient(World world)
    {
        super(world);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        Entity entity = worldObj.getEntityByID(hook.playerEntityID);
        if (entity != null)
        {
            ForgeDirection dir = ForgeDirection.getOrientation(hook.side);
            double x = dir.offsetX * 0.4;
            double y = dir.offsetY * 0.4;
            double z = dir.offsetZ * 0.4;

            FxRope2 rope = new FxRope2(worldObj,
                    hook.x + x, hook.y + y, hook.z + z,
                    entity, 1);
            FMLClientHandler.instance().getClient().effectRenderer.addEffect(rope);
        }
    }
}
