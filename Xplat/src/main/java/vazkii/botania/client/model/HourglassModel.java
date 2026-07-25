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

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import org.joml.Vector3fc;

import vazkii.botania.client.render.block_entity.state.HoveringHourglassRenderState;
import vazkii.botania.common.helper.VecHelper;

import java.util.List;
import java.util.function.Consumer;

public class HourglassModel {
	private final ModelPart top;
	private final ModelPart glassT;
	private final ModelPart ring;
	private final ModelPart glassB;
	private final ModelPart bottom;
	private final ModelPart sandT;
	private final ModelPart sandB;

	public HourglassModel(ModelPart root) {
		top = root.getChild("top");
		glassT = root.getChild("glass_top");
		ring = root.getChild("ring");
		glassB = root.getChild("glass_bottom");
		bottom = root.getChild("bottom");
		sandT = root.getChild("sand_top");
		sandB = root.getChild("sand_bottom");
	}

	public static MeshDefinition createMesh() {
		var mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(20, 0)
				.addBox(-3.0F, -6.5F, -3.0F, 6, 1, 6), PartPose.ZERO);
		root.addOrReplaceChild("glass_top", CubeListBuilder.create()
				.addBox(-2.5F, -5.5F, -2.5F, 5, 5, 5), PartPose.ZERO);
		root.addOrReplaceChild("ring", CubeListBuilder.create().texOffs(0, 20)
				.addBox(-1.5F, -0.5F, -1.5F, 3, 1, 3), PartPose.ZERO);
		root.addOrReplaceChild("glass_bottom", CubeListBuilder.create().texOffs(0, 10)
				.addBox(-2.5F, 0.5F, -2.5F, 5, 5, 5), PartPose.ZERO);
		root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(20, 7)
				.addBox(-3.0F, 5.5F, -3.0F, 6, 1, 6), PartPose.ZERO);
		root.addOrReplaceChild("sand_top", CubeListBuilder.create().texOffs(20, 14)
				.addBox(0.0F, 0.0F, 0.0F, 4, 4, 4), PartPose.ZERO);
		root.addOrReplaceChild("sand_bottom", CubeListBuilder.create().texOffs(20, 14)
				.addBox(0.0F, 0.0F, 0.0F, 4, 4, 4), PartPose.ZERO);
		return mesh;
	}

	public void submit(HoveringHourglassRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, Identifier texture) {
		submit(state, poseStack, submitNodeCollector, texture, OverlayTexture.NO_OVERLAY, false, 0);
	}

	public void submit(HoveringHourglassRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, Identifier texture, int overlay,
			boolean hasFoil, int outlineColor) {
		float fract1 = state.upperSandFraction;
		float fract2 = state.lowerSandFraction;
		if (state.flip) {
			float tmp = fract1;
			fract1 = fract2;
			fract2 = tmp;
		}
		var renderType = RenderTypes.entityTranslucent(texture);
		int sandColor = 0xFF000000 | (state.sandColor & 0xFFFFFF);
		float f = 1F / 16F;

		submitWhite(state, poseStack, submitNodeCollector, texture, ring, overlay, hasFoil, outlineColor);
		submitWhite(state, poseStack, submitNodeCollector, texture, top, overlay, hasFoil, outlineColor);
		submitWhite(state, poseStack, submitNodeCollector, texture, bottom, overlay, hasFoil, outlineColor);

		if (fract1 > 0) {
			poseStack.pushPose();
			if (state.flip) {
				poseStack.translate(-2.0F * f, 1.0F * f, -2.0F * f);
			} else {
				poseStack.mulPose(VecHelper.rotateZ(180F));
				poseStack.translate(-2.0F * f, -5.0F * f, -2.0F * f);
			}
			poseStack.scale(1F, fract1, 1F);
			submitNodeCollector.submitModelPart(sandT, poseStack, renderType, state.lightCoords, overlay,
					null, false, hasFoil, sandColor, state.breakProgress, outlineColor);
			poseStack.popPose();
		}

		if (fract2 > 0) {
			poseStack.pushPose();
			if (state.flip) {
				poseStack.translate(-2.0F * f, -5.0F * f, -2.0F * f);
			} else {
				poseStack.mulPose(VecHelper.rotateZ(180F));
				poseStack.translate(-2.0F * f, 1.0F * f, -2.0F * f);
			}
			poseStack.scale(1F, fract2, 1F);
			submitNodeCollector.submitModelPart(sandB, poseStack, renderType, state.lightCoords, overlay,
					null, false, hasFoil, sandColor, state.breakProgress, outlineColor);
			poseStack.popPose();
		}

		submitWhite(state, poseStack, submitNodeCollector, texture, glassT, overlay, hasFoil, outlineColor);
		submitWhite(state, poseStack, submitNodeCollector, texture, glassB, overlay, hasFoil, outlineColor);
	}

	private static void submitWhite(HoveringHourglassRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, Identifier texture, ModelPart part,
			int overlay, boolean hasFoil, int outlineColor) {
		submitNodeCollector.submitModelPart(part, poseStack, RenderTypes.entityTranslucent(texture),
				state.lightCoords, overlay, null, false, hasFoil, -1, state.breakProgress, outlineColor);
	}

	public void collectItemExtents(PoseStack poseStack, Consumer<Vector3fc> output) {
		for (ModelPart part : List.of(top, glassT, ring, glassB, bottom, sandT, sandB)) {
			part.getExtentsForGui(poseStack, output);
		}
	}
}
