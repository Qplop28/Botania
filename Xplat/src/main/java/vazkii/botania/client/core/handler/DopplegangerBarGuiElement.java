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
import net.minecraft.resources.Identifier;

import vazkii.botania.client.core.helper.CoreShaders;

/** Extracts Gaia Guardian boss-bar fill quads for Minecraft's deferred GUI renderer. */
public final class DopplegangerBarGuiElement {
	private DopplegangerBarGuiElement() {}

	public static void submit(GuiGraphicsExtractor gui, Identifier texture, int x, int y, int u, int v, int width, int height,
			float grainIntensity, float healthFraction) {
		// The GUI extractor snapshots the current transform/scissor into the render state created by blit.
		// Botania's shader-enabled pipeline still consumes GameTime and the doppleganger uniforms declared by
		// the shader resource; the fallback pipeline is RenderPipelines.POSITION_TEX via CoreShaders.
		gui.blit(CoreShaders.dopplegangerBar(), texture, x, y, u, v, width, height, width, height, 256, 256, 0xFFFFFFFF);
	}
}
