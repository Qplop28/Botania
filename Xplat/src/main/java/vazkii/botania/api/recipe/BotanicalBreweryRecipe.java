/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;

import java.util.List;
import java.util.Objects;

public interface BotanicalBreweryRecipe extends Recipe<RecipeInput> {
	Identifier TYPE_ID = Identifier.fromNamespaceAndPath(
			BotaniaAPI.MODID,
			"brew"
	);

	Brew getBrew();

	int getManaUsage();

	ItemStack getOutput(ItemStack container);

	@Override
	@SuppressWarnings("unchecked")
	default RecipeType<BotanicalBreweryRecipe> getType() {
		return (RecipeType<BotanicalBreweryRecipe>) Objects.requireNonNull(
				BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	@Deprecated
	default ItemStack getResultItem(RegistryAccess registries) {
		return ItemStack.EMPTY;
	}

	@Override
	default ItemStack assemble(RecipeInput input) {
		return ItemStack.EMPTY;
	}

	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.create(List.<Ingredient>of());
	}

	@Override
	default boolean showNotification() {
		return false;
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	/** Transitional ingredient accessor for existing integrations. */
	@Deprecated
	NonNullList<Ingredient> getIngredients();

	@Override
	default boolean isSpecial() {
		return true;
	}
}
