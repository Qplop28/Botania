/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.impl;

import com.google.common.base.Suppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaRegistries;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.configdata.ConfigDataManager;
import vazkii.botania.api.corporea.CorporeaNodeDetector;
import vazkii.botania.api.internal.ManaNetwork;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.flower.functional.SolegnoliaBlockEntity;
import vazkii.botania.common.config.ConfigDataManagerImpl;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.handler.ManaNetworkHandler;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.relic.RingOfLokiItem;
import vazkii.botania.common.lib.BotaniaTags;
import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

	public class BotaniaAPIImpl implements BotaniaAPI {

		private static final Supplier<ArmorMaterial> MANASTEEL_ARMOR_MATERIAL =
			armorMaterial(
					"manasteel",
					16,
					armorDefense(2, 5, 6, 2),
					18,
					() -> BotaniaSounds.equipManasteel,
					0.0F,
					BotaniaTags.Items.INGOTS_MANASTEEL
			);

	private static final Supplier<ArmorMaterial> MANAWEAVE_ARMOR_MATERIAL =
			armorMaterial(
					"manaweave",
					5,
					armorDefense(1, 2, 3, 1),
					18,
					() -> BotaniaSounds.equipManaweave,
					0.0F,
					BotaniaTags.Items.MANAWEAVE_CLOTH
			);

	private static final Supplier<ArmorMaterial> ELEMENTIUM_ARMOR_MATERIAL =
			armorMaterial(
					"elementium",
					18,
					armorDefense(2, 5, 6, 2),
					18,
					() -> BotaniaSounds.equipElementium,
					0.0F,
					BotaniaTags.Items.INGOTS_ELEMENTIUM
			);

	private static final Supplier<ArmorMaterial> TERRASTEEL_ARMOR_MATERIAL =
			armorMaterial(
					"terrasteel",
					34,
					armorDefense(3, 6, 8, 3),
					26,
					() -> BotaniaSounds.equipTerrasteel,
					3.0F,
					BotaniaTags.Items.INGOTS_TERRASTEEL
			);

	private static Supplier<ArmorMaterial> armorMaterial(
			String name,
			int durability,
			Map<ArmorType, Integer> defense,
			int enchantmentValue,
			Supplier<SoundEvent> equipSound,
			float toughness,
			TagKey<Item> repairIngredient) {
		return Suppliers.memoize(() -> new ArmorMaterial(
				durability,
				defense,
				enchantmentValue,
				BuiltInRegistries.SOUND_EVENT.wrapAsHolder(equipSound.get()),
				toughness,
				0.0F,
				repairIngredient,
				equipmentAsset(name)
		));
	}

	private static Map<ArmorType, Integer> armorDefense(
			int boots,
			int leggings,
			int chestplate,
			int helmet) {
		return Map.of(
				ArmorType.BOOTS, boots,
				ArmorType.LEGGINGS, leggings,
				ArmorType.CHESTPLATE, chestplate,
				ArmorType.HELMET, helmet
		);
	}

	private static ResourceKey<EquipmentAsset> equipmentAsset(String name) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, prefix(name));
	}

	private static final ToolMaterial MANASTEEL_ITEM_MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
			300,
			6.2F,
			2,
			20,
			BotaniaTags.Items.INGOTS_MANASTEEL
	);

	private static final ToolMaterial ELEMENTIUM_ITEM_MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
			720,
			6.2F,
			2,
			20,
			BotaniaTags.Items.INGOTS_ELEMENTIUM
	);

	private static final ToolMaterial TERRASTEEL_ITEM_MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
			2300,
			9F,
			4,
			26,
			BotaniaTags.Items.INGOTS_TERRASTEEL
	);

	private ConfigDataManager configDataManager = new ConfigDataManagerImpl();

	@Override
	public int apiVersion() {
		return 2;
	}

	@Nullable
	@Override
	public Registry<Brew> getBrewRegistry() {
		@SuppressWarnings("unchecked")
		Registry<Brew> registry = (Registry<Brew>) BuiltInRegistries.REGISTRY.getValue(BotaniaRegistries.BREWS.identifier());
		return registry;
	}

	@Override
	public ArmorMaterial getManasteelArmorMaterial() {
		return MANASTEEL_ARMOR_MATERIAL.get();
	}

	@Override
	public ArmorMaterial getElementiumArmorMaterial() {
		return ELEMENTIUM_ARMOR_MATERIAL.get();
	}

	@Override
	public ArmorMaterial getManaweaveArmorMaterial() {
		return MANAWEAVE_ARMOR_MATERIAL.get();
	}

	@Override
	public ArmorMaterial getTerrasteelArmorMaterial() {
		return TERRASTEEL_ARMOR_MATERIAL.get();
	}

	@Override
	public ToolMaterial getManasteelItemTier() {
		return MANASTEEL_ITEM_MATERIAL;
	}

	@Override
	public ToolMaterial getElementiumItemTier() {
		return ELEMENTIUM_ITEM_MATERIAL;
	}

	@Override
	public ToolMaterial getTerrasteelItemTier() {
		return TERRASTEEL_ITEM_MATERIAL;
	}

	@Override
	public ManaNetwork getManaNetworkInstance() {
		return ManaNetworkHandler.instance;
	}

	@Override
	public Container getAccessoriesInventory(Player player) {
		return EquipmentHandler.getAllWorn(player);
	}

	@Override
	public void breakOnAllCursors(Player player, ItemStack stack, BlockPos pos, Direction side) {
		RingOfLokiItem.breakOnAllCursors(player, stack, pos, side);
	}

	@Override
	public boolean hasSolegnoliaAround(Entity e) {
		return SolegnoliaBlockEntity.hasSolegnoliaAround(e);
	}

	@Override
	public void sparkleFX(Level world, double x, double y, double z, float r, float g, float b, float size, int m) {
		SparkleParticleData data = SparkleParticleData.sparkle(size, r, g, b, m);
		world.addParticle(data, x, y, z, 0, 0, 0);
	}

	private final Map<Identifier, Function<DyeColor, Block>> paintableBlocks = new ConcurrentHashMap<>();

	@Override
	public Map<Identifier, Function<DyeColor, Block>> getPaintableBlocks() {
		return Collections.unmodifiableMap(paintableBlocks);
	}

	@Override
	public void registerPaintableBlock(Identifier block, Function<DyeColor, Block> transformer) {
		paintableBlocks.put(block, transformer);
	}

	@Override
	public void registerCorporeaNodeDetector(CorporeaNodeDetector detector) {
		CorporeaNodeDetectors.register(detector);
	}

	@Override
	public ConfigDataManager getConfigData() {
		return configDataManager;
	}

	@Override
	public void setConfigData(ConfigDataManager configDataManager) {
		this.configDataManager = configDataManager;
	}
}
