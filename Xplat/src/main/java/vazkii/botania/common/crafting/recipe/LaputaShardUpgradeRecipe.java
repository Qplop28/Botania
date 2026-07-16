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
import vazkii.botania.common.item.LaputaShardItem;

public class LaputaShardUpgradeRecipe extends CustomRecipe {
	public static final RecipeSerializer<LaputaShardUpgradeRecipe> SERIALIZER =
			NoOpRecipeSerializer.create(LaputaShardUpgradeRecipe::new);

	@Override
	public RecipeSerializer<LaputaShardUpgradeRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundShard = false;
		boolean foundLifeEssence = false;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.is(BotaniaItems.laputaShard)
					&& !foundShard
					&& LaputaShardItem.getShardLevel(stack) < 19) {
				foundShard = true;
			} else if (stack.is(BotaniaItems.lifeEssence)
					&& !foundLifeEssence) {
				foundLifeEssence = true;
			} else {
				return false;
			}
		}

		return foundShard && foundLifeEssence;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		for (ItemStack stack : input.items()) {
			if (!stack.is(BotaniaItems.laputaShard)) {
				continue;
			}

			int shardLevel = LaputaShardItem.getShardLevel(stack);

			if (shardLevel >= 19) {
				return ItemStack.EMPTY;
			}

			ItemStack result = stack.copyWithCount(1);
			ItemNBTHelper.setInt(
					result,
					LaputaShardItem.TAG_LEVEL,
					shardLevel + 1
			);
			return result;
		}

		return ItemStack.EMPTY;
	}
}