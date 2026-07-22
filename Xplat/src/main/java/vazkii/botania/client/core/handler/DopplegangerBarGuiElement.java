/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.xplat.BotaniaConfig;

/** Extracts Gaia Guardian boss-bar fill quads for Minecraft's deferred GUI renderer. */
public final class DopplegangerBarGuiElement {
	private DopplegangerBarGuiElement() {}

	public static void submit(GuiGraphicsExtractor gui, Identifier texture, int x, int y, int u, int v, int width, int height,
			float grainIntensity, float healthFraction) {
		if (BotaniaConfig.client().useShaders()) {
			gui.blit(CoreShaders.DOPPLEGANGER_BAR, texture, x, y, u, v, width, height, width, height, 256, 256,
					packShaderInputs(grainIntensity, healthFraction));
		} else {
			gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, width, height, 256, 256, 0xFFFFFFFF);
		}
	}

	private static int packShaderInputs(float grainIntensity, float healthFraction) {
		int grain = packUnsigned16(grainIntensity);
		int health = packUnsigned16(healthFraction);
		int red = grain >>> 8;
		int green = grain & 0xFF;
		int blue = health >>> 8;
		int alpha = health & 0xFF;
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private static int packUnsigned16(float value) {
		return Math.min(65535, Math.max(0, Math.round(value * 65535F)));
	}
}
