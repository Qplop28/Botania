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
import net.minecraft.world.item.ItemStack;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class RelicBindTrigger extends ImpossibleTrigger {
	public static final Identifier ID = prefix("relic_bind");
	public static final RelicBindTrigger INSTANCE = new RelicBindTrigger();

	private RelicBindTrigger() {}

	public void trigger(ServerPlayer player, ItemStack relic) {
		// Custom advancement criteria are disabled during the Minecraft 26.1 bootstrap.
	}
}
