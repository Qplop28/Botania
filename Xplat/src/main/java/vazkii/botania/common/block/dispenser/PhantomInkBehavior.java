package vazkii.botania.common.block.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.block.PhantomInkableBlock;
import vazkii.botania.xplat.XplatAbstractions;

public class PhantomInkBehavior
		extends OptionalDispenseItemBehavior {
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

		BlockState state =
				level.getBlockState(target);

		PhantomInkableBlock inkable =
				XplatAbstractions.INSTANCE
						.findPhantomInkable(
								level,
								target,
								state,
								level.getBlockEntity(target)
						);

		setSuccess(
				inkable != null
						&& inkable.onPhantomInked(
								null,
								stack,
								facing.getOpposite()
						)
		);

		return stack;
	}
}