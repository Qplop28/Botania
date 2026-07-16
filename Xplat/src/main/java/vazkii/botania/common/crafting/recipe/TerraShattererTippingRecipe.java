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

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;

public class TerraShattererTippingRecipe extends CustomRecipe {
	public static final RecipeSerializer<TerraShattererTippingRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(TerraShattererTippingRecipe::new);

	@Override
	public RecipeSerializer<TerraShattererTippingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundTerraPick = false;
		boolean foundElementiumPick = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.getItem() instanceof TerraShattererItem
					&& !TerraShattererItem.isTipped(stack)
					&& !foundTerraPick) {
				foundTerraPick = true;
			} else if (stack.is(BotaniaItems.elementiumPick)
					&& !foundElementiumPick) {
				foundElementiumPick = true;
			} else {
				return false;
			}
		}

		return foundTerraPick && foundElementiumPick;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		for (ItemStack stack : input.items()) {
			if (!stack.isEmpty()
					&& stack.getItem() instanceof TerraShattererItem) {
				ItemStack result = stack.copy();
				TerraShattererItem.setTipped(result);
				return result;
			}
		}

		return ItemStack.EMPTY;
	}
}