/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.FloatingFlower;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.common.item.equipment.bauble.ThirdEyeItem;
import vazkii.botania.common.item.relic.KeyOfTheKingsLawItem;
import vazkii.botania.common.lib.LibMisc;

import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/** The extra block-state models which are not reachable from a block state definition. */
public final class MiscellaneousModels {
	private static final Map<Identifier, ExtraModelKey<BlockStateModel>> KEYS = new java.util.LinkedHashMap<>();

	public static final ExtraModelKey<BlockStateModel> GOLDFISH = key(prefix("icon/goldfish"));
	public static final ExtraModelKey<BlockStateModel> PHI_FLOWER = key(prefix("icon/phiflower"));
	public static final ExtraModelKey<BlockStateModel> NERF_BAT = key(prefix("icon/nerfbat"));
	public static final ExtraModelKey<BlockStateModel> BLOOD_PENDANT_CHAIN = key(prefix("icon/blood_pendant_chain"));
	public static final ExtraModelKey<BlockStateModel> BLOOD_PENDANT_GEM = key(prefix("icon/blood_pendant_gem"));
	public static final List<ExtraModelKey<BlockStateModel>> KING_KEY_WEAPONS = keys("icon/gate_weapon_", 0, KeyOfTheKingsLawItem.WEAPON_TYPES);
	public static final ExtraModelKey<BlockStateModel> TERRASTEEL_HELM_WILL = key(prefix("icon/will_flame"));
	public static final List<ExtraModelKey<BlockStateModel>> THIRD_EYE_LAYERS = keys("icon/third_eye_", 0, ThirdEyeItem.Renderer.NUM_LAYERS);
	public static final ExtraModelKey<BlockStateModel> PYROCLAST_GEM = key(prefix("icon/lava_pendant_gem"));
	public static final ExtraModelKey<BlockStateModel> CRIMSON_GEM = key(prefix("icon/super_lava_pendant_gem"));
	public static final ExtraModelKey<BlockStateModel> ITEM_FINDER_GEM = key(prefix("icon/itemfinder_gem"));
	public static final ExtraModelKey<BlockStateModel> CIRRUS_GEM = key(prefix("icon/cloud_pendant_gem"));
	public static final ExtraModelKey<BlockStateModel> NIMBUS_GEM = key(prefix("icon/super_cloud_pendant_gem"));
	public static final ExtraModelKey<BlockStateModel> SNOWFLAKE_PENDANT_GEM = key(prefix("icon/ice_pendant_gem"));
	public static final List<ExtraModelKey<BlockStateModel>> TIARA_WINGS = keys("icon/tiara_wing_", 1, FlugelTiaraItem.WING_TYPES);
	public static final ExtraModelKey<BlockStateModel> CORPOREA_CRYSTAL_CUBE_GLASS = key(prefix("block/corporea_crystal_cube_glass"));
	public static final ExtraModelKey<BlockStateModel> MANA_PUMP_HEAD = key(prefix("block/pump_head"));
	public static final ExtraModelKey<BlockStateModel> ELVEN_SPREADER_CORE = key(prefix("block/elven_spreader_core"));
	public static final ExtraModelKey<BlockStateModel> GAIA_SPREADER_CORE = key(prefix("block/gaia_spreader_core"));
	public static final ExtraModelKey<BlockStateModel> MANA_SPREADER_CORE = key(prefix("block/mana_spreader_core"));
	public static final ExtraModelKey<BlockStateModel> REDSTONE_SPREADER_CORE = key(prefix("block/redstone_spreader_core"));
	public static final ExtraModelKey<BlockStateModel> MANA_SPREADER_SCAFFOLDING = key(prefix("block/mana_spreader_scaffolding"));
	public static final ExtraModelKey<BlockStateModel> ELVEN_SPREADER_SCAFFOLDING = key(prefix("block/elven_spreader_scaffolding"));
	public static final ExtraModelKey<BlockStateModel> GAIA_SPREADER_SCAFFOLDING = key(prefix("block/gaia_spreader_scaffolding"));
	public static final Map<DyeColor, ExtraModelKey<BlockStateModel>> SPREADER_PADDINGS = new EnumMap<>(
			ColorHelper.supportedColors().collect(Collectors.toMap(Function.identity(),
					color -> key(prefix("block/" + color.getSerializedName() + "_spreader_padding")))));

