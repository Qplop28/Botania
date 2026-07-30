/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.world.WorldOverlays;

/** Keeps Botania's world-last submission inside the main frame-graph pass. */
@Mixin(value = LevelRenderer.class, priority = 900)
public class LevelRendererMixin {
	@Shadow
	@Final
	private RenderBuffers renderBuffers;
	@Shadow
	@Final
	private SubmitNodeStorage submitNodeStorage;
	@Shadow
	@Nullable
	private ClientLevel level;

	@WrapOperation(
			method = "addMainPass",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V")
	)
	private void botania$appendWorldOverlays(FramePass pass, Runnable vanillaPass, Operation<Void> original,
			@Local Matrix4fc modelViewMatrix) {
		original.call(pass, (Runnable) () -> {
			vanillaPass.run();
			Minecraft minecraft = Minecraft.getInstance();
			if (level != null) {
				PoseStack pose = new PoseStack();
				pose.last().pose().set(modelViewMatrix);
				WorldOverlays.renderWorldLast(minecraft.gameRenderer.getMainCamera(), ClientTickHandler.partialTicks,
						pose, renderBuffers, level, submitNodeStorage);
			}
		});
	}
}
