/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.entity.ManaPoolMinecartEntity;

// [VanillaCopy] MinecartDispenseItemBehavior
public class ManaPoolMinecartBehavior
		extends DefaultDispenseItemBehavior {
	private final DefaultDispenseItemBehavior
			defaultBehavior =
					new DefaultDispenseItemBehavior();

	@NotNull
	@Override
	protected ItemStack execute(
			BlockSource source,
			ItemStack stack) {
		Direction direction =
				source.state().getValue(
						DispenserBlock.FACING
				);

		ServerLevel level = source.level();
		Vec3 center = source.center();

		double spawnX =
				center.x()
						+ direction.getStepX()
						* 1.125;

		double spawnY =
				Math.floor(center.y())
						+ direction.getStepY();

		double spawnZ =
				center.z()
						+ direction.getStepZ()
						* 1.125;

		BlockPos front =
				source.pos().relative(direction);

		BlockState blockFront =
				level.getBlockState(front);

		double yOffset;

		if (blockFront.is(BlockTags.RAILS)) {
			yOffset =
					getRailShape(blockFront).isSlope()
							? 0.6
							: 0.1;
		} else {
			if (!blockFront.isAir()) {
				return defaultBehavior.dispense(
						source,
						stack
				);
			}

			BlockState blockBelow =
					level.getBlockState(
							front.below()
					);

			if (!blockBelow.is(BlockTags.RAILS)) {
				return defaultBehavior.dispense(
						source,
						stack
				);
			}

			if (direction != Direction.DOWN
					&& getRailShape(
							blockBelow
					).isSlope()) {
				yOffset = -0.4;
			} else {
				yOffset = -0.9;
			}
		}

		ManaPoolMinecartEntity minecart =
				new ManaPoolMinecartEntity(
						level,
						spawnX,
						spawnY + yOffset,
						spawnZ
				);

		if (stack.has(DataComponents.CUSTOM_NAME)) {
			minecart.setCustomName(
					stack.get(DataComponents.CUSTOM_NAME)
			);
		}

		level.addFreshEntity(minecart);
		stack.shrink(1);

		return stack;
	}

	private static RailShape getRailShape(
			BlockState state) {
		if (state.getBlock()
				instanceof BaseRailBlock railBlock) {
			return state.getValue(
					railBlock.getShapeProperty()
			);
		}

		return RailShape.NORTH_SOUTH;
	}

	@Override
	protected void playSound(BlockSource source) {
		source.level().levelEvent(
				1000,
				source.pos(),
				0
		);
	}
}
