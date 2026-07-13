/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.brew.effect.BloodthirstMobEffect;

@Mixin(SpawnPlacements.class)
public class SpawnPlacementsMixin {
	// This injection is kind of scuffed:
	// In vanilla code, canSpawnEntity calls a per-entity predicate to determine spawnability.
	// Many hostile mobs register their own spawn predicates or fall back to a standard
	// monster spawn check, which ordinarily should be jumped over under Bloodthirst.
	// However, certain mobs use the predicate for additional logic, such as slime chunks,
	// and Bloodthirst jumps over that logic as well.
	@Inject(
			method = "checkSpawnRules",
			at = @At("RETURN"),
			cancellable = true
	)
	private static <T extends Entity> void bloodthirstOverride(
			EntityType<T> type,
			ServerLevelAccessor world,
			EntitySpawnReason reason,
			BlockPos position,
			RandomSource random,
			CallbackInfoReturnable<Boolean> cir
	) {
		if (reason == EntitySpawnReason.NATURAL
				&& BloodthirstMobEffect.overrideSpawn(world, position, type.getCategory())) {
			cir.setReturnValue(true);
		}
	}
}