/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.internal_caps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TigerseyeComponent extends SerializableComponent {
	private static final String TAG_PACIFIED = "botania:tigerseye_pacified";
	private boolean pacified = false;

	public boolean isPacified() {
		return pacified;
	}

	public void setPacified() {
		this.pacified = true;
	}

	@Override
	public void readData(ValueInput input) {
		pacified = input.getBooleanOr(TAG_PACIFIED, false);
	}

	@Override
	public void writeData(ValueOutput output) {
		if (pacified) {
			output.putBoolean(TAG_PACIFIED, true);
		}
	}

	@Override
	public void readFromNbt(CompoundTag tag) {
		this.pacified = tag.getBooleanOr(TAG_PACIFIED, false);
	}

	@Override
	public void writeToNbt(CompoundTag tag) {
		if (pacified) {
			tag.putBoolean(TAG_PACIFIED, true);
		}
	}
}
