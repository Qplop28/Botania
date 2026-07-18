/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.BotanicalBreweryModel;
import vazkii.botania.client.render.block_entity.state.BotanicalBreweryRenderState;
import vazkii.botania.common.block.block_entity.BreweryBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class BotanicalBreweryBlockEntityRenderer implements BlockEntityRenderer<BreweryBlockEntity, BotanicalBreweryRenderState> {
	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_BREWERY);
	private final BotanicalBreweryModel model;
	private final ItemModelResolver itemModelResolver;

	public BotanicalBreweryBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new BotanicalBreweryModel(ctx.bakeLayer(BotaniaModelLayers.BREWERY));
		itemModelResolver = ctx.itemModelResolver();
	}

	@Override
	public BotanicalBreweryRenderState createRenderState() {
		return new BotanicalBreweryRenderState();
	}

	@Override
	public void extractRenderState(BreweryBlockEntity blockEntity, BotanicalBreweryRenderState state,
			float partialTicks, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.animationTime = ClientTickHandler.ticksInGame + partialTicks;
		state.plateCount = blockEntity.inventorySize() - 1;
		List<ItemStackRenderState> items = new ArrayList<>();
		for (int i = 0; i < blockEntity.inventorySize(); i++) {
			ItemStackRenderState itemState = new ItemStackRenderState();
			itemModelResolver.updateForTopItem(itemState, blockEntity.getItemHandler().getItem(i),
					ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
			items.add(itemState);
		}
		state.items = List.copyOf(items);
	}

	@Override
	public void submit(BotanicalBreweryRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.scale(1F, -1F, -1F);
		poseStack.translate(0.5F, -1.5F, -0.5F);
		model.submit(state, poseStack, submitNodeCollector, texture);
		poseStack.popPose();
	}
}
