/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.crafting;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.crafting.recipe.TerraShattererTippingRecipe;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;

import java.util.Collections;
import java.util.List;

public class TerraShattererTippingRecipeWrapper implements ICraftingCategoryExtension<TerraShattererTippingRecipe> {
	@Override
	public List<SlotDisplay> getIngredients(@NotNull RecipeHolder<TerraShattererTippingRecipe> recipeHolder) {
		return List.of(
				new SlotDisplay.ItemSlotDisplay(BotaniaItems.terraPick),
				new SlotDisplay.ItemSlotDisplay(BotaniaItems.elementiumPick)
		);
	}

	@Override
	public void setRecipe(@NotNull RecipeHolder<TerraShattererTippingRecipe> recipeHolder, @NotNull IRecipeLayoutBuilder builder, @NotNull ICraftingGridHelper helper, @NotNull IFocusGroup focuses) {
		var inputs = List.of(List.of(new ItemStack(BotaniaItems.terraPick)), List.of(new ItemStack(BotaniaItems.elementiumPick)));
		var output = new ItemStack(BotaniaItems.terraPick);
		TerraShattererItem.setTipped(output);

		helper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputs, 0, 0);
		helper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, Collections.singletonList(output));
	}
}
