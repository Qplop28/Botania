/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SimpleInventoryBlockEntity extends BotaniaBlockEntity implements Clearable {

	private final SimpleContainer itemHandler = new ChangeTrackedContainer(createItemHandler());

	protected SimpleInventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	private final class ChangeTrackedContainer extends SimpleContainer {
		private final SimpleContainer delegate;

		private ChangeTrackedContainer(SimpleContainer delegate) {
			super(0);
			this.delegate = delegate;
		}

		@Override
		public ItemStack getItem(int slot) {
			return delegate.getItem(slot);
		}

		@Override
		public java.util.List<ItemStack> removeAllItems() {
			var removed = delegate.removeAllItems();
			setChanged();
			return removed;
		}

		@Override
		public ItemStack removeItem(int slot, int amount) {
			ItemStack removed = delegate.removeItem(slot, amount);
			if (!removed.isEmpty()) {
				setChanged();
			}
			return removed;
		}

		@Override
		public ItemStack removeItemType(net.minecraft.world.item.Item item, int amount) {
			ItemStack removed = delegate.removeItemType(item, amount);
			if (!removed.isEmpty()) {
				setChanged();
			}
			return removed;
		}

		@Override
		public ItemStack addItem(ItemStack stack) {
			int oldCount = stack.getCount();
			ItemStack remainder = delegate.addItem(stack);
			if (remainder.getCount() != oldCount) {
				setChanged();
			}
			return remainder;
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			return delegate.removeItemNoUpdate(slot);
		}

		@Override
		public void setItem(int slot, ItemStack stack) {
			delegate.setItem(slot, stack);
			setChanged();
		}

		@Override
		public void setChanged() {
			delegate.setChanged();
			SimpleInventoryBlockEntity.this.setChanged();
		}

		@Override
		public int getContainerSize() {
			return delegate.getContainerSize();
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public boolean stillValid(net.minecraft.world.entity.player.Player player) {
			return delegate.stillValid(player);
		}

		@Override
		public void clearContent() {
			delegate.clearContent();
			setChanged();
		}

		@Override
		public int getMaxStackSize() {
			return delegate.getMaxStackSize();
		}

		@Override
		public boolean canPlaceItem(int slot, ItemStack stack) {
			return delegate.canPlaceItem(slot, stack);
		}
	}

	private static void copyToInv(NonNullList<ItemStack> src, Container dest) {
		Preconditions.checkArgument(src.size() == dest.getContainerSize());
		for (int i = 0; i < src.size(); i++) {
			dest.setItem(i, src.get(i));
		}
	}

	private static NonNullList<ItemStack> copyFromInv(Container inv) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ret.set(i, inv.getItem(i));
		}
		return ret;
	}

	@Override
	public void readPacketNBT(CompoundTag tag) {
		NonNullList<ItemStack> tmp = NonNullList.withSize(inventorySize(), ItemStack.EMPTY);
		tag.getList("Items").ifPresent(items -> {
			for (Tag element : items) {
				if (element instanceof CompoundTag itemTag) {
					int slot = itemTag.getByte("Slot").orElse((byte) -1) & 255;
					if (slot < tmp.size()) {
						ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag)
								.result().ifPresent(stack -> tmp.set(slot, stack));
					}
				}
			}
		});
		copyToInv(tmp, itemHandler);
	}

	@Override
	public void writePacketNBT(CompoundTag tag) {
		ListTag items = new ListTag();
		NonNullList<ItemStack> inventory = copyFromInv(itemHandler);
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.get(slot);
			if (!stack.isEmpty()) {
				int slotIndex = slot;
				ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).result().ifPresent(encoded -> {
					if (encoded instanceof CompoundTag itemTag) {
						itemTag.putByte("Slot", (byte) slotIndex);
						items.add(itemTag);
					}
				});
			}
		}
		tag.put("Items", items);
	}

	// NB: Cannot be named the same as the corresponding method in vanilla's interface -- causes obf issues with MCP
	public final int inventorySize() {
		return getItemHandler().getContainerSize();
	}

	protected abstract SimpleContainer createItemHandler();

	@Override
	public void clearContent() {
		getItemHandler().clearContent();
	}

	public final Container getItemHandler() {
		return itemHandler;
	}
}
