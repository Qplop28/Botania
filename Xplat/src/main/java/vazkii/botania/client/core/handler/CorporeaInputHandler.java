/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.core.RecipeBookAccess;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.mixin.client.AbstractContainerScreenAccessor;
import vazkii.botania.mixin.client.AbstractRecipeBookScreenAccessor;
import vazkii.botania.mixin.client.RecipeBookComponentAccessor;
import vazkii.botania.mixin.client.RecipeBookPageAccessor;
import vazkii.botania.network.serverbound.IndexKeybindRequestPacket;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

public class CorporeaInputHandler {

	public static final List<Supplier<ItemStack>> hoveredStackGetters = new ArrayList<>();
	public static Predicate<Screen> supportedGuiFilter = gui -> gui instanceof AbstractContainerScreen<?>;

	public static boolean buttonPressed(int keyCode, int scanCode) {
		Minecraft mc = Minecraft.getInstance();
		KeyEvent event = createKeyEvent(mc, keyCode, scanCode);

		if (mc.level == null || !supportedGuiFilter.test(mc.screen)
				|| !ClientProxy.CORPOREA_REQUEST.matches(event)
				|| CorporeaIndexBlockEntity.getNearbyValidIndexes(mc.player).isEmpty()) {
			return false;
		}

		ItemStack stack = getStackUnderMouse();
		if (stack != null && !stack.isEmpty()) {
			int count = 1;
			int max = stack.getMaxStackSize();

			if (event.hasShiftDown()) {
				count = max;
				if (event.hasControlDownWithQuirk()) {
					count /= 4;
				}
			} else if (event.hasControlDownWithQuirk()) {
				count = max / 2;
			}

			if (count > 0) {
				ItemStack requested = stack.copyWithCount(count);
				ClientXplatAbstractions.INSTANCE.sendToServer(new IndexKeybindRequestPacket(requested));
				return true;
			}
		}
		return false;
	}

	private static KeyEvent createKeyEvent(Minecraft mc, int keyCode, int scanCode) {
		int modifiers = 0;

		if (isEitherKeyDown(mc, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT)) {
			modifiers |= GLFW.GLFW_MOD_SHIFT;
		}
		if (isEitherKeyDown(mc, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL)) {
			modifiers |= GLFW.GLFW_MOD_CONTROL;
		}
		if (isEitherKeyDown(mc, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT)) {
			modifiers |= GLFW.GLFW_MOD_ALT;
		}
		if (isEitherKeyDown(mc, GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER)) {
			modifiers |= GLFW.GLFW_MOD_SUPER;
		}

		return new KeyEvent(keyCode, scanCode, modifiers);
	}

	private static boolean isEitherKeyDown(Minecraft mc, int leftKey, int rightKey) {
		return InputConstants.isKeyDown(mc.getWindow(), leftKey)
				|| InputConstants.isKeyDown(mc.getWindow(), rightKey);
	}

	private static ItemStack getStackUnderMouse() {
		Screen screen = Minecraft.getInstance().screen;
		if (screen instanceof AbstractContainerScreen<?>) {
			Slot slotUnderMouse = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
			if (slotUnderMouse != null && slotUnderMouse.hasItem()) {
				return slotUnderMouse.getItem().copy();
			}

			if (screen instanceof AbstractRecipeBookScreen<?> recipeScreen) {
				RecipeBookComponent<?> recipeBook = ((AbstractRecipeBookScreenAccessor) recipeScreen)
						.botania$getRecipeBookComponent();

				if (recipeBook.isVisible()) {
					RecipeBookPage page = ((RecipeBookComponentAccessor) recipeBook).getRecipesArea();
					RecipeButton widget = ((RecipeBookPageAccessor) page).getHoveredButton();

					if (widget != null) {
						return widget.getDisplayStack().copy();
					}

					ItemStack stack = ((RecipeBookAccess) recipeBook).getHoveredGhostRecipeStack();
					if (stack != null) {
						return stack;
					}
				}
			}
		}

		for (var getter : CorporeaInputHandler.hoveredStackGetters) {
			ItemStack stack = getter.get();
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}
}
