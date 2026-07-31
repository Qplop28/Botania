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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.accessory.AccessoryExtractionContext;
import vazkii.botania.client.render.accessory.AccessoryRenderData;
import vazkii.botania.client.render.accessory.BlockAccessoryRenderData;
import vazkii.botania.client.render.accessory.DeferredAccessoryRenderer;
import vazkii.botania.common.proxy.Proxy;

public class PyroclastPendantItem extends BaubleItem {

	public PyroclastPendantItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.registerDeferred(this, new Renderer()));
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity living) {
		if (living.isOnFire()) {
			living.clearFire();
		}
	}

	public static class Renderer implements DeferredAccessoryRenderer {
		@Override
		public AccessoryRenderData createData() {
			return new BlockAccessoryRenderData();
		}

		@Override
		public void extract(AccessoryRenderData data, ItemStack stack, Player player, float partialTicks,
				AccessoryExtractionContext context) {
			BlockAccessoryRenderData blockData = (BlockAccessoryRenderData) data;
			blockData.chestArmor = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
			blockData.partsA = BlockAccessoryRenderData.collect(MiscellaneousModels.INSTANCE.pyroclastGem());
		}

		@Override
		public void submit(AccessoryRenderData data, ItemStack stack, PlayerModel model, AvatarRenderState state,
				PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			BlockAccessoryRenderData blockData = (BlockAccessoryRenderData) data;
			model.body.translateAndRotate(poseStack);
			poseStack.translate(-0.25, 0.5, blockData.chestArmor ? 0.05 : 0.12);
			poseStack.scale(0.5F, -0.5F, -0.5F);
			collector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), blockData.partsA,
					BlockModelRenderState.EMPTY_TINTS, lightCoords, OverlayTexture.NO_OVERLAY,
					state.outlineColor);
		}
	}
}
