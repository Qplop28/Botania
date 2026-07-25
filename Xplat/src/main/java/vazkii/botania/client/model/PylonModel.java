/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import org.joml.Vector3fc;

import java.util.function.Consumer;

public interface PylonModel {
	void renderRing(PoseStack ms, VertexConsumer buffer, int light, int overlay);

	void renderCrystal(PoseStack ms, VertexConsumer buffer, int light, int overlay);

	void submitRing(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType,
			int light, int overlay, boolean hasFoil, int outlineColor);

	void submitCrystal(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType,
			int light, int overlay, boolean hasFoil, int outlineColor);

	void collectExtents(PoseStack poseStack, Consumer<Vector3fc> output);
}
