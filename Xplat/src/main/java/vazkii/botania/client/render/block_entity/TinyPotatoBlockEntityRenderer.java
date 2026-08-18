/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.client.render.block_entity;

import vazkii.botania.client.core.handler.ClientTickHandler;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.block_entity.state.TinyPotatoRenderState;
import vazkii.botania.common.block.block_entity.TinyPotatoBlockEntity;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.handler.ContributorList;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.block.TinyPotatoBlockItem;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class TinyPotatoBlockEntityRenderer
		implements BlockEntityRenderer<TinyPotatoBlockEntity, TinyPotatoRenderState> {
	public static final String DEFAULT = "default";
	public static final String HALLOWEEN = "halloween";
	private static final Pattern ESCAPED = Pattern.compile("[^a-z0-9/._-]");
	private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
	private final BlockModelResolver blockModelResolver;
	private final ItemModelResolver itemModelResolver;
	private final Font font;

	public TinyPotatoBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		blockModelResolver = context.blockModelResolver();
		itemModelResolver = context.itemModelResolver();
		font = context.font();
	}

	public static BlockStateModel getModelFromDisplayName(Component displayName) {
		var builder = new StringBuilder();
		TinyPotatoBlockItem.isEnchantedName(displayName, builder);
		return getModel(builder.toString().toLowerCase(Locale.ROOT));
	}

	public static ItemModel getItemModelFromDisplayName(Component displayName) {
		var builder = new StringBuilder();
		TinyPotatoBlockItem.isEnchantedName(displayName, builder);
		return MiscellaneousModels.INSTANCE.getTinyPotatoItemModel(taterLocation(builder.toString().toLowerCase(Locale.ROOT)));
	}

	private static BlockStateModel getModel(String name) {
		return MiscellaneousModels.INSTANCE.getTinyPotatoModel(taterLocation(name));
	}

	private static Identifier taterLocation(String name) {
		return prefix(ResourcesLib.PREFIX_TINY_POTATO + "/" + normalizeName(name));
	}

	private static String normalizeName(String name) {
		return ESCAPED.matcher(name).replaceAll("_").toLowerCase(Locale.ROOT);
	}

	@Override
	public TinyPotatoRenderState createRenderState() {
		return new TinyPotatoRenderState();
	}

	@Override
	public void extractRenderState(TinyPotatoBlockEntity blockEntity, TinyPotatoRenderState state,
			float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.displayName = blockEntity.name.copy();
		var builder = new StringBuilder();
		state.enchanted = TinyPotatoBlockItem.isEnchantedName(state.displayName, builder);
		state.name = builder.toString().toLowerCase(Locale.ROOT);
		state.blockPos = blockEntity.getBlockPos().immutable();
		state.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
		state.rotationY = switch (state.facing) {
			default -> 0F;
			case SOUTH -> 180F;
			case EAST -> 90F;
			case WEST -> 270F;
		};
		float jump = blockEntity.jumpTicks;
		if (jump > 0) {
			jump -= partialTicks;
		}
		float wave = (float) Math.sin(jump / 10 * Math.PI);
		state.jumpUp = Math.abs(wave) * 0.2F;
		state.rotationZ = wave * 2;
		state.jumpWiggle = wave * 0.05F;
		state.partialTicks = partialTicks;
		state.renderBody = !(state.name.equals("mami") || state.name.equals("soaryn")
				|| state.name.equals("eloraam") && jump != 0);
		state.bodyParts = collect(getModel(state.name));
		state.bodyItem.clear();
		ItemStack bodyStack = new ItemStack(BotaniaBlocks.tinyPotato);
		bodyStack.set(DataComponents.CUSTOM_NAME, state.displayName.copy());
		if (state.enchanted) {
			bodyStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		}
		itemModelResolver.updateForTopItem(state.bodyItem, bodyStack, ItemDisplayContext.NONE,
				blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode());
		for (int i = 0; i < 6; i++) {
			state.attachedItems.get(i).clear();
			state.attachedPresent[i] = false;
			state.attachedBlock[i] = state.attachedPotato[i] = state.attachedSkull[i] = state.attachedKing[i] = false;
			if (i >= blockEntity.inventorySize()) {
				continue;
			}
			ItemStack stack = blockEntity.getItemHandler().getItem(i).copy();
			if (stack.isEmpty()) {
				continue;
			}
			Direction side = Direction.values()[i];
			if (side.getAxis() != Axis.Y) {
				side = Direction.fromYRot(side.toYRot() - state.facing.toYRot());
			}
			state.attachedSides[i] = side;
			state.attachedPresent[i] = true;
			state.attachedBlock[i] = stack.getItem() instanceof BlockItem;
			state.attachedPotato[i] = stack.getItem() instanceof TinyPotatoBlockItem;
			state.attachedSkull[i] = stack.getItem() instanceof BlockItem item && item.getBlock() instanceof AbstractSkullBlock;
			if (state.attachedPotato[i] && stack.has(DataComponents.CUSTOM_NAME)) {
				var child = new StringBuilder();
				TinyPotatoBlockItem.isEnchantedName(stack.getHoverName(), child);
				state.attachedKing[i] = child.toString().equals("kingdaddydmac");
			}
			itemModelResolver.updateForTopItem(state.attachedItems.get(i), stack, ItemDisplayContext.HEAD,
					blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode() + i);
		}
		extractContributorExtras(blockEntity, state);
		extractName(state);
	}

	private void extractContributorExtras(TinyPotatoBlockEntity blockEntity, TinyPotatoRenderState state) {
		state.phiFlowerParts = state.nerfBatParts = state.goldfishParts = List.of();
		state.showPhiFlower = state.showNerfBat = state.showGoldfish = false;
		state.showMartyBoot = state.showJibrilHalo = state.showKingDaddyExtras = state.showDefaultFlower = false;
		state.martyBoot.clear();
		state.manaRing1.clear();
		state.manaRing2.clear();
		state.defaultFlower.clear();
		switch (state.name) {
			case "phi" -> {
				state.showPhiFlower = true;
				state.phiFlowerParts = collect(MiscellaneousModels.INSTANCE.phiFlowerModel());
			}
			case "vazkii" -> {
				state.showPhiFlower = state.showNerfBat = true;
				state.phiFlowerParts = collect(MiscellaneousModels.INSTANCE.phiFlowerModel());
				state.nerfBatParts = collect(MiscellaneousModels.INSTANCE.nerfBatModel());
			}
			case "haighyorkie" -> {
				state.showGoldfish = true;
				state.goldfishParts = collect(MiscellaneousModels.INSTANCE.goldfishModel());
			}
			case "martysgames", "marty" -> {
				state.showMartyBoot = true;
				ItemStack boot = new ItemStack(BotaniaItems.infiniteFruit);
				boot.set(DataComponents.CUSTOM_NAME, Component.literal("das boot"));
				itemModelResolver.updateForTopItem(state.martyBoot, boot, ItemDisplayContext.HEAD,
						blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode() + 31);
			}
			case "jibril" -> state.showJibrilHalo = true;
			case "kingdaddydmac" -> {
				state.showKingDaddyExtras = true;
				itemModelResolver.updateForTopItem(state.manaRing1, new ItemStack(BotaniaItems.manaRing), ItemDisplayContext.HEAD,
						blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode() + 32);
				itemModelResolver.updateForTopItem(state.manaRing2, new ItemStack(BotaniaItems.manaRing), ItemDisplayContext.HEAD,
						blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode() + 33);
				blockModelResolver.update(state.cake, Blocks.CAKE.defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
			}
			default -> {
				ContributorList.firstStart();
				ItemStack flower = ContributorList.getFlower(state.name);
				if (!flower.isEmpty()) {
					state.showDefaultFlower = true;
					itemModelResolver.updateForTopItem(state.defaultFlower, flower.copy(), ItemDisplayContext.HEAD,
							blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode() + 34);
				}
			}
		}
	}

	private void extractName(TinyPotatoRenderState state) {
		HitResult hit = Minecraft.getInstance().hitResult;
		state.renderName = Minecraft.renderNames() && !state.name.isEmpty()
				&& hit instanceof BlockHitResult blockHit && state.blockPos.equals(blockHit.getBlockPos());
		state.nameWidth = font.width(state.displayName.getString());
		state.sublabel = state.name.equals("pahimar") ? Component.literal("[WIP]")
				: state.name.equals("soaryn") ? Component.literal("(soon)") : Component.empty();
		state.sublabelWidth = font.width(state.sublabel);
		float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
		state.nameBackground = (int) (opacity * 255F) << 24;
	}

	@Override
	public void submit(TinyPotatoRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0, 0.5F);
		poseStack.mulPose(VecHelper.rotateY(-state.rotationY));
		poseStack.translate(state.jumpWiggle, state.jumpUp, 0);
		poseStack.mulPose(VecHelper.rotateZ(state.rotationZ));
		if (state.renderBody) {
			poseStack.pushPose();
			poseStack.translate(-0.5F, 0, -0.5F);
			state.bodyItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
		poseStack.translate(0, 1.5F, 0);
		poseStack.pushPose();
		poseStack.mulPose(VecHelper.rotateZ(180));
		submitAttached(state, poseStack, collector);
		submitExtras(state, poseStack, collector);
		poseStack.pushPose();
		ClientXplatAbstractions.INSTANCE.fireRenderTinyPotato(state.blockPos, state.displayName, state.name,
				state.partialTicks, poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
		poseStack.popPose();
		poseStack.mulPose(VecHelper.rotateZ(-state.rotationZ));
		poseStack.mulPose(VecHelper.rotateY(state.rotationY));
		if (state.renderName) {
			submitName(state, poseStack, collector, camera);
		}
		poseStack.popPose();
	}

	private static void submitAttached(TinyPotatoRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
		poseStack.pushPose();
		poseStack.mulPose(VecHelper.rotateZ(180));
		poseStack.translate(0, -1, 0);
		poseStack.scale(1F / 3.5F, 1F / 3.5F, 1F / 3.5F);
		for (int i = 0; i < 6; i++) {
			if (!state.attachedPresent[i]) continue;
			poseStack.pushPose();
			transformAttached(poseStack, state.attachedSides[i], state.attachedPotato[i], state.attachedBlock[i], state.attachedKing[i]);
			if (state.attachedPotato[i]) poseStack.scale(1.1F, 1.1F, 1.1F);
			else if (state.attachedBlock[i]) poseStack.scale(0.5F, 0.5F, 0.5F);
			if (state.attachedBlock[i] && (state.attachedSides[i] == Direction.NORTH || state.attachedSkull[i]))
				poseStack.mulPose(VecHelper.rotateY(180));
			state.attachedItems.get(i).submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
		poseStack.popPose();
	}

	private static void transformAttached(PoseStack pose, Direction side, boolean potato, boolean block, boolean king) {
		switch (side) {
			case UP -> { if (potato) pose.translate(0, -0.375, 0.5); else if (block) pose.translate(0, 0.3, 0.5); pose.translate(0, -0.5, -0.4); }
			case DOWN -> { pose.translate(0, -2.3, -0.88); if (potato) pose.translate(0, 1.25, 0.5); else if (block) pose.translate(0, 1, 0.6); }
			case NORTH -> { pose.translate(0, -1.9, 0.02); if (potato) pose.translate(0, 0.2, 0.57); else if (block) pose.translate(0, 1, 0.6); }
			case SOUTH -> { pose.translate(0, -1.6, -0.89); if (potato) pose.translate(0, -0.59, 0.26); else if (block) pose.translate(0, 1, 0.5); }
			case EAST -> { if (potato) pose.translate(-0.35, -0.29, -0.06); else if (block) pose.translate(-0.4, 0.8, 0); else pose.mulPose(VecHelper.rotateY(-90)); pose.translate(-0.3, -1.9, 0.04); }
			case WEST -> { if (potato) { pose.translate(0.95, -0.29, 0.9); if (king) pose.translate(0.55, 0, 0); } else if (block) pose.translate(1, 0.8, 1); else pose.mulPose(VecHelper.rotateY(-90)); pose.translate(-0.3, -1.9, -0.92); }
		}
	}

	private static void submitExtras(TinyPotatoRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
		poseStack.pushPose();
		poseStack.translate(0, 1, 0);
		poseStack.scale(0.25F, 0.25F, 0.25F);
		if (state.showPhiFlower) {
			poseStack.pushPose();
			poseStack.translate(-0.08, 0.1, 0.4);
			poseStack.mulPose(VecHelper.rotateY(90F));
			poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(20), 1, 0, 1));
			collector.submitBlockModel(poseStack, Sheets.translucentBlockSheet(), state.phiFlowerParts,
					BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
		if (state.showNerfBat) {
			poseStack.scale(1.25F, 1.25F, 1.25F);
			poseStack.mulPose(VecHelper.rotateX(180F));
			poseStack.mulPose(VecHelper.rotateY(-90F));
			poseStack.translate(0.2, -1.25, -0.075);
			collector.submitBlockModel(poseStack, Sheets.translucentBlockSheet(), state.nerfBatParts,
					BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		} else if (state.showGoldfish) {
			poseStack.scale(1.25F, 1.25F, 1.25F);
			poseStack.mulPose(VecHelper.rotateZ(180F));
			poseStack.mulPose(VecHelper.rotateY(-90F));
			poseStack.translate(-0.5F, -1.2F, -0.075F);
			collector.submitBlockModel(poseStack, Sheets.translucentBlockSheet(), state.goldfishParts,
					BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		} else if (state.showMartyBoot) {
			poseStack.scale(0.7F, 0.7F, 0.7F);
			poseStack.mulPose(VecHelper.rotateZ(180F));
			poseStack.translate(-0.3F, -2.7F, -1.2F);
			poseStack.mulPose(VecHelper.rotateZ(15F));
			state.martyBoot.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		} else if (state.showJibrilHalo) {
			poseStack.scale(1.5F, 1.5F, 1.5F);
			poseStack.translate(0F, 0.8F, 0F);
			FlugelTiaraItem.ClientLogic.submitHalo(poseStack, collector,
					ClientTickHandler.ticksInGame + state.partialTicks);
		} else if (state.showKingDaddyExtras) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(VecHelper.rotateZ(180F));
			poseStack.mulPose(VecHelper.rotateY(90F));
			poseStack.pushPose();
			poseStack.translate(0F, -2.5F, 0.65F);
			state.manaRing1.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.translate(0F, 0F, -4F);
			state.manaRing2.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
			poseStack.translate(1.5, -4, -2.5);
			state.cake.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		} else if (state.showDefaultFlower) {
			poseStack.mulPose(VecHelper.rotateX(180F));
			poseStack.mulPose(VecHelper.rotateY(180F));
			poseStack.translate(0, -0.78, -0.5);
			state.defaultFlower.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		}
		poseStack.popPose();
	}

	private static void submitName(TinyPotatoRenderState state, PoseStack poseStack,
			SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0, -0.6, 0);
		poseStack.mulPose(camera.orientation);
		float scale = 0.016666668F * 1.6F;
		poseStack.scale(-scale, -scale, scale);
		var displayName = state.displayName.getVisualOrderText();
		collector.submitText(poseStack, -state.nameWidth / 2F, 0, displayName, 0x20FFFFFF,
				false, Font.DisplayMode.SEE_THROUGH, state.lightCoords, state.nameBackground, 0);
		collector.submitText(poseStack, -state.nameWidth / 2F, 0, displayName, 0xFFFFFFFF,
				false, Font.DisplayMode.NORMAL, state.lightCoords, 0, 0);
		if (!state.sublabel.getString().isEmpty()) {
			poseStack.translate(0, 14, 0);
			var sublabel = state.sublabel.getVisualOrderText();
			collector.submitText(poseStack, -state.sublabelWidth / 2F, 0, sublabel, 0x20FFFFFF,
					false, Font.DisplayMode.SEE_THROUGH, state.lightCoords, state.nameBackground, 0);
			collector.submitText(poseStack, -state.sublabelWidth / 2F, 0, sublabel, 0xFFFFFFFF,
					false, Font.DisplayMode.SEE_THROUGH, state.lightCoords, 0, 0);
		}
		poseStack.popPose();
	}

	private static List<BlockStateModelPart> collect(BlockStateModel model) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(42), parts);
		return List.copyOf(parts);
	}

}
