/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.fabric.client;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.PlatformBlockEntity;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.function.Predicate;

public final class FabricPlatformModel extends WrapperBlockStateModel {
	public FabricPlatformModel(BlockStateModel wrapped) {
		super(wrapped);
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos,
			BlockState state, RandomSource random, Predicate<Direction> cullTest) {
		if (!(state.getBlock() instanceof PlatformBlock)) {
			Minecraft.getInstance().getModelManager().getMissingBlockStateModel()
					.emitQuads(emitter, blockView, pos, state, random, cullTest);
			return;
		}

		Object data = blockView.getBlockEntityRenderData(pos);
		if (data instanceof PlatformBlockEntity.PlatformData platformData) {
			BlockState heldState = platformData.state();
			if (heldState != null && !heldState.is(BotaniaTags.Blocks.UNSUPPORTED_PLATFORM_DISGUISE)) {
				Minecraft.getInstance().getModelManager().getBlockStateModel(heldState)
						.emitQuads(emitter, blockView, platformData.pos(), heldState, random, cullTest);
				return;
			}
		}

		super.emitQuads(emitter, blockView, pos, state, random, cullTest);
	}
}
