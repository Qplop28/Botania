/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.rod;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.mixin.AbstractCauldronBlockAccessor;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.function.Consumer;

public class SeasRodItem extends Item {

	public static final int COST = 75;

	public SeasRodItem(Properties props) {
		super(props);
	}

	// [VanillaCopy] BucketItem, placement case
	@NotNull
	@Override
	public InteractionResult use(
			Level level,
			Player player,
			@NotNull InteractionHand interactionHand) {
		ItemStack itemStack =
				player.getItemInHand(interactionHand);

		BlockHitResult blockHitResult =
				getPlayerPOVHitResult(
						level,
						player,
						ClipContext.Fluid.NONE
				);

		if (blockHitResult.getType() == HitResult.Type.MISS) {
			return InteractionResult.PASS;
		}

		if (blockHitResult.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}

		BlockPos blockPos = blockHitResult.getBlockPos();
		Direction direction = blockHitResult.getDirection();
		BlockPos adjacentPos = blockPos.relative(direction);

		if (!level.mayInteract(player, blockPos)
				|| !player.mayUseItemAt(
						adjacentPos,
						direction,
						itemStack
				)) {
			return InteractionResult.FAIL;
		}

		BlockState blockState =
				level.getBlockState(blockPos);

		boolean manaSuccess =
				ManaItemHandler.instance()
						.requestManaExactForTool(
								itemStack,
								player,
								COST,
								true
						);

		if (manaSuccess
				&& !player.isShiftKeyDown()
				&& blockState.getBlock()
						instanceof AbstractCauldronBlock cauldronBlock) {
			CauldronInteraction interaction =
					((AbstractCauldronBlockAccessor) cauldronBlock)
							.botania_getInteractions()
							.get(Items.WATER_BUCKET);

			if (interaction != null) {
				InteractionResult result =
						interaction.interact(
								blockState,
								level,
								blockPos,
								player,
								interactionHand,
								itemStack.copy()
						);

				if (!ItemStack.matches(
						player.getItemInHand(interactionHand),
						itemStack
				)) {
					player.setItemInHand(
							interactionHand,
							itemStack
					);
				}

				if (result.consumesAction()) {
					spawnParticles(player, blockPos);
				}

				return result;
			}
		}

		BlockPos placementPos =
				blockState.getBlock()
						instanceof LiquidBlockContainer
						? blockPos
						: adjacentPos;

		boolean success =
				manaSuccess
						&& ((BucketItem) Items.WATER_BUCKET)
								.emptyContents(
										player,
										level,
										placementPos,
										blockHitResult
								);

		if (success) {
			if (player instanceof ServerPlayer serverPlayer) {
				CriteriaTriggers.PLACED_BLOCK.trigger(
						serverPlayer,
						placementPos,
						itemStack
				);
			}

			player.awardStat(Stats.ITEM_USED.get(this));
			spawnParticles(player, placementPos);

			return level.isClientSide()
					? InteractionResult.SUCCESS
					: InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.FAIL;
	}

	private static void spawnParticles(Player player, BlockPos blockPos3) {
		SparkleParticleData data = SparkleParticleData.sparkle(1F, 0.2F, 0.2F, 1F, 5);
		for (int i = 0; i < 6; i++) {
			player.level().addParticle(data, blockPos3.getX() + Math.random(),
					blockPos3.getY() + Math.random(), blockPos3.getZ() + Math.random(), 0, 0, 0);
		}
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack seasRod, Slot slot, ClickAction action, Player player) {
		return fillItemWithWater(seasRod, action, player, slot.getItem(), slot::set);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack seasRod, ItemStack other, Slot slot, ClickAction action,
			Player player, SlotAccess access) {
		return fillItemWithWater(seasRod, action, player, other, access::set);
	}

	private static boolean fillItemWithWater(ItemStack seasRod, ClickAction action, Player player,
			ItemStack stackToFill, Consumer<ItemStack> slotSetter) {
		if (action != ClickAction.SECONDARY || stackToFill.is(BotaniaItems.openBucket)
				|| !ManaItemHandler.instance().requestManaExactForTool(seasRod, player, COST, false)) {
			return false;
		}
		ItemStack filled = XplatAbstractions.instance().fillItemWithWater(stackToFill, player);
		if (!filled.isEmpty()) {
			ManaItemHandler.instance().requestManaExactForTool(seasRod, player, COST, true);
			if (stackToFill.getCount() == 1) {
				slotSetter.accept(filled);
			} else {
				stackToFill.shrink(1);
				player.getInventory().placeItemBackInInventory(filled);
			}
			return true;
		}

		return false;
	}
}
