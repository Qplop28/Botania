/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.bauble;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.accessory.AccessoryExtractionContext;
import vazkii.botania.client.render.accessory.AccessoryRenderData;
import vazkii.botania.client.render.accessory.BlockAccessoryRenderData;
import vazkii.botania.client.render.accessory.DeferredAccessoryRenderer;
import vazkii.botania.common.entity.MagicMissileEntity;
import vazkii.botania.common.proxy.Proxy;

import java.util.List;

public class ThirdEyeItem extends BaubleItem {

	private static final int COST = 2;

	public ThirdEyeItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.registerDeferred(this, new Renderer()));
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity living) {
		if (!(living instanceof Player eplayer)) {
			return;
		}

		double range = 24;
		AABB aabb = new AABB(living.getX(), living.getY(), living.getZ(), living.getX(), living.getY(), living.getZ()).inflate(range);
		List<LivingEntity> mobs = living.level().getEntitiesOfClass(LivingEntity.class, aabb, MagicMissileEntity.targetPredicate(living));

		for (LivingEntity e : mobs) {
			MobEffectInstance potion = e.getEffect(MobEffects.GLOWING);
			if ((potion == null || potion.getDuration() <= 2) && ManaItemHandler.instance().requestManaExact(stack, eplayer, COST, true)) {
				e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 12, 0));
			}
		}
	}

	public static class Renderer implements DeferredAccessoryRenderer {

		public static final int NUM_LAYERS = 3;

		@Override
		public AccessoryRenderData createData() {
			return new BlockAccessoryRenderData();
		}

		@Override
		public void extract(AccessoryRenderData data, ItemStack stack, Player player, float partialTicks,
				AccessoryExtractionContext context) {
			BlockAccessoryRenderData blockData = (BlockAccessoryRenderData) data;
			blockData.chestArmor = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
			blockData.animationTime = ClientTickHandler.total() * 0.12;
			blockData.partsA = BlockAccessoryRenderData.collect(MiscellaneousModels.INSTANCE.thirdEyeLayer(0));
			blockData.partsB = BlockAccessoryRenderData.collect(MiscellaneousModels.INSTANCE.thirdEyeLayer(1));
			blockData.partsC = BlockAccessoryRenderData.collect(MiscellaneousModels.INSTANCE.thirdEyeLayer(2));
		}

		@Override
		public void submit(AccessoryRenderData data, ItemStack stack, PlayerModel model, AvatarRenderState state,
				PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			BlockAccessoryRenderData blockData = (BlockAccessoryRenderData) data;
			for (int i = 0; i < NUM_LAYERS; i++) {
				poseStack.pushPose();
				try {
					model.body.translateAndRotate(poseStack);
					switch (i) {
						case 0 -> {}
						case 1 -> {
							double dist = 0.05;
							poseStack.translate(Math.sin(blockData.animationTime) * dist,
									Math.cos(blockData.animationTime * 0.5) * dist, 0);
							poseStack.scale(0.75F, 0.75F, 1F);
							poseStack.translate(0, 0.1, -0.025);
						}
						case 2 -> poseStack.translate(0, 0, -0.05);
						default -> throw new IllegalStateException("Unexpected third-eye layer: " + i);
					}

					poseStack.translate(-0.3, 0.6, blockData.chestArmor ? 0.10 : 0.15);
					poseStack.scale(0.6F, -0.6F, -0.6F);
					var parts = switch (i) {
						case 0 -> blockData.partsA;
						case 1 -> blockData.partsB;
						case 2 -> blockData.partsC;
						default -> throw new IllegalStateException("Unexpected third-eye layer: " + i);
					};
					collector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), parts,
							BlockModelRenderState.EMPTY_TINTS, lightCoords, OverlayTexture.NO_OVERLAY,
							state.outlineColor);
				} finally {
					poseStack.popPose();
				}
			}
		}
	}

}
