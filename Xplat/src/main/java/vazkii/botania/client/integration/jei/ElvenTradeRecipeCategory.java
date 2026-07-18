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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.ElvenTradeRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.lib.LibMisc;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ElvenTradeRecipeCategory implements IRecipeCategory<ElvenTradeRecipe> {

	public static final IRecipeType<ElvenTradeRecipe> TYPE = IRecipeType.create(LibMisc.MOD_ID, "elven_trade", ElvenTradeRecipe.class);
	private final Component localizedName;
	private final IDrawable background;
	private final IDrawable overlay;
	private final IDrawable icon;

	public ElvenTradeRecipeCategory(IGuiHelper guiHelper) {
		localizedName = Component.translatable("botania.nei.elvenTrade");
		background = guiHelper.createBlankDrawable(145, 95);
		overlay = guiHelper.createDrawable(prefix("textures/gui/elven_trade_overlay.png"), 0, 15, 140, 90);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BotaniaBlocks.alfPortal));
	}

	@NotNull
	@Override
	public IRecipeType<ElvenTradeRecipe> getRecipeType() {
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
		return 145;
	}

	@Override
	public int getHeight() {
		return 95;
	}

	@Override
	public void draw(@NotNull ElvenTradeRecipe recipe, @NotNull IRecipeSlotsView slotsView, @NotNull GuiGraphicsExtractor gui, double mouseX, double mouseY) {
		background.draw(gui, 0, 0);
		overlay.draw(gui, 0, 4);
		TextureAtlasSprite sprite = Minecraft.getInstance()
				.getAtlasManager()
				.get(
						new SpriteId(
								TextureAtlas.LOCATION_BLOCKS,
								prefix(
										"block/alfheim_portal_swirl"
								)
						)
				);
		gui.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				sprite,
				22,
				25,
				48,
				48
		);
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ElvenTradeRecipe recipe, @NotNull IFocusGroup focusGroup) {
		int posX = 42;
		for (var ingr : recipe.getIngredients()) {
			builder.addSlot(RecipeIngredientRole.INPUT, posX, 0)
					.add(ingr);
			posX += 18;
		}

		int outIdx = 0;
		for (var stack : recipe.getOutputs()) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, 93 + outIdx % 2 * 20, 41 + outIdx / 2 * 20)
					.add(stack);
			outIdx++;
		}
	}
}
