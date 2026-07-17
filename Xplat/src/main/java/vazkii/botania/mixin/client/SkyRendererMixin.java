/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import vazkii.botania.client.core.SkyblockWorldInfo;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.world.SkyblockSkyRenderer;
import vazkii.botania.xplat.BotaniaConfig;

/** Owns the Garden renderer for exactly the lifetime of its vanilla SkyRenderer. */
@Mixin(value = SkyRenderer.class, priority = 900)
public class SkyRendererMixin {
	@Unique
	private SkyblockSkyRenderer botania$gardenRenderer;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void botania$createGardenResources(CallbackInfo ci) {
		botania$gardenRenderer = new SkyblockSkyRenderer();
	}

	@Inject(method = "close", at = @At("HEAD"))
	private void botania$closeGardenResources(CallbackInfo ci) {
		if (botania$gardenRenderer != null) {
			botania$gardenRenderer.close();
			botania$gardenRenderer = null;
		}
	}

	@Inject(
			method = "renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;FFFLnet/minecraft/world/level/MoonPhase;FF)V",
			at = @At("TAIL")
	)
	private void botania$renderGardenSky(PoseStack pose, float sunAngle, float moonAngle, float starAngle,
			MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
		var minecraft = Minecraft.getInstance();
		var level = minecraft.level;
		if (botania$gardenRenderer != null && level != null && botania$isGardenSky(level)) {
			float partialTick = ClientTickHandler.partialTicks;
			float insideVoid = Mth.clamp((float) (level.getMinY() - minecraft.gameRenderer.getMainCamera().position().y) / 32F, 0F, 1F);
			botania$gardenRenderer.renderExtra(pose, level, partialTick, insideVoid);
			botania$gardenRenderer.renderStars(pose, level, partialTick);
		}
	}

	@ModifyArgs(
			method = "renderSun(FLcom/mojang/blaze3d/vertex/PoseStack;)V",
			at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;scale(FFF)Lorg/joml/Matrix4fStack;")
	)
	private void botania$scaleSun(Args args) {
		if (botania$isGardenSky(Minecraft.getInstance().level)) {
			args.set(0, 60F);
			args.set(2, 60F);
		}
	}

	@ModifyArgs(
			method = "renderMoon(Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V",
			at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;scale(FFF)Lorg/joml/Matrix4fStack;")
	)
	private void botania$scaleMoon(Args args) {
		if (botania$isGardenSky(Minecraft.getInstance().level)) {
			args.set(0, 30F);
			args.set(2, 30F);
		}
	}

	@Unique
	private static boolean botania$isGardenSky(Level level) {
		if (level == null) {
			return false;
		}
		boolean garden = level.getLevelData() instanceof SkyblockWorldInfo info && info.isGardenOfGlass();
		return BotaniaConfig.client().enableFancySkybox() && level.dimension() == Level.OVERWORLD
				&& (BotaniaConfig.client().enableFancySkyboxInNormalWorlds() || garden);
	}
}
