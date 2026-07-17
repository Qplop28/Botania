/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.render.world.WorldOverlays;

/** Keeps Botania's world-last submission at the end of level extraction. */
@Mixin(value = LevelRenderer.class, priority = 900)
public class LevelRendererMixin {
	@Shadow
	@Final
	private RenderBuffers renderBuffers;
	@Shadow
	@Nullable
	private ClientLevel level;

	@Inject(method = "renderLevel", at = @At("TAIL"))
	private void botania$renderOverlays(CallbackInfo ci) {
		var minecraft = net.minecraft.client.Minecraft.getInstance();
		Camera camera = minecraft.gameRenderer.getMainCamera();
		WorldOverlays.renderWorldLast(camera, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false),
				new PoseStack(), renderBuffers, level);
	}
}
