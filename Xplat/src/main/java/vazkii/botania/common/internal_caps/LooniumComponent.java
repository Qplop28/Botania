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
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import vazkii.botania.api.BotaniaAPI;

import java.util.Objects;

public class LooniumComponent extends SerializableComponent {
	protected static final String TAG_TO_DROP = "toDrop";
	protected static final String TAG_OVERRIDE_DROP = "overrideDrop";
	protected static final String TAG_SLOW_DESPAWN = "slowDespawn";
	private ItemStack toDrop = ItemStack.EMPTY;
	private boolean overrideDrop;
	private boolean slowDespawn;

	public ItemStack getDrop() {
		return toDrop;
	}

	public void setDrop(ItemStack stack) {
		this.toDrop = stack;
	}

	public boolean isOverrideDrop() {
		return overrideDrop;
	}

	public void setOverrideDrop(boolean overrideDrop) {
		this.overrideDrop = overrideDrop;
	}

	public boolean isSlowDespawn() {
		return slowDespawn;
	}

	public void setSlowDespawn(boolean slowDespawn) {
		this.slowDespawn = slowDespawn;
	}

	@Override
	public void readData(ValueInput input) {
		ItemStack drop = input.read(TAG_TO_DROP, ItemStack.OPTIONAL_CODEC)
				.orElse(ItemStack.EMPTY);

		setDrop(drop);
		setOverrideDrop(input.getBooleanOr(TAG_OVERRIDE_DROP, false));
		setSlowDespawn(input.getBooleanOr(TAG_SLOW_DESPAWN, false));
	}

	@Override
	public void writeData(ValueOutput output) {
		if (isOverrideDrop()) {
			if (!getDrop().isEmpty()) {
				output.store(TAG_TO_DROP, ItemStack.OPTIONAL_CODEC, getDrop());
			}

			output.putBoolean(TAG_OVERRIDE_DROP, true);
		}

		if (isSlowDespawn()) {
			output.putBoolean(TAG_SLOW_DESPAWN, true);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof LooniumComponent component && ItemStack.matches(component.toDrop, toDrop)
				&& component.overrideDrop == overrideDrop && component.slowDespawn == slowDespawn;
	}

	@Override
	public int hashCode() {
		return Objects.hash(toDrop.hashCode(), overrideDrop, slowDespawn);
	}

	@Override
	public void readFromNbt(CompoundTag tag) {
		ItemStack drop = tag.getCompound(TAG_TO_DROP)
				.flatMap(compound ->
						ItemStack.OPTIONAL_CODEC
								.parse(NbtOps.INSTANCE, compound)
								.resultOrPartial(error ->
										BotaniaAPI.LOGGER.warn(
												"Failed to decode Loonium drop stack: {}",
												error
										)
								)
				)
				.orElse(ItemStack.EMPTY);

		setDrop(drop);
		setOverrideDrop(tag.getBooleanOr(TAG_OVERRIDE_DROP, false));
		setSlowDespawn(tag.getBooleanOr(TAG_SLOW_DESPAWN, false));
	}

	@Override
	public void writeToNbt(CompoundTag tag) {
		if (isOverrideDrop()) {
			if (!getDrop().isEmpty()) {
				ItemStack.OPTIONAL_CODEC
						.encodeStart(NbtOps.INSTANCE, getDrop())
						.resultOrPartial(error ->
								BotaniaAPI.LOGGER.warn(
										"Failed to encode Loonium drop stack: {}",
										error
								)
						)
						.ifPresent(encoded -> tag.put(TAG_TO_DROP, encoded));
			}
			tag.putBoolean(TAG_OVERRIDE_DROP, true);
		}
		if (isSlowDespawn()) {
			tag.putBoolean(TAG_SLOW_DESPAWN, true);
		}
	}
}
