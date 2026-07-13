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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

public class CocoaBeansBehavior
		extends OptionalDispenseItemBehavior {
	@NotNull
	@Override
	protected ItemStack execute(
			BlockSource source,
			ItemStack stack) {
		Block block = Blocks.COCOA;

		Direction facing =
				source.state().getValue(
						DispenserBlock.FACING
				);

		BlockPos target =
				source.pos().relative(facing);

		Level level = source.level();

		BlockPlaceContext context =
				new DirectionalPlaceContext(
						level,
						target,
						facing,
						new ItemStack(block),
						facing.getOpposite()
				);

		BlockState cocoa =
				block.getStateForPlacement(context);

		if (cocoa != null
				&& level.isEmptyBlock(target)) {
			level.setBlockAndUpdate(
					target,
					cocoa
			);

			stack.shrink(1);
		}

		return stack;
	}
}