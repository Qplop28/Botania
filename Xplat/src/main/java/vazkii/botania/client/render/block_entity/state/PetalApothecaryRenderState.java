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
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.List;

public class PetalApothecaryRenderState extends BlockEntityRenderState {
	public List<ItemStackRenderState> petalItems = List.of();
	public double animationTicks;
	public boolean fluidVisible;
	public boolean lava;
	public int fluidColor;
	public float fluidAlpha;
	public int fluidLight;
}
