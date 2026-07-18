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

import org.jetbrains.annotations.Nullable;

public class LuminizerRenderState extends BlockEntityRenderState {
	@Nullable
	public RadiusRenderData bindingRadius;
	public int radiusColor;
	public int spriteIndex;
	public float iconRotationDegrees;
}
