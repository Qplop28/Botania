/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.gui.box;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import vazkii.botania.client.lib.ResourcesLib;

public class BaubleBoxGui extends AbstractContainerScreen<BaubleBoxContainer> {

	private static final Identifier texture = Identifier.parse(ResourcesLib.GUI_BAUBLE_BOX);
	public BaubleBoxGui(BaubleBoxContainer container, Inventory player, Component title) {
		super(container, player, title);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor gui, int x, int y) {
		// No-op, there's no space for gui titles
	}

	@Override
	protected void extractBackground(GuiGraphicsExtractor gui, float partialTick, int mouseX, int mouseY) {
		gui.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0,
				imageWidth, imageHeight, imageWidth, imageHeight, 256, 256, 0xFFFFFFFF);
		InventoryScreen.extractEntityInInventoryFollowsMouse(gui,
				leftPos + 7, topPos + 8, leftPos + 55, topPos + 78, 30, 0.0625F,
				mouseX, mouseY, minecraft.player);
	}

}
