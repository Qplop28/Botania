package vazkii.botania.common.internal_caps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;

public abstract class SerializableComponent {
	public abstract void readData(ValueInput input);

	public abstract void writeData(ValueOutput output);

	public abstract void readFromNbt(CompoundTag tag);

	public abstract void writeToNbt(CompoundTag tag);

	@NotNull
	public final CompoundTag serializeNBT() {
		var ret = new CompoundTag();
		writeToNbt(ret);
		return ret;
	}

	public final void deserializeNBT(CompoundTag nbt) {
		readFromNbt(nbt);
	}
}
