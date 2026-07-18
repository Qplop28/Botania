/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity.state;

import java.util.List;

public class RedStringRenderState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
	public List<Vertex> vertices = List.of();
	public int alpha;
	public float normalX;
	public float normalY;
	public float normalZ;

	public record Vertex(float x, float y, float z) {}
}
