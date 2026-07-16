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
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BlackHoleTalismanItem;
import vazkii.botania.common.item.BotaniaItems;

public class BlackHoleTalismanExtractRecipe extends CustomRecipe {
	public static final RecipeSerializer<BlackHoleTalismanExtractRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(BlackHoleTalismanExtractRecipe::new);

	@Override
	public RecipeSerializer<BlackHoleTalismanExtractRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundTalisman = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.is(BotaniaItems.blackHoleTalisman)
					&& !foundTalisman) {
				if (BlackHoleTalismanItem.getBlockCount(stack) <= 0) {
					return false;
				}

				foundTalisman = true;
			} else {
				return false;
			}
		}

		return foundTalisman;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack talisman = ItemStack.EMPTY;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (!stack.is(BotaniaItems.blackHoleTalisman)
					|| !talisman.isEmpty()) {
				return ItemStack.EMPTY;
			}

			talisman = stack;
		}

		if (talisman.isEmpty()) {
			return ItemStack.EMPTY;
		}

		int count = BlackHoleTalismanItem.getBlockCount(talisman);
		Block block = BlackHoleTalismanItem.getBlock(talisman);

		if (count <= 0 || block == null) {
			return ItemStack.EMPTY;
		}

		return new ItemStack(block, Math.min(64, count));
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remaining =
				CraftingRecipe.defaultCraftingReminder(input);

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);

			if (!stack.is(BotaniaItems.blackHoleTalisman)) {
				continue;
			}

			int count = BlackHoleTalismanItem.getBlockCount(stack);

			if (count <= 0) {
				continue;
			}

			int extract = Math.min(64, count);
			ItemStack talisman = stack.copyWithCount(1);

			BlackHoleTalismanItem.remove(talisman, extract);
			ItemNBTHelper.setBoolean(
					talisman,
					BlackHoleTalismanItem.TAG_ACTIVE,
					false
			);

			remaining.set(i, talisman);
		}

		return remaining;
	}
}