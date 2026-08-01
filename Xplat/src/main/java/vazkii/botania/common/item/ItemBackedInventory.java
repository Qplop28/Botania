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
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.api.BotaniaAPI;

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
				NonNullList<ItemStack> migrated = NonNullList.withSize(expectedSize, ItemStack.EMPTY);
				for (int i = 0; i < expectedSize && i < legacy.size(); i++) {
					var payload = legacy.getCompound(i);
					if (payload.isEmpty()) {
						BotaniaAPI.LOGGER.error("Could not read legacy item-backed inventory slot {}: {}", i, legacy.get(i));
						return;
					}
					if (payload.get().isEmpty()) {
						continue;
					}
					var decoded = ItemNBTHelper.decodeStoredStack(payload.get()).result();
					if (decoded.isEmpty()) {
						BotaniaAPI.LOGGER.error("Could not migrate legacy item-backed inventory slot {}: {}", i, payload.get());
						return;
					}
					migrated.set(i, decoded.get());
				}
				for (int i = 0; i < expectedSize; i++) {
					getItems().set(i, migrated.get(i));
				}
				stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(migrated));
				ItemNBTHelper.removeEntry(stack, TAG_ITEMS);
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
