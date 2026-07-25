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

import vazkii.botania.client.render.block_entity.state.BotanicalBreweryRenderState;
import vazkii.botania.common.helper.VecHelper;

public class BotanicalBreweryModel {
	final ModelPart top;
	final ModelPart pole;
	final ModelPart bottom;
	final ModelPart plate;

	public BotanicalBreweryModel(ModelPart root) {
		top = root.getChild("top");
		pole = root.getChild("pole");
		bottom = root.getChild("bottom");
		plate = root.getChild("plate");
	}

	public static MeshDefinition createMesh() {
		var mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(8, 0)
				.addBox(-2.0F, -7.0F, -2.0F, 4, 1, 4),
				PartPose.offset(0.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("pole", CubeListBuilder.create()
				.addBox(-1.0F, -6.0F, -1.0F, 2, 10, 2),
				PartPose.offset(0.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(8, 5)
				.addBox(-2.0F, 4.0F, -2.0F, 4, 1, 4),
				PartPose.offset(0.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(8, 5)
				.addBox(5.0F, 0.0F, -2.0F, 4, 1, 4),
				PartPose.offset(0.0F, 17.0F, 0.0F));
		return mesh;
	}

	public void submit(BotanicalBreweryRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, Identifier texture) {
		submit(state, poseStack, submitNodeCollector, texture, OverlayTexture.NO_OVERLAY, 0);
	}

	public void submit(BotanicalBreweryRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, Identifier texture, int overlay, int outlineColor) {
		float offset = (float) Math.sin(state.animationTime / 40) * 0.1F + 0.05F;
		float degrees = (float) state.animationTime / 16F;
		float poleRotation = -degrees * 25F;
		var renderType = RenderTypes.entitySolid(texture);

		poseStack.translate(0F, offset, 0F);
		poseStack.mulPose(VecHelper.rotateY(poleRotation));
		if (!state.items.isEmpty() && !state.items.get(0).isEmpty()) {
			poseStack.pushPose();
			poseStack.mulPose(VecHelper.rotateX(180));
			poseStack.translate(0, -0.45F, 0);
			poseStack.scale(0.25F, 0.25F, 0.25F);
			state.items.get(0).submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}

		submitNodeCollector.submitModelPart(poseStack, pole, renderType, state.lightCoords,
				overlay, 0xFFFFFFFF, outlineColor, state.breakProgress);
		submitNodeCollector.submitModelPart(poseStack, top, renderType, state.lightCoords,
				overlay, 0xFFFFFFFF, outlineColor, state.breakProgress);
		submitNodeCollector.submitModelPart(poseStack, bottom, renderType, state.lightCoords,
				overlay, 0xFFFFFFFF, outlineColor, state.breakProgress);
		poseStack.mulPose(VecHelper.rotateY(-poleRotation));

		if (state.plateCount > 0) {
			float degreesPerPlate = (float) (2F * Math.PI) / state.plateCount;
			for (int index = 0; index < state.plateCount; index++) {
				float plateRotation = degrees + degreesPerPlate * index;
				float plateOffset = (float) Math.sin(state.animationTime / 20 + index * 40F) * 0.2F - 0.2F;
				poseStack.pushPose();
				poseStack.translate(0F, plateOffset, 0F);
				if (index + 1 < state.items.size() && !state.items.get(index + 1).isEmpty()) {
					float rot = plateRotation * 180F / (float) Math.PI;
					poseStack.pushPose();
					poseStack.mulPose(VecHelper.rotateY(rot));
					poseStack.translate(0.3125F, 1.06F, 0.1245F);
					poseStack.mulPose(VecHelper.rotateX(-90F));
					poseStack.translate(0.125F, 0.125F, 0);
					poseStack.scale(0.25F, 0.25F, 0.25F);
					state.items.get(index + 1).submit(poseStack, submitNodeCollector,
							state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
					poseStack.popPose();
				}
				poseStack.mulPose(VecHelper.rotateY(plateRotation * 180F / (float) Math.PI));
				submitNodeCollector.submitModelPart(poseStack, plate, renderType, state.lightCoords,
						overlay, 0xFFFFFFFF, outlineColor, state.breakProgress);
				poseStack.popPose();
			}
		}
		poseStack.translate(0F, -offset, 0F);
	}
}
