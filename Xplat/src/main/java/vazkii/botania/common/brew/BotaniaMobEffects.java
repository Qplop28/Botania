/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.brew;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

import vazkii.botania.common.brew.effect.*;
import vazkii.botania.common.lib.LibPotionNames;

import java.util.function.BiConsumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class BotaniaMobEffects {

	public static final MobEffect soulCross = new SoulCrossMobEffect();
	public static final MobEffect featherfeet = new FeatherfeetMobEffect();
	public static final MobEffect emptiness = new EmptinessMobEffect();
	public static final MobEffect bloodthrst = new BloodthirstMobEffect();
	public static final MobEffect allure = new AllureMobEffect();
	public static final MobEffect clear = new AbsolutionMobEffect();

	// Mob effect registries are intrusive, so these are the same reference holders
	// that registration binds below rather than unbound direct holders.
	public static final Holder<MobEffect> soulCrossHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(soulCross);
	public static final Holder<MobEffect> featherfeetHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(featherfeet);
	public static final Holder<MobEffect> emptinessHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(emptiness);
	public static final Holder<MobEffect> bloodthrstHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(bloodthrst);
	public static final Holder<MobEffect> allureHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(allure);
	public static final Holder<MobEffect> clearHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(clear);

	public static void registerPotions(BiConsumer<MobEffect, Identifier> r) {
		r.accept(soulCross, prefix(LibPotionNames.SOUL_CROSS));
		r.accept(featherfeet, prefix(LibPotionNames.FEATHER_FEET));
		r.accept(emptiness, prefix(LibPotionNames.EMPTINESS));
		r.accept(bloodthrst, prefix(LibPotionNames.BLOODTHIRST));
		r.accept(allure, prefix(LibPotionNames.ALLURE));
		r.accept(clear, prefix(LibPotionNames.CLEAR));
	}
}