	private static volatile Map<Identifier, ExtraModelKey<BlockStateModel>> tinyPotatoKeys = Map.of();
	private static volatile Map<Identifier, ExtraModelKey<ItemModel>> tinyPotatoItemKeys = Map.of();
	public static final MiscellaneousModels INSTANCE = new MiscellaneousModels();

	private MiscellaneousModels() {}

	public static TinyPotatoModels discoverTinyPotatoes(ResourceManager resources) {
		Map<Identifier, ExtraModelKey<BlockStateModel>> blockModels = resources.listResources(ResourcesLib.PREFIX_MODELS + ResourcesLib.PREFIX_TINY_POTATO,
				id -> id.getPath().endsWith(ResourcesLib.ENDING_JSON)).keySet().stream()
				.filter(id -> LibMisc.MOD_ID.equals(id.getNamespace()))
				.map(id -> Identifier.fromNamespaceAndPath(id.getNamespace(),
						id.getPath().substring(ResourcesLib.PREFIX_MODELS.length(),
								id.getPath().length() - ResourcesLib.ENDING_JSON.length())))
				.collect(Collectors.toUnmodifiableMap(Function.identity(), id -> ExtraModelKey.create(id::toString)));
		Map<Identifier, ExtraModelKey<ItemModel>> itemModels = blockModels.keySet().stream()
				.collect(Collectors.toUnmodifiableMap(Function.identity(),
						id -> ExtraModelKey.create(() -> id + " (item)")));
		return new TinyPotatoModels(blockModels, itemModels);
	}

