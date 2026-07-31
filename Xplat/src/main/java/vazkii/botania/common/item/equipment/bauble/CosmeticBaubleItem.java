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

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import vazkii.botania.api.item.CosmeticBauble;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.accessory.AccessoryExtractionContext;
import vazkii.botania.client.render.accessory.AccessoryRenderData;
import vazkii.botania.client.render.accessory.DeferredAccessoryRenderer;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.proxy.Proxy;

import java.util.function.Consumer;

public class CosmeticBaubleItem extends BaubleItem implements CosmeticBauble {

	public enum Variant {
		BLACK_BOWTIE, BLACK_TIE, RED_GLASSES(true), PUFFY_SCARF,
		ENGINEER_GOGGLES(true), EYEPATCH(true), WICKED_EYEPATCH(true), RED_RIBBONS(true),
		PINK_FLOWER_BUD(true), POLKA_DOTTED_BOWS(true), BLUE_BUTTERFLY(true), CAT_EARS(true),
		WITCH_PIN, DEVIL_TAIL, KAMUI_EYE, GOOGLY_EYES(true),
		FOUR_LEAF_CLOVER, CLOCK_EYE(true), UNICORN_HORN(true), DEVIL_HORNS(true),
		HYPER_PLUS(true), BOTANIST_EMBLEM, ANCIENT_MASK(true), EERIE_MASK(true),
		ALIEN_ANTENNA(true), ANAGLYPH_GLASSES(true), ORANGE_SHADES(true), GROUCHO_GLASSES(true),
		THICK_EYEBROWS(true), LUSITANIC_SHIELD, TINY_POTATO_MASK(true), QUESTGIVER_MARK(true),
		THINKING_HAND(true);

		private final boolean isHead;

		Variant(boolean isHead) {
			this.isHead = isHead;
		}

		Variant() {
			this(false);
		}
	}

	private final Variant variant;

