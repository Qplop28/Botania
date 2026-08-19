package vazkii.botania.common.helper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import vazkii.botania.mixin.BundleItemAccessor;

import java.util.ArrayList;
import java.util.List;

public class FilterHelper {

	public static List<ItemStack> getFilterItems(ItemFrame filterFrame) {
		ItemStack filterStack = filterFrame.getItem();
		if (filterStack.isEmpty()) {
			return List.of();
		}
		return filterFrame instanceof GlowItemFrame ? getFilterStacks(filterStack) : List.of(filterStack);
	}

	/**
	 * Expands the given filter item into the list of filter items it represents. This is NOT recursive.
	 * Non-empty container items stand for their contents, not for themselves.
	 */
	public static List<ItemStack> getFilterStacks(ItemStack filterStack) {
		if (filterStack.is(Items.BUNDLE)) {
			// get bundle content
			List<ItemStack> bundledItems = BundleItemAccessor.call_getContents(filterStack).toList();
			if (!bundledItems.isEmpty()) {
				return bundledItems;
			}
		} else {
			// Vanilla block containers and Botania's item-backed inventories both use this component.
			ItemContainerContents contents = filterStack.get(DataComponents.CONTAINER);
			if (contents != null) {
				List<ItemStack> items = contents.stream().filter(stack -> !stack.isEmpty()).toList();
				if (!items.isEmpty()) {
					return items;
				}
			}
			// Item-backed inventories from before components are upgraded when opened, but
			// filters must also understand unopened stacks without mutating them.
			var legacy = ItemNBTHelper.getList(filterStack, "Items", Tag.TAG_COMPOUND, true);
			if (legacy != null) {
				var items = new ArrayList<ItemStack>();
				for (int i = 0; i < legacy.size(); i++) {
					legacy.getCompound(i)
							.flatMap(tag -> ItemNBTHelper.decodeStoredStack(tag).result())
							.filter(stack -> !stack.isEmpty())
							.ifPresent(items::add);
				}
				if (!items.isEmpty()) {
					return List.copyOf(items);
				}
			}
		}
		return List.of(filterStack);
	}

	public record WeightedItemStack(ItemStack stack, int weight) {
		public static WeightedItemStack of(ItemStack stack, int weight) {
			return new WeightedItemStack(stack, weight);
		}
	}
}
