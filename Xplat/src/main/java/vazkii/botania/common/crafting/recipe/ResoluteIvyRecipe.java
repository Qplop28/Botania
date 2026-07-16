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

import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ResoluteIvyItem;

public class ResoluteIvyRecipe extends CustomRecipe {
	public static final RecipeSerializer<ResoluteIvyRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(ResoluteIvyRecipe::new);

	@Override
	public RecipeSerializer<ResoluteIvyRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundIvy = false;
		boolean foundItem = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.is(BotaniaItems.keepIvy) && !foundIvy) {
				foundIvy = true;
			} else if (!foundItem
					&& !ItemNBTHelper.getBoolean(
							stack,
							ResoluteIvyItem.TAG_KEEP,
							false
					)
					&& stack.getItem().getCraftingRemainder() == null) {
				foundItem = true;
			} else {
				return false;
			}
		}

		return foundIvy && foundItem;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		for (ItemStack stack : input.items()) {
			if (!stack.isEmpty() && !stack.is(BotaniaItems.keepIvy)) {
				ItemStack result = stack.copyWithCount(1);
				ItemNBTHelper.setBoolean(
						result,
						ResoluteIvyItem.TAG_KEEP,
						true
				);
				return result;
			}
		}

		return ItemStack.EMPTY;
	}
}