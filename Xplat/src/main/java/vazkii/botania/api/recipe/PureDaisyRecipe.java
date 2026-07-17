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
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import java.util.List;
import java.util.Objects;

public interface PureDaisyRecipe extends Recipe<RecipeInput> {
	Identifier TYPE_ID =
			new Identifier(BotaniaAPI.MODID, "pure_daisy");

	/**
	 * This gets called every tick, so implementations should keep their
	 * matching logic lightweight.
	 */
	boolean matches(
			Level level,
			BlockPos pos,
			SpecialFlowerBlockEntity pureDaisy,
			BlockState state
	);

	/**
	 * Returns true if the block was placed, and therefore whether the Pure
	 * Daisy should display its normal completion effects.
	 *
	 * <p>The implementation should only modify the level on the logical
	 * server, but should return true on the client when it would have
	 * succeeded.</p>
	 */
	boolean set(
			Level level,
			BlockPos pos,
			SpecialFlowerBlockEntity pureDaisy
	);

	StateIngredient getInput();

	BlockState getOutputState();

	@Nullable
	CacheableFunction getSuccessFunction();

	int getTime();

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<PureDaisyRecipe> getType() {
		return (RecipeType<PureDaisyRecipe>)
				Objects.requireNonNull(
						BuiltInRegistries.RECIPE_TYPE
								.getValue(TYPE_ID)
				);
	}

	@Override
	default boolean matches(
			RecipeInput input,
			Level level
	) {
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