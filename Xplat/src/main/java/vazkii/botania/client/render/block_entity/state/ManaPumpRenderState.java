/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity.state;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import java.util.List;

public class ManaPumpRenderState extends BlockEntityRenderState {
	public float rotationDegrees;
	public float headTranslation;
	public List<BlockStateModelPart> headModelParts = List.of();
}
