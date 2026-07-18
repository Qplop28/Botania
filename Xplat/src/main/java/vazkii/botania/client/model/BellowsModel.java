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

import vazkii.botania.client.render.block_entity.state.BellowsRenderState;
import vazkii.botania.common.helper.VecHelper;

public class BellowsModel {
	private final ModelPart top;
	private final ModelPart base;
	private final ModelPart pipe;
	private final ModelPart funnel;

	public BellowsModel(ModelPart root) {
		top = root.getChild("top");
		base = root.getChild("base");
		pipe = root.getChild("pipe");
		funnel = root.getChild("funnel");
	}

	public static MeshDefinition createMesh() {
		var mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("top", CubeListBuilder.create()
				.addBox(-4.0F, -2.0F, -4.0F, 8, 1, 8),
				PartPose.offset(0.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 9)
				.addBox(-5.0F, 6.0F, -5.0F, 10, 2, 10),
				PartPose.offset(0.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("pipe", CubeListBuilder.create().texOffs(0, 21)
				.addBox(-1.0F, 6.0F, -8.0F, 2, 2, 3),
				PartPose.offset(0.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("funnel", CubeListBuilder.create().texOffs(40, 0)
				.addBox(0.0F, 0.0F, 0.0F, 6, 7, 6),
				PartPose.ZERO);
		return mesh;
	}

	public void submit(BellowsRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, Identifier texture) {
		var renderType = RenderTypes.entityCutout(texture);
		submitNodeCollector.submitModelPart(poseStack, base, renderType, state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, state.breakProgress);
		submitNodeCollector.submitModelPart(poseStack, pipe, renderType, state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, state.breakProgress);

		float movement = (1F - state.contractionFraction) * 0.5F;
		poseStack.pushPose();
		poseStack.translate(0F, movement, 0F);
		submitNodeCollector.submitModelPart(poseStack, top, renderType, state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, state.breakProgress);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.mulPose(VecHelper.rotateX(180F));
		poseStack.translate(-0.19F, -1.375F, -0.19F);
		poseStack.scale(1F, state.contractionFraction, 1F);
		submitNodeCollector.submitModelPart(poseStack, funnel, renderType, state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, state.breakProgress);
		poseStack.popPose();
	}
}
