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
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.material.EnderAirItem;

public class EnderAirBottlingBehavior
		extends OptionalDispenseItemBehavior {
	private final DispenseItemBehavior parent;

	public EnderAirBottlingBehavior(
			DispenseItemBehavior parent) {
		this.parent = parent;
	}

	@Override
	protected void playSound(BlockSource source) {
		if (isSuccess()) {
			super.playSound(source);
		}
	}

	@Override
	protected void playAnimation(
			BlockSource source,
			Direction facing) {
		if (isSuccess()) {
			super.playAnimation(source, facing);
		}
	}

	private static boolean pickupInEnd(
			Level level,
			BlockPos target) {
		return level.dimension() == Level.END
				&& level.isEmptyBlock(target)
				&& level.isEmptyBlock(target.above())
				&& EnderAirItem.isClearFromDragonBreath(
						level,
						new AABB(target).inflate(2.0)
				);
	}

	@NotNull
	@Override
	protected ItemStack execute(
			BlockSource source,
			ItemStack stack) {
		Level level = source.level();

		Direction facing =
				source.state().getValue(
						DispenserBlock.FACING
				);

		BlockPos target =
				source.pos().relative(facing);

		boolean collected =
				pickupInEnd(level, target)
						|| EnderAirItem.pickupFromEntity(
								level,
								new AABB(target)
						);

		if (collected) {
			setSuccess(true);

			return consumeWithRemainder(
					source,
					stack,
					new ItemStack(
							BotaniaItems.enderAirBottle
					)
			);
		}

		setSuccess(false);
		return parent.dispense(source, stack);
	}
}