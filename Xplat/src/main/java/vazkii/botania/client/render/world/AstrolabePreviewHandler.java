/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.item.AstrolabeItem;

import java.util.List;

public final class AstrolabePreviewHandler {
	public static void onWorldRenderLast(PoseStack ms, Level level, SubmitNodeCollector submitNodeCollector) {

		for (Player player : level.players()) {
			ItemStack currentStack = player.getMainHandItem();
			InteractionHand hand = InteractionHand.MAIN_HAND;
			if (currentStack.isEmpty() || !(currentStack.getItem() instanceof AstrolabeItem)) {
				currentStack = player.getOffhandItem();
				hand = InteractionHand.OFF_HAND;
			}

			if (!currentStack.isEmpty() && currentStack.getItem() instanceof AstrolabeItem) {
				Block block = AstrolabeItem.getBlock(currentStack, level.holderLookup(Registries.BLOCK));
				if (block != Blocks.AIR) {
					renderPlayerLook(ms, submitNodeCollector, player, currentStack, hand);
				}
			}
		}

	}

	private static void renderPlayerLook(PoseStack ms, SubmitNodeCollector submitNodeCollector, Player player, ItemStack stack, InteractionHand hand) {
		Block blockToPlace = AstrolabeItem.getBlock(stack, player.level().holderLookup(Registries.BLOCK));
		int size = AstrolabeItem.getSize(stack);
		BlockPlaceContext ctx = AstrolabeItem.getBlockPlaceContext(player, hand, blockToPlace);
		List<BlockPos> placePositions = AstrolabeItem.getPlacePositions(ctx, size);
		if (ctx != null && AstrolabeItem.hasBlocks(stack, player, placePositions.size(), blockToPlace)) {
			for (BlockPos pos : placePositions) {
				BlockPlaceContext placeContext = getPlaceContext(player, ctx, pos);
				BlockState state = blockToPlace.getStateForPlacement(placeContext);
				if (state != null && placeContext.canPlace() && state.canSurvive(player.level(), pos)) {
					renderBlockAt(ms, submitNodeCollector, state, pos, player.level());
				}
			}
		}
	}

	@NotNull
	private static BlockPlaceContext getPlaceContext(Player player, BlockPlaceContext ctx, BlockPos pos) {
		Vec3 newHitVec = new Vec3(pos.getX() + Mth.frac(ctx.getClickLocation().x()),
				pos.getY() + Mth.frac(ctx.getClickLocation().y()),
				pos.getZ() + Mth.frac(ctx.getClickLocation().z()));
		BlockHitResult newHit = new BlockHitResult(newHitVec, ctx.getClickedFace(), pos, false);
		return new BlockPlaceContext(player, ctx.getHand(), ctx.getItemInHand(), newHit);
	}

	private static void renderBlockAt(PoseStack ms, SubmitNodeCollector submitNodeCollector,
			BlockState state, BlockPos pos, Level level) {
		Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().position();
		ms.pushPose();
		ms.translate(pos.getX() - cameraPosition.x(), pos.getY() - cameraPosition.y(), pos.getZ() - cameraPosition.z());

		BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
		BlockModelRenderState renderState = new BlockModelRenderState();
		model.collectParts(RandomSource.create(state.getSeed(pos)), renderState.setupModel(ms.last().pose(), true));
		for (var tintSource : Minecraft.getInstance().getBlockColors().getTintSources(state)) {
			int color = tintSource.colorInWorld(state, (net.minecraft.client.renderer.block.BlockAndTintGetter) level, pos);
			renderState.tintLayers().add(ARGB.color(0x66, color));
		}
		renderState.submit(ms, submitNodeCollector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
		ms.popPose();
	}

}
