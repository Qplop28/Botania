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
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;

import org.jetbrains.annotations.Nullable;

public class ManaEnchanterRenderState extends BlockEntityRenderState {
	public @Nullable ItemEntityRenderState itemEntity;
	public float overlayAlpha;
	public boolean animateOverlay;
	public float overlayAngleDegrees;
	public float overlayTranslation;
	public float overlayScale = 1F;
}
