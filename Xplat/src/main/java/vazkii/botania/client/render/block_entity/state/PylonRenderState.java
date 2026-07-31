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

import vazkii.botania.common.block.PylonBlock;

public class PylonRenderState extends BlockEntityRenderState {
	public PylonBlock.Variant variant = PylonBlock.Variant.MANA;
	public float ringRotation;
	public float crystalRotation;
	public float ringBob;
	public float crystalBob;
}
