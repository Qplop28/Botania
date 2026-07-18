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
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.lib.LibMisc;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class PureDaisyRecipeCategory implements IRecipeCategory<PureDaisyRecipe> {

	public static final IRecipeType<PureDaisyRecipe> TYPE = IRecipeType.create(LibMisc.MOD_ID, "pure_daisy", PureDaisyRecipe.class);
	private final IDrawable background;
	private final Component localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;
	private final IPlatformFluidHelper<?> fluidHelper;

	public PureDaisyRecipeCategory(IGuiHelper guiHelper, IPlatformFluidHelper<?> fluidHelper) {
		background = guiHelper.createBlankDrawable(96, 44);
		localizedName = Component.translatable("botania.nei.pureDaisy");
		overlay = guiHelper.createDrawable(prefix("textures/gui/pure_daisy_overlay.png"),
				0, 0, 64, 44);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BotaniaFlowerBlocks.pureDaisy));
		this.fluidHelper = fluidHelper;
	}

	@NotNull
	@Override
	public IRecipeType<PureDaisyRecipe> getRecipeType() {
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
		return 96;
	}

	@Override
	public int getHeight() {
		return 44;
	}

	@Override
	public void draw(PureDaisyRecipe recipe, IRecipeSlotsView slotsView, GuiGraphicsExtractor gui, double mouseX, double mouseY) {
		background.draw(gui, 0, 0);
		overlay.draw(gui, 17, 0);
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull PureDaisyRecipe recipe, @NotNull IFocusGroup focusGroup) {
		StateIngredient input = recipe.getInput();

		IRecipeSlotBuilder inputSlotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 9, 12)
				.setFluidRenderer(1000, false, 16, 16);
		for (var state : input.getDisplayed()) {
			if (!state.getFluidState().isEmpty()) {
				addFluid(inputSlotBuilder, this.fluidHelper, state.getFluidState().getType().builtInRegistryHolder(), 1000);
			}
		}
		inputSlotBuilder.addItemStacks(input.getDisplayedStacks())
				.addTooltipCallback((view, tooltip) -> tooltip.addAll(input.descriptionTooltip()));

		builder.addSlot(RecipeIngredientRole.CATALYST, 39, 12)
				.add(new ItemStack(BotaniaFlowerBlocks.pureDaisy));

		Block outBlock = recipe.getOutputState().getBlock();
		FluidState outFluid = outBlock.defaultBlockState().getFluidState();
		if (!outFluid.isEmpty()) {
			addFluid(builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
					.setFluidRenderer(1000, false, 16, 16), this.fluidHelper, outFluid.getType().builtInRegistryHolder(), 1000);
		} else {
			if (outBlock.asItem() != Items.AIR) {
				builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
						.add(new ItemStack(outBlock));
			}
		}
	}

	private static <T> void addFluid(IRecipeSlotBuilder slot, IPlatformFluidHelper<T> fluidHelper, Holder<Fluid> fluid, long amount) {
		slot.add(fluidHelper.getFluidIngredientType(), fluidHelper.create(fluid, amount));
	}
}
