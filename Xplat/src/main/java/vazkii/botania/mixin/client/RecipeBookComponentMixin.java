/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import vazkii.botania.client.core.RecipeBookAccess;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin implements RecipeBookAccess {
	@Shadow
	@Final
	private GhostSlots ghostSlots;

	@Override
	public ItemStack getHoveredGhostRecipeStack() {
		return ((RecipeBookAccess) ghostSlots).getHoveredGhostRecipeStack();
	}
}
