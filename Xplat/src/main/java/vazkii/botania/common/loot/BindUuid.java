/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;

public class BindUuid extends LootItemConditionalFunction {
	public static final MapCodec<BindUuid> MAP_CODEC =
			RecordCodecBuilder.mapCodec(
					instance ->
							commonFields(instance)
									.apply(instance, BindUuid::new)
			);

	protected BindUuid(
			List<LootItemCondition> conditions) {
		super(conditions);
	}

	@Override
	public MapCodec<BindUuid> codec() {
		return MAP_CODEC;
	}

	@NotNull
	@Override
	protected ItemStack run(
			@NotNull ItemStack stack,
			@NotNull LootContext context) {
		if (context.getOptionalParameter(
				LootContextParams.ATTACKING_ENTITY
		) instanceof Player player) {
			var relic =
					XplatAbstractions.INSTANCE
							.findRelic(stack);

			if (relic != null) {
				relic.bindToUUID(
						player.getUUID()
				);
			}
		}

		return stack;
	}
}