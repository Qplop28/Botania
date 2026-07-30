/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.lens;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import vazkii.botania.api.internal.ManaBurst;

public class KindleLens extends Lens {
	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		Entity entity = burst.entity();
		if (!entity.level().isClientSide()) {
			entity.igniteForSeconds(3.0F);
		}
	}

	@Override
	public boolean collideBurst(ManaBurst burst, HitResult rtr, boolean isManaBlock, boolean shouldKill, ItemStack stack) {
		Projectile entity = burst.entity();

		if (entity.level() instanceof ServerLevel level && rtr.getType() == HitResult.Type.BLOCK
				&& !burst.isFake() && !isManaBlock) {
			BlockHitResult brtr = (BlockHitResult) rtr;
			BlockPos pos = brtr.getBlockPos();
			Direction dir = brtr.getDirection();

			BlockPos offPos = pos.relative(dir);

			BlockState stateAt = level.getBlockState(pos);
			BlockState stateAtOffset = level.getBlockState(offPos);

			if (stateAt.is(Blocks.NETHER_PORTAL) && entity.mayInteract(level, pos)) {
				level.destroyBlock(pos, false, entity);
			}
			if (!entity.mayInteract(level, offPos)) {
				return true;
			}
			if (stateAtOffset.is(Blocks.NETHER_PORTAL)) {
				level.destroyBlock(offPos, false, entity);
			} else if (BaseFireBlock.canBePlacedAt(level, offPos, dir.getOpposite())) {
				level.setBlockAndUpdate(offPos, BaseFireBlock.getState(level, offPos));
			}
		}

		return shouldKill;
	}

}
