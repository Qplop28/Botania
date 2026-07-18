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

import vazkii.botania.client.render.block_entity.state.TeruTeruBozuRenderState;

public class TeruTeruBozuModel {
	private final ModelPart thread;
	private final ModelPart cloth;
	private final ModelPart happyFace;
	private final ModelPart sadFace;

	public TeruTeruBozuModel(ModelPart root) {
		sadFace = root.getChild("sad_face");
		happyFace = root.getChild("happy_face");
		thread = root.getChild("thread");
		cloth = root.getChild("cloth");
	}

	public static MeshDefinition createMesh() {
		var mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("sad_face", CubeListBuilder.create().texOffs(32, 0)
				.addBox(-4.0F, -6.0F, -4.0F, 8, 8, 8),
				PartPose.offsetAndRotation(0.0F, 14.5F, 0.0F, 0.1745F, 0.0F, 0.0F));
		root.addOrReplaceChild("happy_face", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.0F, -6.0F, -4.0F, 8, 8, 8),
				PartPose.offsetAndRotation(0.0F, 14.5F, 0.0F, -0.1745F, 0.0F, 0.0F));
		root.addOrReplaceChild("thread", CubeListBuilder.create().texOffs(32, 16)
				.addBox(-3.0F, 2.0F, -3.0F, 6, 1, 6),
				PartPose.offset(0.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("cloth", CubeListBuilder.create().texOffs(0, 16)
				.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8),
				PartPose.offsetAndRotation(0.0F, 21.5F, -1.0F, 0.7854F, 2.2689F, 1.5708F));
		return mesh;
	}

	public void submit(TeruTeruBozuRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, Identifier texture) {
		var renderType = RenderTypes.entityCutoutNoCull(texture);
		submitNodeCollector.submitModelPart(poseStack, state.raining ? sadFace : happyFace, renderType,
				state.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, state.breakProgress);
		submitNodeCollector.submitModelPart(poseStack, thread, renderType,
				state.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, state.breakProgress);
		submitNodeCollector.submitModelPart(poseStack, cloth, renderType,
				state.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, state.breakProgress);
	}
}
