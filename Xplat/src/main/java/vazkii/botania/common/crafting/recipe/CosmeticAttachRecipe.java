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

import vazkii.botania.api.item.CosmeticAttachable;
import vazkii.botania.api.item.CosmeticBauble;

public class CosmeticAttachRecipe extends CustomRecipe {
	public static final RecipeSerializer<CosmeticAttachRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(CosmeticAttachRecipe::new);

	@Override
	public RecipeSerializer<CosmeticAttachRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundCosmetic = false;
		boolean foundAttachable = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof CosmeticBauble) {
				if (foundCosmetic) {
					return false;
				}

				foundCosmetic = true;
			} else if (stack.getItem() instanceof CosmeticAttachable attachable
					&& attachable.getCosmeticItem(stack).isEmpty()) {
				if (foundAttachable) {
					return false;
				}

				foundAttachable = true;
			} else {
				return false;
			}
		}

		return foundCosmetic && foundAttachable;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack cosmeticStack = ItemStack.EMPTY;
		ItemStack attachableStack = ItemStack.EMPTY;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof CosmeticBauble) {
				if (!cosmeticStack.isEmpty()) {
					return ItemStack.EMPTY;
				}

				cosmeticStack = stack;
			} else if (stack.getItem() instanceof CosmeticAttachable) {
				if (!attachableStack.isEmpty()) {
					return ItemStack.EMPTY;
				}

				attachableStack = stack;
			} else {
				return ItemStack.EMPTY;
			}
		}

		if (cosmeticStack.isEmpty()
				|| attachableStack.isEmpty()
				|| !(attachableStack.getItem()
						instanceof CosmeticAttachable attachable)
				|| !attachable.getCosmeticItem(attachableStack).isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack result = attachableStack.copyWithCount(1);
		attachable.setCosmeticItem(
				result,
				cosmeticStack.copyWithCount(1)
		);
		return result;
	}
}