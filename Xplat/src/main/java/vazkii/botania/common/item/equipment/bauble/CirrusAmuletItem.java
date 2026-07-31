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

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
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
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.proxy.Proxy;
import vazkii.botania.network.serverbound.JumpPacket;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class CirrusAmuletItem extends BaubleItem {

	private static final Set<Player> JUMPING_PLAYERS = Collections.newSetFromMap(new WeakHashMap<>());

	private static int timesJumped;
	private static boolean jumpDown;

	public CirrusAmuletItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.registerDeferred(this, new Renderer()));
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity living) {
		Proxy.INSTANCE.runOnClient(() -> () -> {
			if (living == Minecraft.getInstance().player) {
				LocalPlayer playerSp = (LocalPlayer) living;

				if (playerSp.onGround()) {
					timesJumped = 0;
				} else {
					if (timesJumped == 0) {
						// regardless how ground contact was lost, count that as first jump
						timesJumped = 1;
						jumpDown = true;
					}
					if (playerSp.input.keyPresses.jump()) {
						if (!jumpDown && timesJumped < ((CirrusAmuletItem) stack.getItem()).getMaxAllowedJumps()) {
							playerSp.jumpFromGround();
							ClientXplatAbstractions.INSTANCE.sendToServer(JumpPacket.INSTANCE);
							timesJumped++;
						}
						jumpDown = true;
					} else {
						jumpDown = false;
					}
				}
			}
		});
	}

	public static void setJumping(Player entity) {
		JUMPING_PLAYERS.add(entity);
	}

	public static boolean popJumping(Player entity) {
		if (entity.level().isClientSide()) {
			return timesJumped > 0;
		}
		return JUMPING_PLAYERS.remove(entity);
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
			blockData.partsA = BlockAccessoryRenderData.collect(stack.is(BotaniaItems.superCloudPendant)
					? MiscellaneousModels.INSTANCE.nimbusGem()
					: MiscellaneousModels.INSTANCE.cirrusGem());
		}

		@Override
		public void submit(AccessoryRenderData data, ItemStack stack, PlayerModel model, AvatarRenderState state,
				PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			BlockAccessoryRenderData blockData = (BlockAccessoryRenderData) data;
			model.body.translateAndRotate(poseStack);
			poseStack.translate(-0.3, 0.4, blockData.chestArmor ? 0.05 : 0.12);
			poseStack.scale(0.5F, -0.5F, -0.5F);
			collector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), blockData.partsA,
					BlockModelRenderState.EMPTY_TINTS, lightCoords, OverlayTexture.NO_OVERLAY,
					state.outlineColor);
		}
	}

	public int getMaxAllowedJumps() {
		return 2;
	}

}
