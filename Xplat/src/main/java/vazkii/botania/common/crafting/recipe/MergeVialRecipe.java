/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.item.brew.BaseBrewItem;

public class MergeVialRecipe extends CustomRecipe {
	public static final RecipeSerializer<MergeVialRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(MergeVialRecipe::new);

	@Override
	public RecipeSerializer<MergeVialRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		int count = 0;
		Brew brew = null;
		boolean foundBrew = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (!(stack.getItem() instanceof BaseBrewItem brewItem)) {
				return false;
			}

			Brew currentBrew = brewItem.getBrew(stack);

			if (!foundBrew) {
				brew = currentBrew;
				foundBrew = true;
			} else if (brew != currentBrew) {
				return false;
			}

			count++;
		}

		return count > 1;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack result = ItemStack.EMPTY;
		BaseBrewItem resultItem = null;
		Brew brew = null;
		boolean foundBrew = false;
		int swigs = 0;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (!(stack.getItem() instanceof BaseBrewItem brewItem)) {
				return ItemStack.EMPTY;
			}

			Brew currentBrew = brewItem.getBrew(stack);

			if (!foundBrew) {
				brew = currentBrew;
				foundBrew = true;
			} else if (brew != currentBrew) {
				return ItemStack.EMPTY;
			}

			if (resultItem == null) {
				result = stack.copyWithCount(1);
				resultItem = brewItem;
			}

			swigs += brewItem.getSwigsLeft(stack);

			if (swigs >= resultItem.getSwigs()) {
				swigs = resultItem.getSwigs();
				break;
			}
		}

		if (resultItem == null || result.isEmpty()) {
			return ItemStack.EMPTY;
		}

		resultItem.setSwigsLeft(result, swigs);
		return result;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remaining =
				NonNullList.withSize(input.size(), ItemStack.EMPTY);

		boolean foundFirst = false;
		int swigs = 0;
		int maxSwigs = 0;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);

			if (stack.isEmpty()) {
				continue;
			}

			if (!(stack.getItem() instanceof BaseBrewItem brewItem)) {
				continue;
			}

			if (!foundFirst) {
				foundFirst = true;
				swigs = brewItem.getSwigsLeft(stack);
				maxSwigs = brewItem.getSwigs();
				continue;
			}

			swigs += brewItem.getSwigsLeft(stack);

			if (swigs > maxSwigs) {
				ItemStack leftover = stack.copyWithCount(1);
				brewItem.setSwigsLeft(
						leftover,
						swigs - maxSwigs
				);
				swigs = maxSwigs;
				remaining.set(i, leftover);
			} else {
				remaining.set(i, brewItem.getBaseStack());
			}
		}

		return remaining;
	}
}