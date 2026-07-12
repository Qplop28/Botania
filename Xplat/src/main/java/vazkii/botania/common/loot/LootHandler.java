/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.equipment.tool.elementium.ElementiumAxeItem;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.function.Consumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public final class LootHandler {
	public static final Identifier GOG_SEEDS_TABLE =
			Identifier.fromNamespaceAndPath(
					BotaniaAPI.GOG_MODID,
					"extra_seeds"
			);

	public static void lootLoad(
			Identifier id,
			Consumer<LootPool.Builder> addPool) {
		String prefix = "minecraft:chests/";
		String name = id.toString();

		if (name.startsWith(prefix)) {
			String file =
					name.substring(
							name.indexOf(prefix)
									+ prefix.length()
					);

			switch (file) {
				case "abandoned_mineshaft":
				case "desert_pyramid":
				case "jungle_temple":
				case "simple_dungeon":
				case "spawn_bonus_chest":
				case "stronghold_corridor":
					addPool.accept(
							getInjectPool(file)
					);
					break;
				case "village/village_temple":
				case "village/village_toolsmith":
				case "village/village_weaponsmith":
					addPool.accept(
							getInjectPool(
									"village_chest"
							)
					);
					break;
				default:
					break;
			}
		} else if (id.getPath()
				.startsWith("entities/")) {
			addPool.accept(
					LootPool.lootPool()
							.add(
									NestedLootTable
											.lootTableReference(
													lootTableKey(
															ElementiumAxeItem
																	.BEHEADING_LOOT_TABLE
													)
											)
							)
			);
		} else if (
				XplatAbstractions.INSTANCE.gogLoaded()
						&& (
								isLootTable(
										Blocks.GRASS,
										id
								)
										|| isLootTable(
												Blocks.TALL_GRASS,
												id
										)
						)
		) {
			addPool.accept(
					LootPool.lootPool()
							.add(
									NestedLootTable
											.lootTableReference(
													lootTableKey(
															GOG_SEEDS_TABLE
													)
											)
							)
			);
		}
	}

	private static LootPool.Builder getInjectPool(
			String entryName) {
		return LootPool.lootPool()
				.add(
						getInjectEntry(
								entryName,
								1
						)
				)
				.setBonusRolls(
						UniformGenerator.between(
								0,
								1
						)
				);
	}

	private static LootPoolEntryContainer.Builder<?>
			getInjectEntry(
					String name,
					int weight) {
		ResourceKey<LootTable> table =
				lootTableKey(
						prefix(
								"inject/" + name
						)
				);

		return NestedLootTable
				.lootTableReference(table)
				.setWeight(weight);
	}

	private static ResourceKey<LootTable> lootTableKey(
			Identifier id) {
		return ResourceKey.create(
				Registries.LOOT_TABLE,
				id
		);
	}

	private static boolean isLootTable(
			Block block,
			Identifier id) {
		return block.getLootTable()
				.map(ResourceKey::identifier)
				.filter(id::equals)
				.isPresent();
	}
}