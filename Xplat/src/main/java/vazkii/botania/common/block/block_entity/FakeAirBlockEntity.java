/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.common.block.flower.functional.BubbellBlockEntity;

public class FakeAirBlockEntity extends BotaniaBlockEntity {
	private static final String TAG_FLOWER_X = "flowerX";
	private static final String TAG_FLOWER_Y = "flowerY";
	private static final String TAG_FLOWER_Z = "flowerZ";

	private BlockPos flowerPos = ManaBurst.NO_SOURCE;

	public FakeAirBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.FAKE_AIR, pos, state);
	}

	public void setFlower(BlockEntity tile) {
		flowerPos = tile.getBlockPos();
		setChanged();
	}

	public boolean canStay() {
		return BubbellBlockEntity.isValidBubbell(level, flowerPos);
	}

	@Override
	protected void writePersistentData(ValueOutput output) {
		output.putInt(TAG_FLOWER_X, flowerPos.getX());
		output.putInt(TAG_FLOWER_Y, flowerPos.getY());
		output.putInt(TAG_FLOWER_Z, flowerPos.getZ());
	}

	@Override
	protected void readPersistentData(ValueInput input) {
		if (input.getInt(TAG_FLOWER_X).isPresent()
				&& input.getInt(TAG_FLOWER_Y).isPresent()
				&& input.getInt(TAG_FLOWER_Z).isPresent()) {
			flowerPos = new BlockPos(input.getIntOr(TAG_FLOWER_X, 0),
					input.getIntOr(TAG_FLOWER_Y, 0), input.getIntOr(TAG_FLOWER_Z, 0));
		} else {
			flowerPos = ManaBurst.NO_SOURCE;
		}
	}

}
