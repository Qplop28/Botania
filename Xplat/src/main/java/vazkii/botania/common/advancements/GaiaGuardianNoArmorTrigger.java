/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.advancements;

import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import vazkii.botania.common.entity.GaiaGuardianEntity;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class GaiaGuardianNoArmorTrigger extends ImpossibleTrigger {
	public static final Identifier ID = prefix("gaia_guardian_no_armor");
	public static final GaiaGuardianNoArmorTrigger INSTANCE = new GaiaGuardianNoArmorTrigger();

	private GaiaGuardianNoArmorTrigger() {}

	public void trigger(ServerPlayer player, GaiaGuardianEntity guardian, DamageSource source) {
		// Custom advancement criteria are disabled during the Minecraft 26.1 bootstrap.
	}
}
