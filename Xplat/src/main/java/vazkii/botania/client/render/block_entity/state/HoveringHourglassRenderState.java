/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class HoveringHourglassRenderState extends BlockEntityRenderState {
	public float animationTime;
	public float upperSandFraction;
	public float lowerSandFraction;
	public float flipRotationDegrees = 1F;
	public boolean flip;
	public int sandColor;
}
