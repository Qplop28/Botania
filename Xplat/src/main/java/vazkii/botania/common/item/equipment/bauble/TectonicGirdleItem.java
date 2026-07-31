/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.core.helper.AccessoryRenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.accessory.AccessoryExtractionContext;
import vazkii.botania.client.render.accessory.AccessoryRenderData;
import vazkii.botania.client.render.accessory.DeferredAccessoryRenderer;
import vazkii.botania.client.render.accessory.ModelAccessoryRenderData;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.proxy.Proxy;

public class TectonicGirdleItem extends BaubleItem {

	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_KNOCKBACK_BELT);

	public TectonicGirdleItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.registerDeferred(this, new Renderer()));
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(getBaubleModifierId(stack), 1, AttributeModifier.Operation.ADD_VALUE));
		return attributes;
	}

	public static boolean negateExplosionKnockback(LivingEntity living) {
		// TODO 1.21: replace with explosion knockback resistance attribute
		return !EquipmentHandler.findOrEmpty(BotaniaItems.knockbackBelt, living).isEmpty();
	}

	public static class Renderer implements DeferredAccessoryRenderer {
		private HumanoidModel<HumanoidRenderState> model;
		private EntityModelSet modelSet;

		@Override
		public AccessoryRenderData createData() {
			return new ModelAccessoryRenderData();
		}

		@Override
		public void extract(AccessoryRenderData data, ItemStack stack, Player player, float partialTicks,
				AccessoryExtractionContext context) {
			ModelAccessoryRenderData modelData = (ModelAccessoryRenderData) data;
			modelData.crouching = player.isCrouching();
			ensureModel(context);
		}

		@Override
		public void submit(AccessoryRenderData data, ItemStack stack, PlayerModel playerModel, AvatarRenderState state,
				PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			ModelAccessoryRenderData modelData = (ModelAccessoryRenderData) data;
			AccessoryRenderHelper.rotateIfSneaking(poseStack, modelData.crouching);

			float scale = 1.15F;
			poseStack.scale(scale, scale, scale);

			collector.submitModelPart(model.body, poseStack, model.renderType(texture),
					lightCoords, OverlayTexture.NO_OVERLAY, null, false, false,
					0xFFFFFFFF, null, state.outlineColor);
		}

		private void ensureModel(AccessoryExtractionContext context) {
			if (model == null || modelSet != context.entityModels()) {
				modelSet = context.entityModels();
				model = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER));
			}
		}
	}
}
