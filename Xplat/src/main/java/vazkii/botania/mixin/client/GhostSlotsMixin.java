/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.core.RecipeBookAccess;

@Mixin(GhostSlots.class)
public class GhostSlotsMixin implements RecipeBookAccess {
	@Unique
	private ItemStack botania$hoveredGhostRecipeStack;

	@Override
	public ItemStack getHoveredGhostRecipeStack() {
		return botania$hoveredGhostRecipeStack;
	}

	@Inject(
			method = "extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/Minecraft;IILnet/minecraft/world/inventory/Slot;)V",
			at = @At("HEAD")
	)
	private void botania$clearHoveredGhostRecipeStack(
			GuiGraphicsExtractor graphics,
			Minecraft minecraft,
			int mouseX,
			int mouseY,
			Slot hoveredSlot,
			CallbackInfo ci
	) {
		botania$hoveredGhostRecipeStack = null;
	}

	@ModifyVariable(
			method = "extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/Minecraft;IILnet/minecraft/world/inventory/Slot;)V",
			at = @At("STORE"),
			ordinal = 0,
			require = 1
	)
	private ItemStack botania$captureHoveredGhostRecipeStack(ItemStack stack) {
		botania$hoveredGhostRecipeStack = stack;
		return stack;
	}
}
