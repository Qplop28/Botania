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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.accessory.AccessoryExtractionContext;
import vazkii.botania.client.render.accessory.AccessoryRenderData;
import vazkii.botania.client.render.accessory.BlockAccessoryRenderData;
import vazkii.botania.client.render.accessory.DeferredAccessoryRenderer;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.proxy.Proxy;
import vazkii.botania.mixin.BiomeAccessor;

public class SnowflakePendantItem extends BaubleItem {

	public SnowflakePendantItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.registerDeferred(this, new Renderer()));
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		if (!entity.level().isClientSide() && !entity.isShiftKeyDown()) {
			ServerLevel level = (ServerLevel) entity.level();

			boolean lastOnGround = entity.onGround();
			entity.setOnGround(true);

			// Frost Walker's 26.1 radius is level + 2.
			// Level 6 preserves Botania's previous radius of 8.
			var frostWalker = level.registryAccess()
					.lookupOrThrow(Registries.ENCHANTMENT)
					.getOrThrow(Enchantments.FROST_WALKER);

			frostWalker.value().runLocationChangedEffects(
					level,
					6,
					new EnchantedItemInUse(
							stack,
							EquipmentSlot.FEET,
							entity
					),
					entity
			);

			entity.setOnGround(lastOnGround);

			int x;
			int y = Mth.floor(entity.getY());
			int z;
			BlockState blockstate = Blocks.SNOW.defaultBlockState();

			for (int l = 0; l < 4; ++l) {
				x = Mth.floor(entity.getX() + (double) ((float) (l % 2 * 2 - 1) * 0.25F));
				z = Mth.floor(entity.getZ() + (double) ((float) (l / 2 % 2 * 2 - 1) * 0.25F));
				BlockPos blockpos = new BlockPos(x, y, z);

				if (entity.level().isEmptyBlock(blockpos) && blockstate.canSurvive(entity.level(), blockpos)) {
					var biome = entity.level().getBiome(blockpos);
					if (((BiomeAccessor) (Object) biome.value()).callGetTemperature(blockpos) < 0.9F) {
						entity.level().setBlockAndUpdate(blockpos, blockstate);
					}
				}
			}
		} else if (entity.level().isClientSide() && !entity.isShiftKeyDown()) {
			var random = entity.getRandom();
			if (random.nextFloat() >= 0.25F) {
				entity.level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SNOW_BLOCK.defaultBlockState()), entity.getX() + random.nextFloat() * 0.6 - 0.3, entity.getY() + 1.1, entity.getZ() + random.nextFloat() * 0.6 - 0.3, 0, -0.15, 0);
			}
		}
	}

	// called via Curio API on Forge
	@SoftImplement("IForgeItem")
	public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
		return true;
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
			blockData.partsA = BlockAccessoryRenderData.collect(MiscellaneousModels.INSTANCE.snowflakePendantGem());
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
