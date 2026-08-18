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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TeruTeruBozuBlockEntity extends BotaniaBlockEntity {
	private boolean wasRaining = false;

	public TeruTeruBozuBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.TERU_TERU_BOZU, pos, state);
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state, TeruTeruBozuBlockEntity self) {
		boolean isRaining = level.isRaining();
		if (isRaining && level.getRandom().nextInt(9600) == 0) {
			setRaining(level, false);
		}

		if (self.wasRaining != isRaining) {
			level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
		}
		self.wasRaining = isRaining;
	}

	public static void setRaining(Level level, boolean raining) {
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.getWeatherData().setRaining(raining);
			resetRainTime(level, raining);
		}
	}

	public static void resetRainTime(Level w) {
		resetRainTime(w, w.isRaining());
	}

	private static void resetRainTime(Level w, boolean raining) {
		int time = w.getRandom().nextInt(raining ? 12000 : 168000) + 12000;
		if (w instanceof ServerLevel serverLevel) {
			var weather = serverLevel.getWeatherData();
			weather.setRaining(raining);
			weather.setThundering(false);
			weather.setThunderTime(0);
			if (raining) {
				weather.setClearWeatherTime(0);
				weather.setRainTime(time);
			} else {
				weather.setClearWeatherTime(time);
				weather.setRainTime(0);
			}
		}
	}
}
