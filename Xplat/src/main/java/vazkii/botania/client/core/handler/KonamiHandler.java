/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import org.lwjgl.glfw.GLFW;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.LexicaBotaniaItem;

public class KonamiHandler {
	private static final int[] KONAMI_CODE = {
			GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_UP,
			GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_DOWN,
			GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT,
			GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT,
			GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_A,
	};
	private static int nextLetter = 0;
	private static int konamiTime = 0;

	public static void clientTick(Minecraft client) {
		if (konamiTime > 0) {
			konamiTime--;
		}

		if (!LexicaBotaniaItem.isOpen()) {
			nextLetter = 0;
		}
	}

	public static void handleInput(int key, int action, int modifiers) {
		Minecraft mc = Minecraft.getInstance();
		if (modifiers == 0 && action == GLFW.GLFW_PRESS && LexicaBotaniaItem.isOpen()) {
			if (konamiTime == 0 && key == KONAMI_CODE[nextLetter]) {
				nextLetter++;
				if (nextLetter >= KONAMI_CODE.length) {
					mc.getSoundManager().play(SimpleSoundInstance.forUI(BotaniaSounds.way, 1.0F));
					nextLetter = 0;
					konamiTime = 240;
				}
			} else {
				nextLetter = 0;
			}
		}
	}
}
