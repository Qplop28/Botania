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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class AlfheimPortalTrigger extends ImpossibleTrigger {
	public static final Identifier ID = prefix("open_elf_portal");
	public static final AlfheimPortalTrigger INSTANCE = new AlfheimPortalTrigger();

	private AlfheimPortalTrigger() {}

	public void trigger(ServerPlayer player, ServerLevel world, BlockPos pos, ItemStack wand) {
		// Custom advancement criteria are disabled during the Minecraft 26.1 bootstrap.
	}
}
