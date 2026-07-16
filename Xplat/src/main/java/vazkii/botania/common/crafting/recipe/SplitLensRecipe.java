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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.BasicLensItem;

public class SplitLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<SplitLensRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(SplitLensRecipe::new);

	@Override
	public RecipeSerializer<SplitLensRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.REDSTONE;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return !assemble(input).isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack compositeLens = ItemStack.EMPTY;

		for (ItemStack candidate : input.items()) {
			if (candidate.isEmpty()) {
				continue;
			}

			if (!compositeLens.isEmpty()) {
				return ItemStack.EMPTY;
			}

			compositeLens = getComposite(candidate);

			if (compositeLens.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}

		return compositeLens.isEmpty()
				? ItemStack.EMPTY
				: compositeLens.copyWithCount(1);
	}

	private ItemStack getComposite(ItemStack stack) {
		Item item = stack.getItem();

		if (!(item instanceof BasicLensItem basicLens)) {
			return ItemStack.EMPTY;
		}

		return basicLens.getCompositeLens(stack);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remaining =
				CraftingRecipe.defaultCraftingReminder(input);

		for (int i = 0; i < input.size(); i++) {
			ItemStack candidate = input.getItem(i);

			if (candidate.getItem() instanceof BasicLensItem basicLens) {
				ItemStack separatedLens = candidate.copyWithCount(1);
				separatedLens = basicLens.setCompositeLens(
						separatedLens,
						ItemStack.EMPTY
				);
				remaining.set(i, separatedLens);
			}
		}

		return remaining;
	}
}