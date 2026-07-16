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

import vazkii.botania.api.item.CosmeticAttachable;
import vazkii.botania.api.item.CosmeticBauble;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

public class CosmeticRemoveRecipe extends CustomRecipe {
	public static final RecipeSerializer<CosmeticRemoveRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(CosmeticRemoveRecipe::new);

	@Override
	public RecipeSerializer<CosmeticRemoveRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundAttachable = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof CosmeticAttachable attachable
					&& !(stack.getItem() instanceof CosmeticBauble)
					&& !attachable.getCosmeticItem(stack).isEmpty()
					&& !foundAttachable) {
				foundAttachable = true;
			} else {
				return false;
			}
		}

		return foundAttachable;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack attachableStack = ItemStack.EMPTY;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (!(stack.getItem() instanceof CosmeticAttachable)
					|| stack.getItem() instanceof CosmeticBauble
					|| !attachableStack.isEmpty()) {
				return ItemStack.EMPTY;
			}

			attachableStack = stack;
		}

		if (attachableStack.isEmpty()
				|| !(attachableStack.getItem()
						instanceof CosmeticAttachable attachable)
				|| attachable.getCosmeticItem(attachableStack).isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack result = attachableStack.copyWithCount(1);
		attachable.setCosmeticItem(result, ItemStack.EMPTY);
		return result;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remaining =
				CraftingRecipe.defaultCraftingReminder(input);

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);

			if (stack.getItem() instanceof BaubleItem bauble) {
				ItemStack cosmetic = bauble.getCosmeticItem(stack);

				if (!cosmetic.isEmpty()) {
					remaining.set(i, cosmetic.copyWithCount(1));
				}
			}
		}

		return remaining;
	}
}