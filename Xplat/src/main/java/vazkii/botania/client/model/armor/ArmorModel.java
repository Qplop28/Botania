/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;

public class ArmorModel extends HumanoidModel<HumanoidRenderState> {
	protected final EquipmentSlot slot;

	public ArmorModel(ModelPart root, EquipmentSlot slot) {
		super(root);
		this.slot = slot;
	}

	// [VanillaCopy] ArmorStandArmorModel.setupAnim because armor stands are dumb
	// This fixes the armor "breathing" and helmets always facing south on armor stands
	@Override
	public void setupAnim(HumanoidRenderState state) {
		if (!(state instanceof ArmorStandRenderState armorStandState)) {
			super.setupAnim(state);
			return;
		}

		this.head.xRot = ((float) Math.PI / 180F) * armorStandState.headPose.x();
		this.head.yRot = ((float) Math.PI / 180F) * armorStandState.headPose.y();
		this.head.zRot = ((float) Math.PI / 180F) * armorStandState.headPose.z();
		this.head.setPos(0.0F, 1.0F, 0.0F);
		this.body.xRot = ((float) Math.PI / 180F) * armorStandState.bodyPose.x();
		this.body.yRot = ((float) Math.PI / 180F) * armorStandState.bodyPose.y();
		this.body.zRot = ((float) Math.PI / 180F) * armorStandState.bodyPose.z();
		this.leftArm.xRot = ((float) Math.PI / 180F) * armorStandState.leftArmPose.x();
		this.leftArm.yRot = ((float) Math.PI / 180F) * armorStandState.leftArmPose.y();
		this.leftArm.zRot = ((float) Math.PI / 180F) * armorStandState.leftArmPose.z();
		this.rightArm.xRot = ((float) Math.PI / 180F) * armorStandState.rightArmPose.x();
		this.rightArm.yRot = ((float) Math.PI / 180F) * armorStandState.rightArmPose.y();
		this.rightArm.zRot = ((float) Math.PI / 180F) * armorStandState.rightArmPose.z();
		this.leftLeg.xRot = ((float) Math.PI / 180F) * armorStandState.leftLegPose.x();
		this.leftLeg.yRot = ((float) Math.PI / 180F) * armorStandState.leftLegPose.y();
		this.leftLeg.zRot = ((float) Math.PI / 180F) * armorStandState.leftLegPose.z();
		this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
		this.rightLeg.xRot = ((float) Math.PI / 180F) * armorStandState.rightLegPose.x();
		this.rightLeg.yRot = ((float) Math.PI / 180F) * armorStandState.rightLegPose.y();
		this.rightLeg.zRot = ((float) Math.PI / 180F) * armorStandState.rightLegPose.z();
		this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
		this.hat.copyFrom(this.head);
	}

	public void prepareForRender() {
		setPartVisibility(slot);
	}

	// [VanillaCopy] HumanoidArmorLayer
	private void setPartVisibility(EquipmentSlot slot) {
		setAllVisible(false);
		switch (slot) {
			case HEAD -> {
				head.visible = true;
				hat.visible = true;
			}
			case CHEST -> {
				body.visible = true;
				rightArm.visible = true;
				leftArm.visible = true;
			}
			case LEGS -> {
				body.visible = true;
				rightLeg.visible = true;
				leftLeg.visible = true;
			}
			case FEET -> {
				rightLeg.visible = true;
				leftLeg.visible = true;
			}
		}
	}
}
