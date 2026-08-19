package vazkii.botania.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaFabricClientCapabilities;
import vazkii.botania.client.BotaniaItemProperties;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.CorporeaInputHandler;
import vazkii.botania.client.core.handler.KonamiHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.fx.BotaniaParticleRenderTypes;
import vazkii.botania.client.fx.BotaniaParticles;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.client.gui.ManaBarTooltipComponent;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.client.gui.bag.FlowerPouchGui;
import vazkii.botania.client.gui.box.BaubleBoxGui;
import vazkii.botania.client.model.BotaniaLayerDefinitions;
import vazkii.botania.client.model.TinyPotatoModel;
import vazkii.botania.client.model.ManaBlasterItemModel;
import vazkii.botania.client.model.armor.ArmorModels;
import vazkii.botania.client.render.BotaniaItemTintSource;
import vazkii.botania.client.render.ColorHandler;
import vazkii.botania.client.render.entity.*;
import vazkii.botania.client.render.item.BotaniaBlockEntityItemRenderer;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.entity.GaiaGuardianEntity;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.fabric.network.FabricPacketHandler;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.List;
import java.util.function.Function;

public class FabricClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// ensure API implementations are loaded
		BotaniaAPI.LOGGER.debug("Client API instances: {}",
				List.of(BotaniaAPIClient.instance(), ClientXplatAbstractions.instance()));

		FabricPacketHandler.initClient();

		ColorHandler.submitBlocks(BlockColorRegistry::register);
		ColorHandler.initItemTints();
		ItemTintSources.ID_MAPPER.put(Identifier.fromNamespaceAndPath(LibMisc.MOD_ID, "dynamic_item_color"), BotaniaItemTintSource.MAP_CODEC);
		ItemModels.ID_MAPPER.put(ClientXplatAbstractions.MANA_GUN_MODEL_LOADER_ID, ManaBlasterItemModel.Unbaked.MAP_CODEC);
		ConditionalItemModelProperties.ID_MAPPER.put(Identifier.fromNamespaceAndPath(LibMisc.MOD_ID, "boolean"),
				BotaniaItemProperties.Conditional.MAP_CODEC);
		RangeSelectItemModelProperties.ID_MAPPER.put(Identifier.fromNamespaceAndPath(LibMisc.MOD_ID, "swigs_taken"),
				BotaniaItemProperties.SwigsTaken.MAP_CODEC);
		SpecialModelRenderers.ID_MAPPER.put(Identifier.fromNamespaceAndPath(LibMisc.MOD_ID, "block_entity"),
				BotaniaBlockEntityItemRenderer.Unbaked.MAP_CODEC);

		// Guis
		MenuScreens.register(BotaniaItems.FLOWER_BAG_CONTAINER, FlowerPouchGui::new);
		MenuScreens.register(BotaniaItems.BAUBLE_BOX_CONTAINER, BaubleBoxGui::new);

		// Blocks and Items
		PreparableModelLoadingPlugin.register(
				(sharedState, executor) -> java.util.concurrent.CompletableFuture.supplyAsync(
						() -> MiscellaneousModels.discoverTinyPotatoes(sharedState.resourceManager()), executor),
				(data, context) -> {
				MiscellaneousModels.register(context, data);
				context.modifyBlockModelAfterBake().register((model, modifierContext) -> {
					var block = modifierContext.state().getBlock();
					if (block == vazkii.botania.common.block.BotaniaBlocks.abstrusePlatform
							|| block == vazkii.botania.common.block.BotaniaBlocks.spectralPlatform
							|| block == vazkii.botania.common.block.BotaniaBlocks.infrangiblePlatform) {
						return ClientXplatAbstractions.INSTANCE.wrapPlatformModel(model);
					}
					return model;
				});
				context.modifyItemModelAfterBake().register((model, modifierContext) ->
						modifierContext.itemId().equals(BuiltInRegistries.ITEM.getKey(
								vazkii.botania.common.block.BotaniaBlocks.tinyPotato.asItem()))
								? new TinyPotatoModel(model)
								: model);
				});

		// BE/Entity Renderer
		BotaniaLayerDefinitions.init((location, supplier) ->
				ModelLayerRegistry.registerModelLayer(location, supplier::get));
		EntityRenderers.registerBlockEntityRenderers(BlockEntityRenderers::register);
		EntityRenderers.registerEntityRenderers(EntityRendererRegistry::register);

		BotaniaParticles.FactoryHandler.registerFactories(new BotaniaParticles.FactoryHandler.Consumer() {
			@Override
			public <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> constructor) {
				ParticleProviderRegistry.getInstance().register(type, constructor::apply);
			}
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(client ->
				BotaniaParticleRenderTypes.init(client.getTextureManager()));

		// Events
		ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof GaiaGuardianEntity gaia) {
				gaia.onClientEntityLoad();
			}
		});
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
			if (entity instanceof GaiaGuardianEntity gaia) {
				gaia.onClientEntityUnload();
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::clientTickEnd);
		ClientTickEvents.END_CLIENT_TICK.register(KonamiHandler::clientTick);
		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.fromNamespaceAndPath(LibMisc.MOD_ID, "hud"), HUDHandler::onDrawScreenPost);
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> TooltipHandler.onTooltipEvent(stack, flag, lines));
		ClientTooltipComponentCallback.EVENT.register(ManaBarTooltipComponent::tryConvert);
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> ScreenKeyboardEvents.beforeKeyPress(screen)
				.register((screen2, event) -> CorporeaInputHandler.buttonPressed(event.key(), event.scancode())));

		// Etc
		ClientProxy.initSeasonal();
		ClientProxy.initKeybindings(KeyMappingHelper::registerKeyMapping);

		registerArmors();
		registerCapabilities();

	}

	private static void registerCapabilities() {
		BotaniaEntities.registerWandHudCaps((factory, types) -> BotaniaFabricClientCapabilities.ENTITY_WAND_HUD.registerForTypes((e, c) -> factory.apply(e), types));
		BotaniaBlockEntities.registerWandHudCaps((factory, types) -> BotaniaFabricClientCapabilities.WAND_HUD.registerForBlockEntities((be, c) -> factory.apply(be), types));
		BotaniaFlowerBlocks.registerWandHudCaps((factory, types) -> BotaniaFabricClientCapabilities.WAND_HUD.registerForBlockEntities((be, c) -> factory.apply(be), types));
	}

	private static void registerArmors() {
		Item[] armors = BuiltInRegistries.ITEM.stream()
				.filter(i -> i instanceof ManasteelArmorItem
						&& BuiltInRegistries.ITEM.getKey(i).getNamespace().equals(LibMisc.MOD_ID))
				.toArray(Item[]::new);

		ArmorRenderer renderer = (poseStack, submitNodeCollector, stack, renderState, slot, light, contextModel) -> {
			ManasteelArmorItem armor = (ManasteelArmorItem) stack.getItem();
			var model = ArmorModels.get(stack);
			var texture = armor.getArmorTexture(stack, null, slot, "");
			if (model != null) {
				model.prepareForRender();
				ArmorRenderer.submitTransformCopyingModel(contextModel, renderState, model, renderState, false,
						submitNodeCollector, poseStack, model.renderType(Identifier.parse(texture)), light, 0, -1, null);
			}
		};
		ArmorRenderer.register(renderer, armors);
	}
}
