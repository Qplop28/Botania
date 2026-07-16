/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/VazkiiMods/Botania
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

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;

public class ManaBlasterRemoveLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<ManaBlasterRemoveLensRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(ManaBlasterRemoveLensRecipe::new);

	@Override
	public RecipeSerializer<ManaBlasterRemoveLensRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundGun = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof ManaBlasterItem
					&& !ManaBlasterItem.getLens(stack).isEmpty()
					&& !foundGun) {
				foundGun = true;
			} else {
				return false;
			}
		}

		return foundGun;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		for (ItemStack stack : input.items()) {
			if (!stack.isEmpty()
					&& stack.getItem() instanceof ManaBlasterItem) {
				ItemStack result = stack.copyWithCount(1);
				ManaBlasterItem.setLens(result, ItemStack.EMPTY);
				return result;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remaining =
				CraftingRecipe.defaultCraftingReminder(input);

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);

			if (stack.is(BotaniaItems.manaGun)) {
				ItemStack lens = ManaBlasterItem.getLens(stack);

				if (!lens.isEmpty()) {
					remaining.set(i, lens.copyWithCount(1));
				}
			}
		}

		return remaining;
	}
}