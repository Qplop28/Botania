/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity.state;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block_entity.RadiusDescriptor;

public sealed interface RadiusRenderData permits RadiusRenderData.Circle, RadiusRenderData.Rectangle {
	@Nullable
	static RadiusRenderData from(BlockPos tilePos, @Nullable RadiusDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		if (descriptor instanceof RadiusDescriptor.Circle circle) {
			return new Circle(
					circle.subtileCoords().getX() - tilePos.getX() + 0.5F,
					circle.subtileCoords().getY() - tilePos.getY(),
					circle.subtileCoords().getZ() - tilePos.getZ() + 0.5F,
					(float) circle.radius());
		} else if (descriptor instanceof RadiusDescriptor.Rectangle rectangle) {
			return new Rectangle(
					(float) (rectangle.aabb().minX - tilePos.getX()),
					(float) (rectangle.aabb().minY - tilePos.getY()),
					(float) (rectangle.aabb().minZ - tilePos.getZ()),
					(float) rectangle.aabb().getXsize(),
					(float) rectangle.aabb().getZsize());
		}
		return null;
	}

	record Circle(float offsetX, float offsetY, float offsetZ, float radius) implements RadiusRenderData {}

	record Rectangle(float offsetX, float offsetY, float offsetZ, float xSize, float zSize) implements RadiusRenderData {}
}
