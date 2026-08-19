/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import vazkii.botania.client.gui.ManaBarTooltipComponent;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsFabricMixin {
	@Redirect(
			method = "renderTooltipInternal",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"
			)
	)
	private Vector2ic positionManaBarTooltip(ClientTooltipPositioner positioner,
			int screenWidth, int screenHeight, int oldX, int oldY, int width, int height,
			Font font, List<ClientTooltipComponent> components) {
		Vector2ic position = positioner.positionTooltip(screenWidth, screenHeight, oldX, oldY, width, height);
		for (ClientTooltipComponent component : components) {
			if (component instanceof ManaBarTooltipComponent manaBar) {
				manaBar.setContext(position.x(), position.y(), width);
			}
		}
		return position;
	}
}
