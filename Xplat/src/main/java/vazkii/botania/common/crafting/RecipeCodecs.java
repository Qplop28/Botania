/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

import net.minecraft.world.item.ItemStackTemplate;

public final class RecipeCodecs {
	/** Botania-compatible item stack template codec. */
	public static final Codec<ItemStackTemplate> ITEM_STACK_TEMPLATE = Codec.of(
			new Encoder<>() {
				@Override
				public <T> DataResult<T> encode(ItemStackTemplate input, DynamicOps<T> ops, T prefix) {
					return ItemStackTemplate.CODEC.encode(input, ops, prefix)
							.flatMap(encoded -> renameField(ops, encoded, "id", "item"));
				}
			},
			new Decoder<>() {
				@Override
				public <T> DataResult<Pair<ItemStackTemplate, T>> decode(DynamicOps<T> ops, T input) {
					return renameField(ops, input, "item", "id")
							.flatMap(normalized -> ItemStackTemplate.CODEC.decode(ops, normalized));
				}
			}
	);

	private RecipeCodecs() {}

	private static <T> DataResult<T> renameField(
			DynamicOps<T> ops, T input, String from, String to
	) {
		var mapResult = ops.getMap(input).result();
		if (mapResult.isEmpty()) {
			return DataResult.success(input);
		}

		var map = mapResult.get();
		// Prefer the target spelling when both are present.
		if (map.get(to) != null || map.get(from) == null) {
			return DataResult.success(input);
		}

		var builder = ops.mapBuilder();
		map.entries().forEach(entry -> {
			var key = ops.getStringValue(entry.getFirst()).result();
			builder.add(
					key.filter(from::equals).isPresent() ? ops.createString(to) : entry.getFirst(),
					entry.getSecond()
			);
		});
		return builder.build(ops.empty());
	}
}
