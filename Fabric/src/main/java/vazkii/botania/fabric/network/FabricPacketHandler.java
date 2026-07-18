/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import vazkii.botania.network.BotaniaPacket;
import vazkii.botania.network.TriConsumer;
import vazkii.botania.network.clientbound.*;
import vazkii.botania.network.serverbound.*;

import java.util.function.Consumer;
import java.util.function.Function;

public final class FabricPacketHandler {
	private static final CustomPacketPayload.Type<DodgePacket> DODGE =
			BotaniaPacket.type(DodgePacket.ID);
	private static final CustomPacketPayload.Type<IndexKeybindRequestPacket> INDEX_KEYBIND_REQUEST =
			BotaniaPacket.type(IndexKeybindRequestPacket.ID);
	private static final CustomPacketPayload.Type<IndexStringRequestPacket> INDEX_STRING_REQUEST =
			BotaniaPacket.type(IndexStringRequestPacket.ID);
	private static final CustomPacketPayload.Type<JumpPacket> JUMP =
			BotaniaPacket.type(JumpPacket.ID);
	private static final CustomPacketPayload.Type<LeftClickPacket> LEFT_CLICK =
			BotaniaPacket.type(LeftClickPacket.ID);

	private static final CustomPacketPayload.Type<AvatarSkiesRodPacket> AVATAR_SKIES_ROD =
			BotaniaPacket.type(AvatarSkiesRodPacket.ID);
	private static final CustomPacketPayload.Type<BotaniaEffectPacket> BOTANIA_EFFECT =
			BotaniaPacket.type(BotaniaEffectPacket.ID);
	private static final CustomPacketPayload.Type<GogWorldPacket> GOG_WORLD =
			BotaniaPacket.type(GogWorldPacket.ID);
	private static final CustomPacketPayload.Type<ItemAgePacket> ITEM_AGE =
			BotaniaPacket.type(ItemAgePacket.ID);
	private static final CustomPacketPayload.Type<SpawnGaiaGuardianPacket> SPAWN_GAIA_GUARDIAN =
			BotaniaPacket.type(SpawnGaiaGuardianPacket.ID);
	private static final CustomPacketPayload.Type<UpdateItemsRemainingPacket> UPDATE_ITEMS_REMAINING =
			BotaniaPacket.type(UpdateItemsRemainingPacket.ID);

	public static void init() {
		registerServerbound(
				DODGE,
				DodgePacket::decode,
				DodgePacket::handle
		);
		registerServerbound(
				INDEX_KEYBIND_REQUEST,
				IndexKeybindRequestPacket::decode,
				IndexKeybindRequestPacket::handle
		);
		registerServerbound(
				INDEX_STRING_REQUEST,
				IndexStringRequestPacket::decode,
				IndexStringRequestPacket::handle
		);
		registerServerbound(
				JUMP,
				JumpPacket::decode,
				JumpPacket::handle
		);
		registerServerbound(
				LEFT_CLICK,
				LeftClickPacket::decode,
				LeftClickPacket::handle
		);

		registerClientboundType(
				AVATAR_SKIES_ROD,
				AvatarSkiesRodPacket::decode
		);
		registerClientboundType(
				BOTANIA_EFFECT,
				BotaniaEffectPacket::decode
		);
		registerClientboundType(
				GOG_WORLD,
				GogWorldPacket::decode
		);
		registerClientboundType(
				ITEM_AGE,
				ItemAgePacket::decode
		);
		registerClientboundType(
				SPAWN_GAIA_GUARDIAN,
				SpawnGaiaGuardianPacket::decode
		);
		registerClientboundType(
				UPDATE_ITEMS_REMAINING,
				UpdateItemsRemainingPacket::decode
		);
	}

	private static <T extends BotaniaPacket> void registerServerbound(
			CustomPacketPayload.Type<T> type,
			Function<RegistryFriendlyByteBuf, T> decoder,
			TriConsumer<T, MinecraftServer, ServerPlayer> handler) {
		PayloadTypeRegistry.serverboundPlay().register(
				type,
				BotaniaPacket.codec(decoder)
		);

		ServerPlayNetworking.registerGlobalReceiver(type, (packet, context) ->
				handler.accept(
						packet,
						context.server(),
						context.player()
				)
		);
	}

	private static <T extends BotaniaPacket> void registerClientboundType(
			CustomPacketPayload.Type<T> type,
			Function<RegistryFriendlyByteBuf, T> decoder) {
		PayloadTypeRegistry.clientboundPlay().register(
				type,
				BotaniaPacket.codec(decoder)
		);
	}

	public static void initClient() {
		registerClientboundReceiver(
				AVATAR_SKIES_ROD,
				AvatarSkiesRodPacket.Handler::handle
		);
		registerClientboundReceiver(
				BOTANIA_EFFECT,
				BotaniaEffectPacket.Handler::handle
		);
		registerClientboundReceiver(
				GOG_WORLD,
				GogWorldPacket.Handler::handle
		);
		registerClientboundReceiver(
				ITEM_AGE,
				ItemAgePacket.Handler::handle
		);
		registerClientboundReceiver(
				SPAWN_GAIA_GUARDIAN,
				SpawnGaiaGuardianPacket.Handler::handle
		);
		registerClientboundReceiver(
				UPDATE_ITEMS_REMAINING,
				UpdateItemsRemainingPacket.Handler::handle
		);
	}

	private static <T extends BotaniaPacket> void registerClientboundReceiver(
			CustomPacketPayload.Type<T> type,
			Consumer<T> handler) {
		ClientPlayNetworking.registerGlobalReceiver(type, (packet, context) ->
				handler.accept(packet)
		);
	}

	private FabricPacketHandler() {}
}