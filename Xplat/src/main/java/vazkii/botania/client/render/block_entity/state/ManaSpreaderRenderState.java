/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.List;

public class ManaSpreaderRenderState extends BlockEntityRenderState {
	public final BlockModelRenderState bodyModel = new BlockModelRenderState();
	public List<BlockStateModelPart> coreParts = List.of();
	public List<BlockStateModelPart> paddingParts = List.of();
	public List<BlockStateModelPart> scaffoldingParts = List.of();
	public final ItemStackRenderState lensItem = new ItemStackRenderState();
	public boolean lensVisible;
	public boolean paddingVisible;
	public boolean scaffoldingVisible;
	public float spreaderRotationX;
	public float spreaderRotationY;
	public double animationTime;
	public int modelTint;
}
