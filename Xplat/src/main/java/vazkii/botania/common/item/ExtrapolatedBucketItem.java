/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.mixin.AbstractCauldronBlockAccessor;

public class ExtrapolatedBucketItem extends Item {

	public ExtrapolatedBucketItem(Properties props) {
		super(props);
	}

	// [VanillaCopy] BucketItem, only the empty cases
	@NotNull
	@Override
	public InteractionResult use(
			Level level,
			Player player,
			@NotNull InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		BlockHitResult blockHitResult = getPlayerPOVHitResult(
				level,
				player,
				ClipContext.Fluid.SOURCE_ONLY
		);

		if (blockHitResult.getType() == HitResult.Type.MISS) {
			return InteractionResult.PASS;
		}

		if (blockHitResult.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}

		BlockPos blockPos = blockHitResult.getBlockPos();
		Direction direction = blockHitResult.getDirection();
		BlockPos relativePos = blockPos.relative(direction);

		if (!level.mayInteract(player, blockPos)
				|| !player.mayUseItemAt(
						relativePos,
						direction,
						itemStack
				)) {
			return InteractionResult.FAIL;
		}

		BlockState blockState = level.getBlockState(blockPos);

		if (blockState.getBlock() instanceof BucketPickup bucketPickup) {
			ItemStack pickedUp = bucketPickup.pickupBlock(
					player,
					level,
					blockPos,
					blockState
			);

			if (!pickedUp.isEmpty()) {
				player.awardStat(Stats.ITEM_USED.get(this));

				bucketPickup.getPickupSound().ifPresent(soundEvent ->
						player.playSound(soundEvent, 1.0F, 1.0F)
				);

				spawnParticles(level, blockPos);
				level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);

				if (!level.isClientSide()) {
					CriteriaTriggers.FILLED_BUCKET.trigger(
							(ServerPlayer) player,
							pickedUp
					);
				}

				// The extrapolated bucket deliberately remains unchanged.
				return level.isClientSide()
						? InteractionResult.SUCCESS
						: InteractionResult.CONSUME;
			}
		} else if (blockState.getBlock()
				instanceof AbstractCauldronBlock cauldronBlock) {
			CauldronInteraction interaction =
					((AbstractCauldronBlockAccessor) cauldronBlock)
							.botania_getInteractions()
							.get(new ItemStack(Items.BUCKET));

			BlockState fullState =
					blockState.hasProperty(LayeredCauldronBlock.LEVEL)
							? blockState.setValue(
									LayeredCauldronBlock.LEVEL,
									LayeredCauldronBlock.MAX_FILL_LEVEL
							)
							: blockState;

			InteractionResult result = interaction.interact(
					fullState,
					level,
					blockPos,
					player,
					interactionHand,
					itemStack.copy()
			);

			if (result instanceof InteractionResult.Success success) {
				spawnParticles(level, blockPos);

				if (!ItemStack.matches(
						player.getItemInHand(interactionHand),
						itemStack
				)) {
					// Do not replace the extrapolated bucket with a filled bucket.
					player.setItemInHand(interactionHand, itemStack);
				}

				// Remove any held-item transformation supplied by the vanilla
				// cauldron result while retaining its swing behavior.
				return switch (success.swingSource()) {
					case CLIENT -> InteractionResult.SUCCESS;
					case SERVER -> InteractionResult.SUCCESS_SERVER;
					case NONE -> InteractionResult.CONSUME;
				};
			}

			return result;
		}

		return InteractionResult.FAIL;
	}

	private static void spawnParticles(Level level, BlockPos blockPos) {
		for (int x = 0; x < 5; x++) {
			level.addParticle(ParticleTypes.POOF, blockPos.getX() + Math.random(), blockPos.getY() + Math.random(), blockPos.getZ() + Math.random(), 0, 0, 0);
		}
	}

}
