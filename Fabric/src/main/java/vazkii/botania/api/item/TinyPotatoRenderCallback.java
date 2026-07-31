/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.item;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Why would you ever want this ._.
 */
public interface TinyPotatoRenderCallback {
	Event<TinyPotatoRenderCallback> EVENT = EventFactory.createArrayBacked(TinyPotatoRenderCallback.class,
			listeners -> (pos, name, contributor, tickDelta, poseStack, collector, light, overlay) -> {
				for (TinyPotatoRenderCallback listener : listeners) {
					listener.onRender(pos, name, contributor, tickDelta, poseStack, collector, light, overlay);
				}
			});

	void onRender(BlockPos pos, Component name, String contributor, float tickDelta,
			PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay);
}
