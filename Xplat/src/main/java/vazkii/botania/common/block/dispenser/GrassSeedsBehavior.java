package vazkii.botania.common.block.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.block.FloatingFlower.IslandType;
import vazkii.botania.common.item.GrassSeedsItem;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

public class GrassSeedsBehavior
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

		GrassSeedsItem seeds =
				(GrassSeedsItem) stack.getItem();

		IslandType islandType =
				seeds.getIslandType(stack);

		setSuccess(
				seeds.applySeeds(
						level,
						target,
						stack
				).consumesAction()
		);

		if (isSuccess()) {
			XplatAbstractions.INSTANCE.sendToNear(
					level,
					target,
					new BotaniaEffectPacket(
							EffectType
									.GRASS_SEED_PARTICLES,
							target.getX(),
							target.getY(),
							target.getZ(),
							GrassSeedsItem.getColor(
									islandType
							)
					)
			);

			return stack;
		}

		return super.execute(source, stack);
	}
}