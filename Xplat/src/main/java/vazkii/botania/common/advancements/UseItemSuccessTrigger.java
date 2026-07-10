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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class UseItemSuccessTrigger extends ImpossibleTrigger {
	public static final Identifier ID = prefix("use_item_success");
	public static final UseItemSuccessTrigger INSTANCE = new UseItemSuccessTrigger();

	private UseItemSuccessTrigger() {}

	public void trigger(ServerPlayer player, ItemStack stack, ServerLevel world,
			double x, double y, double z) {
		// Custom advancement criteria are disabled during the Minecraft 26.1 bootstrap.
	}
}
