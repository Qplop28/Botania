/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import vazkii.botania.common.helper.ItemNBTHelper;

/**
 * An inventory that writes into the provided stack's NBT on save
 */
public class ItemBackedInventory extends SimpleContainer {
	private static final String TAG_ITEMS = "Items";
	private final ItemStack stack;

	public ItemBackedInventory(ItemStack stack, int expectedSize) {
		super(expectedSize);
		this.stack = stack;

		ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
		if (contents != null) {
			contents.copyInto(getItems());
		} else {
			// Upgrade the pre-components representation once. New writes remove this key.
			var legacy = ItemNBTHelper.getList(stack, TAG_ITEMS, Tag.TAG_COMPOUND, true);
			if (legacy != null) {
				for (int i = 0; i < expectedSize && i < legacy.size(); i++) {
					int slot = i;
					legacy.getCompound(i)
							.flatMap(tag -> ItemStack.CODEC.parse(NbtOps.INSTANCE, tag).result())
							.ifPresent(item -> setItem(slot, item));
				}
				ItemNBTHelper.removeEntry(stack, TAG_ITEMS);
				setChanged();
			}
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return !stack.isEmpty();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
	}
}