	public CosmeticBaubleItem(Variant variant, Properties props) {
		super(props);
		this.variant = variant;
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.registerDeferred(this, new Renderer()));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flags) {
		if (variant == Variant.THINKING_HAND) {
			tooltip.accept(Component.translatable("botaniamisc.cosmeticThinking").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		} else {
			tooltip.accept(Component.translatable("botaniamisc.cosmeticBauble").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
		super.appendHoverText(stack, context, display, tooltip, flags);
	}

	public static class Renderer implements DeferredAccessoryRenderer {
		@Override
		public void extract(AccessoryRenderData data, ItemStack stack, Player player, float partialTicks,
				AccessoryExtractionContext context) {
			data.itemState.clear();
			context.itemModels().updateForLiving(data.itemState, stack, ItemDisplayContext.NONE, player);
		}

		@Override
		public void submit(AccessoryRenderData data, ItemStack stack, PlayerModel model, AvatarRenderState state,
				PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			Variant variant = ((CosmeticBaubleItem) stack.getItem()).variant;
			if (variant.isHead) {
				model.head.translateAndRotate(poseStack);
				switch (variant) {
					case RED_GLASSES, ENGINEER_GOGGLES, ANAGLYPH_GLASSES -> {
						poseStack.translate(0, -0.225, -0.3);
						poseStack.scale(0.7F, -0.7F, -0.7F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case EYEPATCH -> {
						poseStack.translate(0.125, -0.225, -0.3);
						poseStack.mulPose(VecHelper.rotateY(180F));
						poseStack.scale(0.3F, -0.3F, -0.3F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case WICKED_EYEPATCH -> {
						poseStack.translate(-0.125, -0.225, -0.3);
						poseStack.scale(0.3F, -0.3F, -0.3F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case RED_RIBBONS -> {
						poseStack.translate(0, -0.65, 0.2);
						poseStack.mulPose(VecHelper.rotateY(180F));
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case PINK_FLOWER_BUD -> {
						poseStack.translate(0.275, -0.6, 0);
						poseStack.mulPose(VecHelper.rotateY(-90F));
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case POLKA_DOTTED_BOWS -> {
						poseStack.pushPose();
						poseStack.translate(0.275, -0.4, 0);
						poseStack.mulPose(VecHelper.rotateY(-90F));
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
						poseStack.popPose();
						poseStack.translate(-0.275, -0.4, 0);
						poseStack.mulPose(VecHelper.rotateY(90F));
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case BLUE_BUTTERFLY -> {
						poseStack.pushPose();
						poseStack.translate(0.275, -0.4, 0);
						poseStack.mulPose(VecHelper.rotateY(45F));
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
						poseStack.popPose();
						poseStack.translate(0.275, -0.4, 0);
						poseStack.mulPose(VecHelper.rotateY(-45F));
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case CAT_EARS -> {
						poseStack.translate(0F, -0.5F, -0.175F);
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case GOOGLY_EYES -> {
						poseStack.translate(0, -0.225, -0.3);
						poseStack.scale(0.9F, -0.9F, -0.9F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case CLOCK_EYE -> {
						poseStack.translate(0.1, -0.225, -0.3F);
						poseStack.scale(0.4F, -0.4F, -0.4F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case UNICORN_HORN -> {
						poseStack.translate(0, -0.7, -0.3);
						poseStack.mulPose(VecHelper.rotateY(-90F));
						poseStack.scale(0.6F, -0.6F, -0.6F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case DEVIL_HORNS -> {
						poseStack.translate(0F, -0.4F, -0.175F);
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case HYPER_PLUS -> {
						poseStack.translate(-0.15F, -0.45F, -0.3F);
						poseStack.scale(0.2F, -0.2F, -0.2F);
						submitItem(data, state, poseStack, collector, lightCoords);
						poseStack.translate(1.45F, 0F, 0F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case ANCIENT_MASK -> {
						poseStack.translate(0, -0.3, -0.3);
						poseStack.scale(0.7F, -0.7F, -0.7F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case EERIE_MASK -> {
						poseStack.translate(0, -0.25, -0.3);
						poseStack.scale(0.75F, -0.75F, -0.75F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case ALIEN_ANTENNA -> {
						poseStack.translate(0, -0.65, 0.2);
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case ORANGE_SHADES -> {
						poseStack.translate(0, -0.3, -0.3);
						poseStack.scale(0.7F, -0.7F, -0.7F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case GROUCHO_GLASSES -> {
						poseStack.translate(0, -0.1, -0.3);
						poseStack.scale(0.75F, -0.75F, -0.75F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case THICK_EYEBROWS -> {
						poseStack.pushPose();
						poseStack.translate(-0.1, -0.3, -0.3);
						poseStack.scale(0.3F, -0.3F, -0.3F);
						submitItem(data, state, poseStack, collector, lightCoords);
						poseStack.popPose();
						poseStack.translate(0.1, -0.3, -0.3);
						poseStack.mulPose(VecHelper.rotateY(180F));
						poseStack.scale(0.3F, -0.3F, -0.3F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case TINY_POTATO_MASK -> {
						poseStack.translate(0, -0.3, -0.3);
						poseStack.scale(0.6F, -0.6F, -0.6F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case QUESTGIVER_MARK -> {
						poseStack.translate(0, -0.8, -0.2);
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case THINKING_HAND -> {
						poseStack.translate(-0.1, 0, -0.3);
						poseStack.mulPose(VecHelper.rotateZ(-15F));
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					default -> {}
				}
			} else {
				model.body.translateAndRotate(poseStack);
				switch (variant) {
					case BLACK_BOWTIE -> {
						poseStack.translate(0, 0.1, -0.13);
						poseStack.scale(0.6F, -0.6F, -0.6F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case BLACK_TIE, PUFFY_SCARF -> {
						poseStack.translate(0, 0.25, -0.15);
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case WITCH_PIN -> {
						poseStack.translate(-0.1, 0.15, -0.15);
						poseStack.scale(0.2F, -0.2F, -0.2F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case DEVIL_TAIL -> {
						poseStack.translate(0, 0.55, 0.2);
						poseStack.mulPose(VecHelper.rotateY(-90F));
						poseStack.scale(0.6F, -0.6F, -0.6F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case KAMUI_EYE -> {
						poseStack.pushPose();
						poseStack.translate(0.4, 0.1, -0.2);
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
						poseStack.popPose();
						poseStack.translate(-0.4, 0.1, -0.2);
						poseStack.mulPose(VecHelper.rotateY(180F));
						poseStack.scale(0.5F, -0.5F, -0.5F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case FOUR_LEAF_CLOVER -> {
						poseStack.translate(0.1, 0.1, -0.13);
						poseStack.scale(0.3F, -0.3F, -0.3F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case BOTANIST_EMBLEM -> {
						poseStack.translate(0F, 0.375, -0.13);
						poseStack.scale(0.3F, -0.3F, -0.3F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					case LUSITANIC_SHIELD -> {
						poseStack.translate(0F, 0.35, 0.13);
						poseStack.mulPose(VecHelper.rotateZ(8F));
						poseStack.mulPose(VecHelper.rotateY(180F));
						poseStack.scale(0.6F, -0.6F, -0.6F);
						submitItem(data, state, poseStack, collector, lightCoords);
					}
					default -> {}
				}
			}
		}

		private static void submitItem(AccessoryRenderData data, AvatarRenderState state, PoseStack poseStack,
				SubmitNodeCollector collector, int lightCoords) {
			data.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		}
	}

}
