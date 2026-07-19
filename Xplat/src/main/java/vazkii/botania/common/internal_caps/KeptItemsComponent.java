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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KeptItemsComponent extends SerializableComponent {
	private final List<ItemStack> stacks = new ArrayList<>();

	public void addAll(Collection<ItemStack> stack) {
		stacks.addAll(stack);
	}

	public List<ItemStack> getStacks() {
		return stacks;
	}

	@Override
	public void readFromNbt(CompoundTag tag) {
		stacks.clear();
		ListTag list = tag.getListOrEmpty("stacks");
		for (Tag element : list) {
			ItemStack stack = ItemStack.OPTIONAL_CODEC
					.parse(NbtOps.INSTANCE, element)
					.resultOrPartial(error ->
							BotaniaAPI.LOGGER.warn(
									"Failed to decode kept item stack: {}",
									error
							)
					)
					.orElse(ItemStack.EMPTY);

			stacks.add(stack);
		}
	}

	@Override
	public void writeToNbt(CompoundTag tag) {
		ListTag list = new ListTag();
		for (ItemStack stack : stacks) {
			Tag encoded = ItemStack.OPTIONAL_CODEC
					.encodeStart(NbtOps.INSTANCE, stack)
					.resultOrPartial(error ->
							BotaniaAPI.LOGGER.warn(
									"Failed to encode kept item stack: {}",
									error
							)
					)
					.orElseGet(CompoundTag::new);

			list.add(encoded);
		}
		tag.put("stacks", list);
	}
}
