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
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;

import org.jetbrains.annotations.NotNull;

public class SeedsBehavior
		extends OptionalDispenseItemBehavior {
	private final Block block;

	public SeedsBehavior(Block block) {
		this.block = block;
	}

	@NotNull
	@Override
	protected ItemStack execute(
			BlockSource source,
			ItemStack stack) {
		Direction facing =
				source.state().getValue(
						DispenserBlock.FACING
				);

		BlockPos target =
				source.pos().relative(facing);

		Level level = source.level();

		setSuccess(false);

		if (level.isEmptyBlock(target)
				&& block.defaultBlockState()
						.canSurvive(level, target)) {
			level.setBlockAndUpdate(
					target,
					block.defaultBlockState()
			);

			stack.shrink(1);
			setSuccess(true);
		}

		return stack;
	}
}