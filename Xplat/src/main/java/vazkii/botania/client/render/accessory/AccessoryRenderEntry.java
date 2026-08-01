package vazkii.botania.client.render.accessory;

import net.minecraft.world.item.ItemStack;

public record AccessoryRenderEntry(DeferredAccessoryRenderer renderer, ItemStack stack, AccessoryRenderData data) {}
