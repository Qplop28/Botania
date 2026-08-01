package vazkii.botania.client.render.accessory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import java.util.List;

public final class AccessoryRenderExtraction {
	private AccessoryRenderExtraction() {}

	public static void extract(Player player, float partialTicks, AccessoryExtractionContext context,
			List<AccessoryRenderEntry> output) {
		Container worn = EquipmentHandler.getAllWorn(player);
		for (int slot = 0; slot < worn.getContainerSize(); slot++) {
			ItemStack source = worn.getItem(slot).copy();
			if (source.isEmpty() || !(source.getItem() instanceof BaubleItem bauble)) {
				continue;
			}
			if (!bauble.hasRender(source, player)) {
				continue;
			}

			ItemStack cosmetic = bauble.getCosmeticItem(source);
			ItemStack rendered = cosmetic.isEmpty() ? source : cosmetic.copy();
			DeferredAccessoryRenderer renderer = AccessoryRenderRegistry.getDeferred(rendered);
			if (renderer == null) {
				continue;
			}

			AccessoryRenderData data = renderer.createData();
			renderer.extract(data, rendered, player, partialTicks, context);
			output.add(new AccessoryRenderEntry(renderer, rendered.copy(), data));
		}
	}
}
