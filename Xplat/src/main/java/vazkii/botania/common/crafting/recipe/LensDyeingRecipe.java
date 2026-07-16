/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.google.common.base.Suppliers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.lens.LensItem;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class LensDyeingRecipe extends CustomRecipe {
	public static final RecipeSerializer<LensDyeingRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(LensDyeingRecipe::new);

	private final Supplier<List<Ingredient>> dyes =
			Suppliers.memoize(() -> Arrays.asList(
					Ingredient.of(Items.WHITE_DYE),
					Ingredient.of(Items.ORANGE_DYE),
					Ingredient.of(Items.MAGENTA_DYE),
					Ingredient.of(Items.LIGHT_BLUE_DYE),
					Ingredient.of(Items.YELLOW_DYE),
					Ingredient.of(Items.LIME_DYE),
					Ingredient.of(Items.PINK_DYE),
					Ingredient.of(Items.GRAY_DYE),
					Ingredient.of(Items.LIGHT_GRAY_DYE),
					Ingredient.of(Items.CYAN_DYE),
					Ingredient.of(Items.PURPLE_DYE),
					Ingredient.of(Items.BLUE_DYE),
					Ingredient.of(Items.BROWN_DYE),
					Ingredient.of(Items.GREEN_DYE),
					Ingredient.of(Items.RED_DYE),
					Ingredient.of(Items.BLACK_DYE),
					Ingredient.of(BotaniaItems.manaPearl)
			));

	@Override
	public RecipeSerializer<LensDyeingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.REDSTONE;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundLens = false;
		boolean foundDye = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof BasicLensItem && !foundLens) {
				foundLens = true;
			} else if (!foundDye && getStackColor(stack) >= 0) {
				foundDye = true;
			} else {
				return false;
			}
		}

		return foundLens && foundDye;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack lens = ItemStack.EMPTY;
		int color = -1;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof BasicLensItem) {
				if (!lens.isEmpty()) {
					return ItemStack.EMPTY;
				}

				lens = stack;
			} else {
				int stackColor = getStackColor(stack);

				if (stackColor < 0 || color >= 0) {
					return ItemStack.EMPTY;
				}

				color = stackColor;
			}
		}

		if (lens.isEmpty() || color < 0) {
			return ItemStack.EMPTY;
		}

		ItemStack result = lens.copyWithCount(1);
		LensItem.setLensColor(result, color);
		return result;
	}

	private int getStackColor(ItemStack stack) {
		List<Ingredient> dyes = this.dyes.get();

		for (int i = 0; i < dyes.size(); i++) {
			if (dyes.get(i).test(stack)) {
				return i;
			}
		}

		return -1;
	}
}