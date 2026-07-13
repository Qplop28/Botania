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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.BotaniaBlocks;

// Taken from vanilla pumpkin dispense behaviour
public class FelPumpkinBehavior
		extends OptionalDispenseItemBehavior {
	@NotNull
	@Override
	protected ItemStack execute(
			BlockSource source,
			ItemStack stack) {
		ServerLevel level = source.level();

		Direction facing =
				source.state().getValue(
						DispenserBlock.FACING
				);

		BlockPos target =
				source.pos().relative(facing);

		Block felPumpkin =
				BotaniaBlocks.felPumpkin;

		setSuccess(false);

		if (level.isEmptyBlock(target)
				&& level.getBlockState(
						target.below()
				).is(Blocks.IRON_BARS)
				&& level.getBlockState(
						target.below(2)
				).is(Blocks.IRON_BARS)) {
			setSuccess(true);

			level.setBlockAndUpdate(
					target,
					felPumpkin.defaultBlockState()
			);

			stack.shrink(1);
		}

		return stack;
	}
}