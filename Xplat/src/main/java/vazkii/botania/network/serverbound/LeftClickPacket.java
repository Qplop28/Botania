/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import vazkii.botania.common.item.equipment.tool.terrasteel.TerraBladeItem;
import vazkii.botania.network.BotaniaPacket;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class LeftClickPacket implements BotaniaPacket {
	public static final LeftClickPacket INSTANCE = new LeftClickPacket();
	public static final Identifier ID = prefix("lc");

	@Override
	public void encode(RegistryFriendlyByteBuf buf) {

	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	public static LeftClickPacket decode(RegistryFriendlyByteBuf buf) {
		return INSTANCE;
	}

	public void handle(MinecraftServer server, ServerPlayer player) {
		// The swing packet will run on the netty thread immediately,
		// so we need to fetch the attack strength ahead of time
		float scale = player.getAttackStrengthScale(0F);
		server.execute(() -> TerraBladeItem.trySpawnBurst(player, scale));
	}
}
