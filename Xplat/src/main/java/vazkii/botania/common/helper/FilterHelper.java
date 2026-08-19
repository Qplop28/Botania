package vazkii.botania.common.helper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import vazkii.botania.mixin.BundleItemAccessor;

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
				List<ItemStack> items = contents.streamNonEmpty().toList();
				if (!items.isEmpty()) {
					return items;
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
