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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import vazkii.botania.common.item.BotaniaItems;

public class SpellbindingClothRecipe extends CustomRecipe {
	public static final RecipeSerializer<SpellbindingClothRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(SpellbindingClothRecipe::new);

	@Override
	public RecipeSerializer<SpellbindingClothRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundCloth = false;
		boolean foundEnchanted = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.isEnchanted() && !foundEnchanted) {
				foundEnchanted = true;
			} else if (stack.is(BotaniaItems.spellCloth)
					&& !foundCloth) {
				foundCloth = true;
			} else {
				return false;
			}
		}

		return foundCloth && foundEnchanted;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		for (ItemStack stack : input.items()) {
			if (!stack.isEmpty()
					&& stack.isEnchanted()
					&& !stack.is(BotaniaItems.spellCloth)) {
				ItemStack result = stack.copyWithCount(1);

				EnchantmentHelper.updateEnchantments(
						result,
						enchantments ->
								enchantments.removeIf(enchantment -> true)
				);
				result.set(DataComponents.REPAIR_COST, 0);

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

			if (stack.is(BotaniaItems.spellCloth)) {
				ItemStack cloth = stack.copyWithCount(1);
				cloth.setDamageValue(cloth.getDamageValue() + 1);
				remaining.set(i, cloth);
			}
		}

		return remaining;
	}
}