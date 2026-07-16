/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/VazkiiMods/Botania
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

import vazkii.botania.common.item.ManaBlasterItem;

public class ManaBlasterLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<ManaBlasterLensRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(ManaBlasterLensRecipe::new);

	@Override
	public RecipeSerializer<ManaBlasterLensRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundLens = false;
		boolean foundGun = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof ManaBlasterItem
					&& ManaBlasterItem.getLens(stack).isEmpty()
					&& !foundGun) {
				foundGun = true;
			} else if (ManaBlasterItem.isValidLens(stack)
					&& !foundLens) {
				foundLens = true;
			} else {
				return false;
			}
		}

		return foundLens && foundGun;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack lens = ItemStack.EMPTY;
		ItemStack gun = ItemStack.EMPTY;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof ManaBlasterItem) {
				gun = stack;
			} else if (ManaBlasterItem.isValidLens(stack)) {
				lens = stack.copyWithCount(1);
			}
		}

		if (lens.isEmpty() || gun.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack result = gun.copyWithCount(1);
		ManaBlasterItem.setLens(result, lens);
		return result;
	}
}