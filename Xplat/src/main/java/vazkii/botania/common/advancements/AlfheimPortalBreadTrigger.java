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
import net.minecraft.server.level.ServerPlayer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class AlfheimPortalBreadTrigger extends ImpossibleTrigger {
	public static final Identifier ID = prefix("alf_portal_bread");
	public static final AlfheimPortalBreadTrigger INSTANCE = new AlfheimPortalBreadTrigger();

	private AlfheimPortalBreadTrigger() {}

	public void trigger(ServerPlayer player, BlockPos portal) {
		// Custom advancement criteria are disabled during the Minecraft 26.1 bootstrap.
	}
}
