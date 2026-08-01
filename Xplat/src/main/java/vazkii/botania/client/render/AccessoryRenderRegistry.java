package vazkii.botania.client.render;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.render.accessory.DeferredAccessoryRenderer;

import java.util.HashMap;
import java.util.Map;

public class AccessoryRenderRegistry {
	private static final Map<Item, DeferredAccessoryRenderer> DEFERRED_REGISTRATIONS = new HashMap<>();

	public static void registerDeferred(Item item, DeferredAccessoryRenderer renderer) {
		DEFERRED_REGISTRATIONS.put(item, renderer);
	}

	@Nullable
	public static DeferredAccessoryRenderer getDeferred(ItemStack stack) {
		return DEFERRED_REGISTRATIONS.get(stack.getItem());
	}
}
