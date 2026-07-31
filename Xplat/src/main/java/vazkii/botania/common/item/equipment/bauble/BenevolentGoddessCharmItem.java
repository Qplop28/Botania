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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.accessory.AccessoryExtractionContext;
import vazkii.botania.client.render.accessory.AccessoryRenderData;
import vazkii.botania.client.render.accessory.DeferredAccessoryRenderer;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.proxy.Proxy;

import java.util.List;

public class BenevolentGoddessCharmItem extends BaubleItem {

	public static final int COST = 1000;

	public BenevolentGoddessCharmItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.registerDeferred(this, new Renderer()));
	}

	public static boolean shouldProtectExplosion(Level world, Vec3 vec) {
		List<Player> players = world.getEntitiesOfClass(Player.class, new AABB(vec.x, vec.y, vec.z, vec.x, vec.y, vec.z).inflate(8));

		for (Player player : players) {
			ItemStack charm = EquipmentHandler.findOrEmpty(BotaniaItems.goddessCharm, player);
			if (!charm.isEmpty() && ManaItemHandler.instance().requestManaExact(charm, player, COST, true)) {
				return true;
			}
		}
		return false;
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
			model.head.translateAndRotate(poseStack);
			poseStack.translate(0.275, -0.4, 0);
			poseStack.mulPose(VecHelper.rotateY(-90F));
			poseStack.scale(0.55F, -0.55F, -0.55F);
			data.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		}
	}

}
