/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;

import java.util.List;
import java.util.Objects;

public interface OrechidRecipe extends Recipe<RecipeInput> {
	Identifier TYPE_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "orechid");
	Identifier IGNEM_TYPE_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "orechid_ignem");
	Identifier MARIMORPHOSIS_TYPE_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "marimorphosis");

	StateIngredient getInput();

	StateIngredient getOutput();

	default StateIngredient getOutput(@NotNull Level level, @NotNull BlockPos pos) {
		return getOutput();
	}

	int getWeight();

	default int getWeight(@NotNull Level level, @NotNull BlockPos pos) {
		return getWeight();
	}

	@Nullable
	CacheableFunction getSuccessFunction();

	@Override
	@SuppressWarnings("unchecked")
	default RecipeType<? extends OrechidRecipe> getType() {
		return (RecipeType<? extends OrechidRecipe>) Objects.requireNonNull(
				BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	@Override
	default boolean matches(RecipeInput input, Level level) {
		return false;
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

	@Override
	default boolean isSpecial() {
		return true;
	}
}
