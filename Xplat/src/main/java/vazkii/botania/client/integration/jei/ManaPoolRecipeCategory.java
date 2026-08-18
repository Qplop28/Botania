/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.lib.LibMisc;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ManaPoolRecipeCategory implements IRecipeCategory<ManaInfusionRecipe> {

	public static final IRecipeType<ManaInfusionRecipe> TYPE =
			IRecipeType.create(LibMisc.MOD_ID, "mana_pool", ManaInfusionRecipe.class);
	private final IDrawable background;
	private final Component localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;
	private final ItemStack renderStack = new ItemStack(BotaniaBlocks.manaPool);

	public ManaPoolRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(142, 55);
		localizedName = Component.translatable("botania.nei.manaPool");
		overlay = guiHelper.createDrawable(prefix("textures/gui/pure_daisy_overlay.png"),
				0, 0, 64, 46);
		ItemNBTHelper.setBoolean(renderStack, "RenderFull", true);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, renderStack.copy());
	}

	@NotNull
	@Override
	public IRecipeType<ManaInfusionRecipe> getRecipeType() {
		return TYPE;
	}

	@NotNull
	@Override
	public Component getTitle() {
		return localizedName;
	}

	@NotNull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public int getWidth() {
		return 142;
	}

	@Override
	public int getHeight() {
		return 55;
	}

	@Override
	public void draw(ManaInfusionRecipe recipe, @NotNull IRecipeSlotsView slotsView, @NotNull GuiGraphicsExtractor gui, double mouseX, double mouseY) {
		background.draw(gui, 0, 0);
		overlay.draw(gui, 40, 0);
		HUDHandler.renderManaBar(gui, 20, 50, 0x0000FF, 0.75F, recipe.getManaToConsume(), ManaPoolBlockEntity.MAX_MANA / 10);
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ManaInfusionRecipe recipe, @NotNull IFocusGroup focusGroup) {
		builder.addSlot(RecipeIngredientRole.INPUT, 32, 12)
				.add(recipe.getIngredients().get(0));

		var catalyst = recipe.getRecipeCatalyst();
		if (catalyst != null) {
			builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 12, 12)
					.addItemStacks(catalyst.getDisplayedStacks())
					.addRichTooltipCallback((view, tooltip) -> tooltip.addAll(catalyst.descriptionTooltip()));
		}

		builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 62, 12).add(renderStack);
		// TODO 1.19.4 figure out the proper way to get a registry access
		builder.addSlot(RecipeIngredientRole.OUTPUT, 93, 12).add(recipe.getResultItem(RegistryAccess.EMPTY));
	}
}
