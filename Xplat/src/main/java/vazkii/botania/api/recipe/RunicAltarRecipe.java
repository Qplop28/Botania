/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.Objects;

public interface RunicAltarRecipe extends RecipeWithReagent {
	Identifier TYPE_ID = Identifier.fromNamespaceAndPath(
			BotaniaAPI.MODID,
			"runic_altar"
	);

	// TODO: read from recipe definition
	@Override
	default Ingredient getReagent() {
		return Ingredient.of(BotaniaBlocks.livingrock);
	}

	int getManaUsage();

	@Override
	@SuppressWarnings("unchecked")
	default RecipeType<RunicAltarRecipe> getType() {
		return (RecipeType<RunicAltarRecipe>) Objects.requireNonNull(
				BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}
}
