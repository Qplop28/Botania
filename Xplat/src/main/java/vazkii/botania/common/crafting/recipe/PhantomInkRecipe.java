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

import vazkii.botania.api.item.PhantomInkable;
import vazkii.botania.common.item.BotaniaItems;

public class PhantomInkRecipe extends CustomRecipe {
	public static final RecipeSerializer<PhantomInkRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(PhantomInkRecipe::new);

	@Override
	public RecipeSerializer<PhantomInkRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundInk = false;
		boolean foundItem = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.is(BotaniaItems.phantomInk) && !foundInk) {
				foundInk = true;
			} else if (!foundItem
					&& stack.getItem() instanceof PhantomInkable
					&& stack.getItem().getCraftingRemainder() == null) {
				foundItem = true;
			} else {
				return false;
			}
		}

		return foundInk && foundItem;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		for (ItemStack stack : input.items()) {
			if (!stack.isEmpty()
					&& stack.getItem() instanceof PhantomInkable inkable) {
				ItemStack result = stack.copyWithCount(1);
				inkable.setPhantomInk(
						result,
						!inkable.hasPhantomInk(stack)
				);
				return result;
			}
		}

		return ItemStack.EMPTY;
	}
}