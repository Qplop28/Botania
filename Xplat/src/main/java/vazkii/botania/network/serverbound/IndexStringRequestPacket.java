package vazkii.botania.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.network.BotaniaPacket;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public record IndexStringRequestPacket(String message) implements BotaniaPacket {
	public static final Identifier ID = prefix("idxs");

	public static IndexStringRequestPacket decode(RegistryFriendlyByteBuf buf) {
		return new IndexStringRequestPacket(buf.readUtf());
	}

	@Override
	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeUtf(message);
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	public void handle(MinecraftServer server, ServerPlayer player) {
		server.execute(() -> CorporeaIndexBlockEntity.onChatMessage(player, message()));
	}
}
