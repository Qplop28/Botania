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

import vazkii.botania.api.item.AncientWillContainer;
import vazkii.botania.common.item.AncientWillItem;

public class AncientWillRecipe extends CustomRecipe {
	public static final RecipeSerializer<AncientWillRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(AncientWillRecipe::new);

	@Override
	public RecipeSerializer<AncientWillRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundWill = false;
		boolean foundContainer = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof AncientWillItem) {
				if (foundWill) {
					return false;
				}

				foundWill = true;
			} else if (stack.getItem() instanceof AncientWillContainer) {
				if (foundContainer) {
					return false;
				}

				foundContainer = true;
			} else {
				return false;
			}
		}

		return foundWill && foundContainer;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack containerStack = ItemStack.EMPTY;
		AncientWillContainer.AncientWillType will = null;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof AncientWillItem willItem) {
				if (will != null) {
					return ItemStack.EMPTY;
				}

				will = willItem.type;
			} else if (stack.getItem() instanceof AncientWillContainer) {
				if (!containerStack.isEmpty()) {
					return ItemStack.EMPTY;
				}

				containerStack = stack;
			} else {
				return ItemStack.EMPTY;
			}
		}

		if (containerStack.isEmpty() || will == null) {
			return ItemStack.EMPTY;
		}

		AncientWillContainer container =
				(AncientWillContainer) containerStack.getItem();

		if (container.hasAncientWill(containerStack, will)) {
			return ItemStack.EMPTY;
		}

		ItemStack result = containerStack.copyWithCount(1);
		container.addAncientWill(result, will);
		return result;
	}
}