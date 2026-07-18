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
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public class ManaPoolRenderState extends BlockEntityRenderState {
	public final BlockModelRenderState fabulousModel = new BlockModelRenderState();
	public boolean fabulousVisible;
	public int fabulousTint;
	public int insideUvStart;
	public int insideUvEnd;
	public float poolBottom;
	public float poolTop;
	public float manaLevel;
	public @Nullable ResourceLocation overlayTexture;
	public float overlayAlpha;
}
