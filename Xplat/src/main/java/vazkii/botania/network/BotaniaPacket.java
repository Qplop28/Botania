package vazkii.botania.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public interface BotaniaPacket extends CustomPacketPayload {
	static <T extends BotaniaPacket> CustomPacketPayload.Type<T> type(Identifier id) {
		return new CustomPacketPayload.Type<>(id);
	}

	static <T extends BotaniaPacket> StreamCodec<RegistryFriendlyByteBuf, T> codec(
			Function<RegistryFriendlyByteBuf, T> decoder) {
		return StreamCodec.of(
				(buf, packet) -> packet.encode(buf),
				decoder::apply
		);
	}

	@Override
	default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return BotaniaPacket.type(getFabricId());
	}

	void encode(RegistryFriendlyByteBuf buf);

	/**
	 * Stable custom payload identifier.
	 *
	 * <p>The method retains its old name for source compatibility while the
	 * Minecraft 26.1 port is in progress.
	 */
	Identifier getFabricId();
}