	public static void register(ModelLoadingPlugin.Context context,
			TinyPotatoModels preparedTinyPotatoes) {
		registerIslands();
		KEYS.forEach((id, key) -> context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(id)));
		preparedTinyPotatoes.blockModels.forEach((id, key) -> context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(id)));
		preparedTinyPotatoes.itemModels.forEach((id, key) -> context.addModel(key,
				new SimpleUnbakedExtraModel<>(id, (resolved, baker) -> {
					var textures = resolved.getTopTextureSlots();
					var quads = resolved.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY);
					var properties = ModelRenderProperties.fromResolvedModel(baker, resolved, textures);
					return (renderState, stack, resolver, displayContext, level, owner, seed) -> {
						renderState.appendModelIdentityElement(key);
						var layer = renderState.newLayer();
						if (stack.hasFoil()) {
							layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
						}
						layer.setExtents(() -> net.minecraft.client.renderer.item.CuboidItemModelWrapper
								.computeExtents(quads.getAll()));
						properties.applyToLayer(layer, displayContext);
						layer.prepareQuadList().addAll(quads.getAll());
					};
				})));
		tinyPotatoKeys = preparedTinyPotatoes.blockModels;
		tinyPotatoItemKeys = preparedTinyPotatoes.itemModels;
	}

	public BlockStateModel get(ExtraModelKey<BlockStateModel> key) {
		BlockStateModel model = ((FabricModelManager) Minecraft.getInstance().getModelManager()).getModel(key);
		if (model == null) {
			Identifier id = KEYS.entrySet().stream().filter(entry -> entry.getValue().equals(key))
					.map(Map.Entry::getKey).findFirst().orElse(null);
			BotaniaAPI.LOGGER.error("Missing registered Botania extra model; identifier={}, key={}", id, key);
			return Minecraft.getInstance().getModelManager().getBlockStateModelSet().missingModel();
		}
		return model;
	}

	public BlockStateModel getTinyPotatoModel(Identifier id) {
		ExtraModelKey<BlockStateModel> key = tinyPotatoKeys.get(id);
		if (key == null) {
			String fallback = ClientProxy.dootDoot ? "halloween" : "default";
			key = tinyPotatoKeys.get(prefix(ResourcesLib.PREFIX_TINY_POTATO + "/" + fallback));
		}
		return key == null ? Minecraft.getInstance().getModelManager().getBlockStateModelSet().missingModel() : get(key);
	}

	public ItemModel getTinyPotatoItemModel(Identifier id) {
		ExtraModelKey<ItemModel> key = tinyPotatoItemKeys.get(id);
		if (key == null) {
			String fallback = ClientProxy.dootDoot ? "halloween" : "default";
			key = tinyPotatoItemKeys.get(prefix(ResourcesLib.PREFIX_TINY_POTATO + "/" + fallback));
		}
		if (key == null) {
			BotaniaAPI.LOGGER.error("No Tiny Potato item model was registered for {}", id);
			return Minecraft.getInstance().getModelManager().getItemModel(id);
		}
		ItemModel model = ((FabricModelManager) Minecraft.getInstance().getModelManager()).getModel(key);
		if (model == null) {
			BotaniaAPI.LOGGER.error("Missing registered Tiny Potato item model; identifier={}, key={}", id, key);
			return Minecraft.getInstance().getModelManager().getItemModel(id);
		}
		return model;
	}

	public record TinyPotatoModels(Map<Identifier, ExtraModelKey<BlockStateModel>> blockModels,
			Map<Identifier, ExtraModelKey<ItemModel>> itemModels) {}

	public BlockStateModel goldfishModel() { return get(GOLDFISH); }
	public BlockStateModel phiFlowerModel() { return get(PHI_FLOWER); }
	public BlockStateModel nerfBatModel() { return get(NERF_BAT); }
	public BlockStateModel bloodPendantChain() { return get(BLOOD_PENDANT_CHAIN); }
	public BlockStateModel bloodPendantGem() { return get(BLOOD_PENDANT_GEM); }
	public BlockStateModel terrasteelHelmWillModel() { return get(TERRASTEEL_HELM_WILL); }
	public BlockStateModel pyroclastGem() { return get(PYROCLAST_GEM); }
	public BlockStateModel crimsonGem() { return get(CRIMSON_GEM); }
	public BlockStateModel itemFinderGem() { return get(ITEM_FINDER_GEM); }
	public BlockStateModel cirrusGem() { return get(CIRRUS_GEM); }
	public BlockStateModel nimbusGem() { return get(NIMBUS_GEM); }
	public BlockStateModel snowflakePendantGem() { return get(SNOWFLAKE_PENDANT_GEM); }
	public BlockStateModel corporeaCrystalCubeGlass() { return get(CORPOREA_CRYSTAL_CUBE_GLASS); }
	public BlockStateModel manaPumpHead() { return get(MANA_PUMP_HEAD); }
	public BlockStateModel elvenSpreaderCore() { return get(ELVEN_SPREADER_CORE); }
	public BlockStateModel gaiaSpreaderCore() { return get(GAIA_SPREADER_CORE); }
	public BlockStateModel manaSpreaderCore() { return get(MANA_SPREADER_CORE); }
	public BlockStateModel redstoneSpreaderCore() { return get(REDSTONE_SPREADER_CORE); }
	public BlockStateModel manaSpreaderScaffolding() { return get(MANA_SPREADER_SCAFFOLDING); }
	public BlockStateModel elvenSpreaderScaffolding() { return get(ELVEN_SPREADER_SCAFFOLDING); }
	public BlockStateModel gaiaSpreaderScaffolding() { return get(GAIA_SPREADER_SCAFFOLDING); }
	public BlockStateModel kingKeyWeaponModel(int index) { return get(KING_KEY_WEAPONS.get(index)); }
	public BlockStateModel thirdEyeLayer(int index) { return get(THIRD_EYE_LAYERS.get(index)); }
	public BlockStateModel tiaraWing(int index) { return get(TIARA_WINGS.get(index)); }
	public BlockStateModel spreaderPadding(DyeColor color) { return get(SPREADER_PADDINGS.get(color)); }

	private static ExtraModelKey<BlockStateModel> key(Identifier id) {
		ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(id::toString);
		KEYS.put(id, key);
		return key;
	}

	private static List<ExtraModelKey<BlockStateModel>> keys(String prefix, int first, int count) {
		return IntStream.range(first, first + count).mapToObj(i -> key(prefix(prefix + i)))
				.toList();
	}

	private static void registerIslands() {
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.GRASS, prefix("block/islands/island_grass"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.PODZOL, prefix("block/islands/island_podzol"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.MYCEL, prefix("block/islands/island_mycel"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.SNOW, prefix("block/islands/island_snow"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.DRY, prefix("block/islands/island_dry"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.GOLDEN, prefix("block/islands/island_golden"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.VIVID, prefix("block/islands/island_vivid"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.SCORCHED, prefix("block/islands/island_scorched"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.INFUSED, prefix("block/islands/island_infused"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.MUTATED, prefix("block/islands/island_mutated"));
	}
}
