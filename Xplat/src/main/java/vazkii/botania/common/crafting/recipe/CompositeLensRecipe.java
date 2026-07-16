/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.CompositableLensItem;
import vazkii.botania.common.lib.BotaniaTags;

public class CompositeLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<CompositeLensRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(CompositeLensRecipe::new);

	@Override
	public RecipeSerializer<CompositeLensRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.REDSTONE;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		int lensCount = 0;
		boolean foundGlue = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof CompositableLensItem) {
				lensCount++;

				if (lensCount > 2) {
					return false;
				}
			} else if (stack.is(BotaniaTags.Items.LENS_GLUE)
					&& !foundGlue) {
				foundGlue = true;
			} else {
				return false;
			}
		}

		return lensCount == 2 && foundGlue;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack firstLens = ItemStack.EMPTY;
		ItemStack secondLens = ItemStack.EMPTY;
		boolean foundGlue = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof CompositableLensItem) {
				if (firstLens.isEmpty()) {
					firstLens = stack;
				} else if (secondLens.isEmpty()) {
					secondLens = stack;
				} else {
					return ItemStack.EMPTY;
				}
			} else if (stack.is(BotaniaTags.Items.LENS_GLUE)
					&& !foundGlue) {
				foundGlue = true;
			} else {
				return ItemStack.EMPTY;
			}
		}

		if (!foundGlue
				|| firstLens.isEmpty()
				|| secondLens.isEmpty()
				|| !(firstLens.getItem()
						instanceof CompositableLensItem lensItem)
				|| !lensItem.canCombineLenses(
						firstLens,
						secondLens
				)
				|| !lensItem.getCompositeLens(firstLens).isEmpty()
				|| !lensItem.getCompositeLens(secondLens).isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack result = firstLens.copyWithCount(1);
		ItemStack composite = secondLens.copyWithCount(1);

		return lensItem.setCompositeLens(result, composite);
	}
}