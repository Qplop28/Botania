/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.mixin.client;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.core.SkyblockWorldInfo;
import vazkii.botania.client.render.world.SkyblockSkyRenderer;
import vazkii.botania.xplat.BotaniaConfig;

/** Adds the Garden sky immediately after vanilla submits its celestial geometry. */
@Mixin(value = SkyRenderer.class, priority = 900)
public class SkyRendererMixin {
	@Inject(
			method = "renderSky(Lnet/minecraft/client/renderer/state/level/SkyRenderState;Lnet/minecraft/client/Camera;FLcom/mojang/blaze3d/textures/GpuTextureView;Lcom/mojang/blaze3d/textures/GpuTextureView;)V",
			at = @At("TAIL")
	)
	private void botania$renderGardenSky(SkyRenderState state, Camera camera, float partialTick,
			GpuTextureView colorTarget, GpuTextureView depthTarget, CallbackInfo ci) {
		var level = Minecraft.getInstance().level;
		if (level != null && botania$isGardenSky(level)) {
			PoseStack pose = new PoseStack();
			SkyblockSkyRenderer.renderExtra(pose, level, partialTick, 0);
			SkyblockSkyRenderer.renderStars(pose, level, partialTick);
		}
	}

	@Unique
	private static boolean botania$isGardenSky(Level level) {
		boolean garden = level.getLevelData() instanceof SkyblockWorldInfo info && info.isGardenOfGlass();
		return BotaniaConfig.client().enableFancySkybox() && level.dimension() == Level.OVERWORLD
				&& (BotaniaConfig.client().enableFancySkyboxInNormalWorlds() || garden);
	}
}
