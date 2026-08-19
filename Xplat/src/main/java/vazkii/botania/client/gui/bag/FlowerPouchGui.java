/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.gui.bag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import vazkii.botania.client.lib.ResourcesLib;

public class FlowerPouchGui extends AbstractContainerScreen<FlowerPouchContainer> {

	private static final Identifier texture = Identifier.parse(ResourcesLib.GUI_FLOWER_BAG);

	public FlowerPouchGui(FlowerPouchContainer container, Inventory playerInv, Component title) {
		super(container, playerInv, title, 176, 202);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
		int k = (width - imageWidth) / 2;
		int l = (height - imageHeight) / 2;
		gui.blit(RenderPipelines.GUI_TEXTURED, texture, k, l, 0, 0,
				imageWidth, imageHeight, imageWidth, imageHeight, 256, 256, 0xFFFFFFFF);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
		super.extractLabels(gui, mouseX, mouseY);
		Minecraft mc = Minecraft.getInstance();

		for (Slot slot : menu.slots) {
			if (slot.container == menu.flowerBagInv
					&& slot.hasItem()
					&& slot.getItem().getCount() == 1) {
				int x = slot.x;
				int y = slot.y;

				// Always draw the count even at 1
				gui.text(
						mc.font,
						"1",
						x + 11,
						y + 9,
						0xFFFFFF,
						true
				);
			}
		}
	}

}
