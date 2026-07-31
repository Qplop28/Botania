package vazkii.botania.client.render.accessory;

import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.item.ItemModelResolver;

public record AccessoryExtractionContext(ItemModelResolver itemModels, BlockModelResolver blockModels) {}
