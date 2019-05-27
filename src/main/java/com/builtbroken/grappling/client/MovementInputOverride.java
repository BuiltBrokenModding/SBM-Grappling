package com.builtbroken.grappling.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;

/**
 * Used to override movement from the player client side
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2017.
 */
public class MovementInputOverride extends MovementInputFromOptions
{
	public MovementInput original;

	public MovementInputOverride(MovementInput original)
	{
		super(Minecraft.getMinecraft().gameSettings);
		this.original = original;
		if (this.original instanceof MovementInputOverride)
		{
			moveForward = this.original.moveForward;
			moveStrafe = this.original.moveStrafe;
			jump = this.original.jump;
			sneak = this.original.sneak;
		}
	}

	@Override
	public void updatePlayerMoveState()
	{
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (ClientHookHandler.hook == null)
		{
			original.updatePlayerMoveState();
			player.movementInput = original;
		}
		else if (player.onGround)
		{
			super.updatePlayerMoveState();
		}
		else
		{
			this.jump = Minecraft.getMinecraft().gameSettings.keyBindJump.isPressed();
			this.sneak = Minecraft.getMinecraft().gameSettings.keyBindSneak.isPressed();
			//TODO implement swing on the rope
		}
	}
}
