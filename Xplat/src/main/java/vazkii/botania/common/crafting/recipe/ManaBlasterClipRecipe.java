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

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;

public class ManaBlasterClipRecipe extends CustomRecipe {
	public static final RecipeSerializer<ManaBlasterClipRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(ManaBlasterClipRecipe::new);

	@Override
	public RecipeSerializer<ManaBlasterClipRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundGun = false;
		boolean foundClip = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof ManaBlasterItem
					&& !ManaBlasterItem.hasClip(stack)
					&& !foundGun) {
				foundGun = true;
			} else if (stack.is(BotaniaItems.clip) && !foundClip) {
				foundClip = true;
			} else {
				return false;
			}
		}

		return foundGun && foundClip;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		for (ItemStack stack : input.items()) {
			if (!stack.isEmpty()
					&& stack.getItem() instanceof ManaBlasterItem) {
				ItemStack result = stack.copyWithCount(1);
				ItemStack lens = ManaBlasterItem.getLens(stack);

				ManaBlasterItem.setLens(result, ItemStack.EMPTY);
				ManaBlasterItem.setClip(result, true);
				ManaBlasterItem.setLensAtPos(result, lens, 0);

				return result;
			}
		}

		return ItemStack.EMPTY;
	}
}