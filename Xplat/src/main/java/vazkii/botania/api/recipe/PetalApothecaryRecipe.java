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
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;

public interface PetalApothecaryRecipe extends RecipeWithReagent {
	Identifier TYPE_ID = new Identifier(BotaniaAPI.MODID, "petal_apothecary");

	@NotNull
	@Override
	default RecipeType<?> getType() {
		return BuiltInRegistries.RECIPE_TYPE.get(TYPE_ID);
	}
}
