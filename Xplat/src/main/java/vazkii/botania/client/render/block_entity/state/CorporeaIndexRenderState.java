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

public class CorporeaIndexRenderState extends BlockEntityRenderState {
	public float rotation;
	public float translation;
	public boolean renderStars;
	public double starX;
	public double starZ;
	public int starSeed;
	public float starAnimationTicks;
}
