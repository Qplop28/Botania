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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.accessory.AccessoryExtractionContext;
import vazkii.botania.client.render.accessory.AccessoryRenderData;
import vazkii.botania.client.render.accessory.BlockAccessoryRenderData;
import vazkii.botania.client.render.accessory.DeferredAccessoryRenderer;
import vazkii.botania.client.render.accessory.FlugelAccessoryRenderData;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.InventoryHelper;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.helper.StringObfuscator;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.StoneOfTemperanceItem;
import vazkii.botania.common.proxy.Proxy;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class FlugelTiaraItem extends BaubleItem implements CustomCreativeTabContents {

	private static final Identifier textureHud = Identifier.parse(ResourcesLib.GUI_HUD_ICONS);
	public static final Identifier textureHalo = Identifier.parse(ResourcesLib.MISC_HALO);

	private static final String TAG_VARIANT = "variant";
	private static final String TAG_FLYING = "flying";
	private static final String TAG_GLIDING = "gliding";
	private static final String TAG_TIME_LEFT = "timeLeft";
	private static final String TAG_INFINITE_FLIGHT = "infiniteFlight";
	private static final String TAG_DASH_COOLDOWN = "dashCooldown";
	private static final String TAG_IS_SPRINTING = "isSprinting";
	private static final String TAG_BOOST_PENDING = "boostPending";

	private static final List<String> playersWithFlight = Collections.synchronizedList(new ArrayList<>());
	private static final int COST = 35;
	private static final int COST_OVERKILL = COST * 3;
	private static final int MAX_FLY_TIME = 1200;

	private static final int SUBTYPES = 8;
	public static final int WING_TYPES = 9;

	private static final String SUPER_AWESOME_HASH = "4D0F274C5E3001C95640B5E88A821422C8B1E132264492C043A3D746B705C025";

	public FlugelTiaraItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.registerDeferred(this, new Renderer()));
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		for (int i = 0; i < SUBTYPES + 1; i++) {
			ItemStack stack = new ItemStack(this);
			ItemNBTHelper.setInt(stack, TAG_VARIANT, i);
			output.accept(stack);
		}

	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(Component.translatable("botania.wings" + getVariant(stack)));
	}

	public static void updatePlayerFlyStatus(Player player) {
		ItemStack tiara = EquipmentHandler.findOrEmpty(BotaniaItems.flightTiara, player);
		int left = ItemNBTHelper.getInt(tiara, TAG_TIME_LEFT, MAX_FLY_TIME);

		if (playersWithFlight.contains(playerStr(player))) {
			if (shouldPlayerHaveFlight(player)) {
				player.getAbilities().mayfly = true;
				if (player.getAbilities().flying) {
					if (!player.level().isClientSide()) {
						if (!player.isCreative() && !player.isSpectator()) {
							ManaItemHandler.instance().requestManaExact(tiara, player, getCost(tiara, left), true);
						}
					} else if (Math.abs(player.getDeltaMovement().x()) > 0.1 || Math.abs(player.getDeltaMovement().z()) > 0.1) {
						double x = player.getX() - 0.5;
						double y = player.getY() - 0.5;
						double z = player.getZ() - 0.5;

						float r = 1F;
						float g = 1F;
						float b = 1F;

						int variant = getVariant(tiara);
						switch (variant) {
							case 2 -> {
								r = 0.1F;
								g = 0.1F;
								b = 0.1F;
							}
							case 3 -> {
								r = 0F;
								g = 0.6F;
							}
							case 4 -> {
								g = 0.3F;
								b = 0.3F;
							}
							case 5 -> {
								r = 0.6F;
								g = 0F;
								b = 0.6F;
							}
							case 6 -> {
								r = 0.4F;
								g = 0F;
								b = 0F;
							}
							case 7 -> {
								r = 0.2F;
								g = 0.6F;
								b = 0.2F;
							}
							case 8 -> {
								r = 0.85F;
								g = 0.85F;
								b = 0F;
							}
							case 9 -> {
								r = 0F;
								b = 0F;
							}
						}

						for (int i = 0; i < 2; i++) {
							SparkleParticleData data = SparkleParticleData.sparkle(2F * (float) Math.random(), r, g, b, 20);
							player.level().addParticle(data, x + Math.random() * player.getBbWidth(), y + Math.random() * 0.4, z + Math.random() * player.getBbWidth(), 0, 0, 0);
						}
					}
				}
			} else {
				if (!player.isSpectator() && !player.getAbilities().instabuild) {
					player.getAbilities().mayfly = false;
					player.getAbilities().flying = false;
					player.getAbilities().invulnerable = false;
				}
				playersWithFlight.remove(playerStr(player));
			}
		} else if (shouldPlayerHaveFlight(player)) {
			playersWithFlight.add(playerStr(player));
			player.getAbilities().mayfly = true;
		}
	}

	public static void playerLoggedOut(ServerPlayer player) {
		String username = player.getGameProfile().name();
		playersWithFlight.remove(username + ":false");
		playersWithFlight.remove(username + ":true");
	}

	private static String playerStr(Player player) {
		return player.getGameProfile().name() + ":" + player.level().isClientSide();
	}

	private static boolean shouldPlayerHaveFlight(Player player) {
		ItemStack armor = EquipmentHandler.findOrEmpty(BotaniaItems.flightTiara, player);
		if (!armor.isEmpty()) {
			int left = ItemNBTHelper.getInt(armor, TAG_TIME_LEFT, MAX_FLY_TIME);
			boolean flying = ItemNBTHelper.getBoolean(armor, TAG_FLYING, false);
			return (left > (flying ? 0 : MAX_FLY_TIME / 10) || InventoryHelper.containsType(player.getInventory(), BotaniaItems.flugelEye)) && ManaItemHandler.instance().requestManaExact(armor, player, getCost(armor, left), false);
		}

		return false;
	}

	public static int getCost(ItemStack stack, int timeLeft) {
		return timeLeft <= 0 ? COST_OVERKILL : COST;
	}

	@Override
	public void onEquipped(ItemStack stack, LivingEntity living) {
		super.onEquipped(stack, living);
		int variant = getVariant(stack);
		if (variant != WING_TYPES && StringObfuscator.matchesHash(stack.getHoverName().getString(), SUPER_AWESOME_HASH)) {
			ItemNBTHelper.setInt(stack, TAG_VARIANT, WING_TYPES);
			stack.remove(DataComponents.CUSTOM_NAME);
		}
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity living) {
		if (living instanceof Player player) {
			boolean flying = player.getAbilities().flying;

			boolean wasSprting = ItemNBTHelper.getBoolean(stack, TAG_IS_SPRINTING, false);
			boolean isSprinting = player.isSprinting();
			if (isSprinting != wasSprting) {
				ItemNBTHelper.setBoolean(stack, TAG_IS_SPRINTING, isSprinting);
			}

			int time = ItemNBTHelper.getInt(stack, TAG_TIME_LEFT, MAX_FLY_TIME);
			int newTime = time;
			Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();

			if (flying) {
				if (time > 0 && !player.isSpectator() && !player.isCreative()
						&& !ItemNBTHelper.getBoolean(stack, TAG_INFINITE_FLIGHT, false)) {
					newTime--;
				}
				final int maxCd = 80;
				int cooldown = ItemNBTHelper.getInt(stack, TAG_DASH_COOLDOWN, 0);
				if (!wasSprting && isSprinting && cooldown == 0 && !StoneOfTemperanceItem.hasTemperanceActive(player)) {
					player.setDeltaMovement(player.getDeltaMovement().add(look.x, 0, look.z));
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.dash, SoundSource.PLAYERS, 1F, 1F);
					ItemNBTHelper.setInt(stack, TAG_DASH_COOLDOWN, maxCd);
					ItemNBTHelper.setBoolean(stack, TAG_BOOST_PENDING, true);
				} else if (cooldown > 0) {
					if (ItemNBTHelper.getBoolean(stack, TAG_BOOST_PENDING, false)) {
						living.moveRelative(5F, new Vec3(0F, 0F, 1F));
						ItemNBTHelper.removeEntry(stack, TAG_BOOST_PENDING);
					}
					ItemNBTHelper.setInt(stack, TAG_DASH_COOLDOWN, cooldown - 2);
				}
			} else {
				boolean wasGliding = ItemNBTHelper.getBoolean(stack, TAG_GLIDING, false);
				boolean doGlide = living.isShiftKeyDown() && !living.onGround() && (living.getDeltaMovement().y() < -.7F || wasGliding);
				if (time < MAX_FLY_TIME && living.tickCount % (doGlide ? 6 : 2) == 0) {
					newTime++;
				}

				if (doGlide) {
					float mul = 0.6F;
					living.setDeltaMovement(look.x * mul, Math.max(-0.15F, living.getDeltaMovement().y()), look.z * mul);
					living.fallDistance = 2F;
				}
				ItemNBTHelper.setBoolean(stack, TAG_GLIDING, doGlide);
			}

			ItemNBTHelper.setBoolean(stack, TAG_FLYING, flying);
			if (newTime != time) {
				ItemNBTHelper.setInt(stack, TAG_TIME_LEFT, newTime);
			}
		}
	}

	@Override
	public boolean hasRender(ItemStack stack, LivingEntity living) {
		return super.hasRender(stack, living) && living instanceof Player;
	}

	public static class Renderer implements DeferredAccessoryRenderer {
		@Override
		public AccessoryRenderData createData() {
			return new FlugelAccessoryRenderData();
		}

		@Override
		public void extract(AccessoryRenderData data, ItemStack stack, Player player, float partialTicks,
				AccessoryExtractionContext context) {
			FlugelAccessoryRenderData flugelData = (FlugelAccessoryRenderData) data;
			int variant = getVariant(stack);
			flugelData.variant = variant;
			flugelData.flying = player.getAbilities().flying;
			flugelData.animationTime = player.tickCount + partialTicks;

			if (variant <= 0 || variant > WING_TYPES) {
				flugelData.wingParts = List.of();
				return;
			}

			BlockStateModel wingModel = MiscellaneousModels.INSTANCE.tiaraWing(variant - 1);
			ClientXplatAbstractions.instance().markSpriteActive(wingModel.particleMaterial().sprite());
			flugelData.wingParts = BlockAccessoryRenderData.collect(wingModel);

			if (variant == 9) {
				flugelData.flap = -(float) ((Math.sin(flugelData.animationTime * 0.2F) + 0.6F)
						* (flugelData.flying ? 12F : 5F));
			} else {
				flugelData.flap = 20F + (float) ((Math.sin(flugelData.animationTime
						* (flugelData.flying ? 0.4F : 0.2F)) + 0.5F)
						* (flugelData.flying ? 30F : 5F));
			}

			if (variant == 7) {
				float alpha = 0.5F + (float) Math.cos(flugelData.animationTime * 0.3F) * 0.2F;
				flugelData.tintColor = 0xFFFFFF | ((int) (alpha * 255F) << 24);
			} else if (variant == 9) {
				float alpha = 0.5F + (flugelData.flying
						? (float) Math.cos(flugelData.animationTime * 0.3F) * 0.25F + 0.25F
						: 0F);
				flugelData.tintColor = 0xFFFFFF | ((int) (alpha * 255F) << 24);
			} else {
				flugelData.tintColor = 0xFFFFFFFF;
			}
		}

		@Override
		public void submit(AccessoryRenderData data, ItemStack stack, PlayerModel playerModel, AvatarRenderState state,
				PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			FlugelAccessoryRenderData flugelData = (FlugelAccessoryRenderData) data;
			if (flugelData.variant <= 0 || flugelData.variant > WING_TYPES || flugelData.wingParts.isEmpty()) {
				return;
			}

			switch (flugelData.variant) {
				case 1 -> {
					submitBasic(flugelData, playerModel, state, poseStack, collector, lightCoords);
					poseStack.pushPose();
					try {
						playerModel.body.translateAndRotate(poseStack);
						ClientLogic.submitHalo(poseStack, collector, flugelData.animationTime);
					} finally {
						poseStack.popPose();
					}
				}
				case 2 -> submitSephiroth(flugelData, playerModel, state, poseStack, collector, lightCoords);
				case 3 -> submitCirno(flugelData, playerModel, state, poseStack, collector, lightCoords);
				case 4 -> submitPhoenix(flugelData, playerModel, state, poseStack, collector);
				case 5 -> submitKuroyukihime(flugelData, playerModel, state, poseStack, collector);
				case 6, 8 -> submitBasic(flugelData, playerModel, state, poseStack, collector, lightCoords);
				case 7, 9 -> submitCustomColor(flugelData, playerModel, state, poseStack, collector);
				default -> {
				}
			}
		}

		private static void submitBasic(FlugelAccessoryRenderData data, PlayerModel playerModel,
				AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			poseStack.pushPose();
			try {
				playerModel.body.translateAndRotate(poseStack);
				poseStack.translate(0, 0.5, 0.2);
				for (int i = 0; i < 2; i++) {
					poseStack.pushPose();
					try {
						poseStack.mulPose(VecHelper.rotateY(i == 0 ? data.flap : 180F - data.flap));
						poseStack.translate(-1, 0, 0);
						poseStack.mulPose(VecHelper.rotateZ(-60));
						poseStack.scale(1.5F, -1.5F, -1.5F);
						submitWing(data, poseStack, collector, state, lightCoords, false);
					} finally {
						poseStack.popPose();
					}
				}
			} finally {
				poseStack.popPose();
			}
		}

		private static void submitSephiroth(FlugelAccessoryRenderData data, PlayerModel playerModel,
				AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			poseStack.pushPose();
			try {
				playerModel.body.translateAndRotate(poseStack);
				poseStack.translate(0, 0.5, 0.2);
				poseStack.mulPose(VecHelper.rotateY(data.flap));
				poseStack.translate(-1.1, 0, 0);
				poseStack.mulPose(VecHelper.rotateZ(-60));
				poseStack.scale(1.6F, -1.6F, -1.6F);
				submitWing(data, poseStack, collector, state, lightCoords, false);
			} finally {
				poseStack.popPose();
			}
		}

		private static void submitCirno(FlugelAccessoryRenderData data, PlayerModel playerModel,
				AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
			poseStack.pushPose();
			try {
				playerModel.body.translateAndRotate(poseStack);
				poseStack.translate(-0.8, 0.15, 0.25);
				for (int i = 0; i < 2; i++) {
					poseStack.pushPose();
					try {
						if (i == 1) {
							poseStack.mulPose(VecHelper.rotateY(180));
							poseStack.translate(-1.6, 0, 0);
						}
						poseStack.scale(1.6F, -1.6F, -1.6F);
						submitWing(data, poseStack, collector, state, lightCoords, false);
					} finally {
						poseStack.popPose();
					}
				}
			} finally {
				poseStack.popPose();
			}
		}

		private static void submitPhoenix(FlugelAccessoryRenderData data, PlayerModel playerModel,
				AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
			poseStack.pushPose();
			try {
				playerModel.body.translateAndRotate(poseStack);
				poseStack.translate(0, -0.2, 0.2);
				for (int i = 0; i < 2; i++) {
					poseStack.pushPose();
					try {
						poseStack.mulPose(VecHelper.rotateY(i == 0 ? data.flap : 180F - data.flap));
						poseStack.translate(-0.9, 0, 0);
						poseStack.scale(1.7F, -1.7F, -1.7F);
						submitWing(data, poseStack, collector, state, 0xF000F0, false);
					} finally {
						poseStack.popPose();
					}
				}
			} finally {
				poseStack.popPose();
			}
		}

		private static void submitKuroyukihime(FlugelAccessoryRenderData data, PlayerModel playerModel,
				AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
			poseStack.pushPose();
			try {
				playerModel.body.translateAndRotate(poseStack);
				poseStack.translate(0, -0.4, 0.2);
				for (int i = 0; i < 2; i++) {
					poseStack.pushPose();
					try {
						poseStack.mulPose(VecHelper.rotateY(i == 0 ? data.flap : 180F - data.flap));
						poseStack.translate(-1.3, 0, 0);
						poseStack.scale(2.5F, -2.5F, -2.5F);
						submitWing(data, poseStack, collector, state, 0xF000F0, false);
					} finally {
						poseStack.popPose();
					}
				}
			} finally {
				poseStack.popPose();
			}
		}

		private static void submitCustomColor(FlugelAccessoryRenderData data, PlayerModel playerModel,
				AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
			poseStack.pushPose();
			try {
				playerModel.body.translateAndRotate(poseStack);
				poseStack.translate(0, 0, 0.2);
				for (int i = 0; i < 2; i++) {
					poseStack.pushPose();
					try {
						poseStack.mulPose(VecHelper.rotateY(i == 0 ? data.flap : 180F - data.flap));
						poseStack.translate(-0.7, 0, 0);
						poseStack.scale(1.5F, -1.5F, -1.5F);
						submitWing(data, poseStack, collector, state, 0xF000F0, true);
					} finally {
						poseStack.popPose();
					}
				}
			} finally {
				poseStack.popPose();
			}
		}

		private static void submitWing(FlugelAccessoryRenderData data, PoseStack poseStack,
				SubmitNodeCollector collector, AvatarRenderState state, int lightCoords, boolean translucent) {
			collector.submitBlockModel(
					poseStack,
					translucent ? RenderHelper.TRANSLUCENT : Sheets.cutoutBlockSheet(),
					data.wingParts,
					translucent ? new int[] { data.tintColor } : BlockModelRenderState.EMPTY_TINTS,
					lightCoords,
					OverlayTexture.NO_OVERLAY,
					state.outlineColor
			);
		}
	}

	public static class ClientLogic {
		public static void submitHalo(PoseStack poseStack, SubmitNodeCollector collector, float animationTime) {
			poseStack.translate(0.2, -0.65, 0);
			poseStack.mulPose(VecHelper.rotateZ(30));
			poseStack.mulPose(VecHelper.rotateY(animationTime));
			poseStack.scale(0.75F, -0.75F, -0.75F);
			collector.submitCustomGeometry(poseStack, RenderHelper.HALO, (pose, buffer) -> {
				Matrix4f matrix = pose.pose();
				buffer.addVertex(matrix, -1F, 0, -1F).setColor(0xFFFFFFFF).setUv(0, 0);
				buffer.addVertex(matrix, 1F, 0, -1F).setColor(0xFFFFFFFF).setUv(1, 0);
				buffer.addVertex(matrix, 1F, 0, 1F).setColor(0xFFFFFFFF).setUv(1, 1);
				buffer.addVertex(matrix, -1F, 0, 1F).setColor(0xFFFFFFFF).setUv(0, 1);
			});
		}

		private static int estimateAdditionalNumRowsRendered(Player player) {
			if (player.isEyeInFluid(FluidTags.WATER) || player.getAirSupply() < player.getMaxAirSupply()) {
				// shift up single row if player is underwater or still recovering air
				return 1;
			}

			Entity playerVehicle = player.getVehicle();
			if (playerVehicle instanceof LivingEntity vehicle && vehicle.showVehicleHealth()) {
				// shift up if vehicle health requires more than one row (vanilla HUD limits vehicle hearts to 3 rows)
				return (Math.min(30, (int) (vehicle.getMaxHealth() + 0.5) / 2) - 1) / 10;
			}

			return 0;
		}

		public static void renderHUD(GuiGraphicsExtractor gui, Player player, ItemStack stack) {
			int u = Math.max(1, getVariant(stack)) * 9 - 9;
			int v = 0;

			Minecraft mc = Minecraft.getInstance();
			int xo = mc.getWindow().getGuiScaledWidth() / 2 + 10;
			int y = mc.getWindow().getGuiScaledHeight() - 10 * estimateAdditionalNumRowsRendered(player) - 49;

			int left = ItemNBTHelper.getInt(stack, TAG_TIME_LEFT, MAX_FLY_TIME);

			int segTime = MAX_FLY_TIME / 10;
			int segs = left / segTime + 1;
			int last = left % segTime;

			for (int i = 0; i < segs; i++) {
				float trans = 1F;
				if (i == segs - 1) {
					trans = (float) last / (float) segTime;
				}
				gui.blit(RenderPipelines.GUI_TEXTURED, textureHud, xo + 8 * i, y, u, v,
						9, 9, 9, 9, 256, 256, ARGB.white(trans));
			}

			if (player.getAbilities().flying) {
				int width = ItemNBTHelper.getInt(stack, TAG_DASH_COOLDOWN, 0);
				if (width > 0) {
					gui.fill(xo, y - 2, xo + 80, y - 1, 0x88000000);
				}
				gui.fill(xo, y - 2, xo + width, y - 1, 0xFFFFFFFF);
			}
		}
	}

	public static int getVariant(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_VARIANT, 0);
	}
}
