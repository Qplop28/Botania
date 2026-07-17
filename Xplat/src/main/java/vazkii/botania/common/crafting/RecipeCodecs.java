/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import net.minecraft.world.item.ItemStackTemplate;

public final class RecipeCodecs {
	/**
	 * Item stack template codec retaining Botania's historical {@code item}
	 * field while Minecraft's codec uses {@code id}.
	 */
	public static final Codec<ItemStackTemplate> ITEM_STACK_TEMPLATE =
			Codec.PASSTHROUGH.comapFlatMap(
					RecipeCodecs::decodeItemStackTemplate,
					RecipeCodecs::encodeItemStackTemplate
			);

	private RecipeCodecs() {}

	private static DataResult<ItemStackTemplate> decodeItemStackTemplate(
			Dynamic<?> dynamic
	) {
		JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
		if (!json.isJsonObject()) {
			return DataResult.error(() -> "Recipe output must be an object");
		}

		JsonObject object = json.getAsJsonObject().deepCopy();
		if (!object.has("id") && object.has("item")) {
			object.add("id", object.remove("item"));
		}
		return ItemStackTemplate.CODEC.parse(JsonOps.INSTANCE, object);
	}

	private static Dynamic<?> encodeItemStackTemplate(ItemStackTemplate template) {
		JsonElement json = ItemStackTemplate.CODEC
				.encodeStart(JsonOps.INSTANCE, template)
				.getOrThrow();
		JsonObject object = json.getAsJsonObject().deepCopy();
		if (object.has("id")) {
			object.add("item", object.remove("id"));
		}
		return new Dynamic<>(JsonOps.INSTANCE, object);
	}
}
