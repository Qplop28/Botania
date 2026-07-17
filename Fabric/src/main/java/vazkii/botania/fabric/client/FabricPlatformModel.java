/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.fabric.client;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.PlatformBlockEntity;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.function.Predicate;

public final class FabricPlatformModel extends WrapperBlockStateModel {
	public FabricPlatformModel(BlockStateModel wrapped) {
		super(wrapped);
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos,
			BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
		if (!(state.getBlock() instanceof PlatformBlock)) {
			modelSet.missingModel().emitQuads(emitter, level, pos, state, random, cullTest);
			return;
		}

		if (((FabricBlockGetter) level).getBlockEntityRenderData(pos)
				instanceof PlatformBlockEntity.PlatformData platformData) {
			BlockState heldState = platformData.state();
			if (heldState != null && !heldState.is(BotaniaTags.Blocks.UNSUPPORTED_PLATFORM_DISGUISE)) {
				modelSet.get(heldState).emitQuads(
						emitter, level, platformData.pos(), heldState, random, cullTest);
				return;
			}
		}

		super.emitQuads(emitter, level, pos, state, random, cullTest);
	}

	@Override
	public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos,
			BlockState state, RandomSource random) {
		var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
		if (!(state.getBlock() instanceof PlatformBlock)) {
			return modelSet.missingModel().createGeometryKey(level, pos, state, random);
		}

		if (((FabricBlockGetter) level).getBlockEntityRenderData(pos)
				instanceof PlatformBlockEntity.PlatformData platformData) {
			BlockState heldState = platformData.state();
			if (heldState != null && !heldState.is(BotaniaTags.Blocks.UNSUPPORTED_PLATFORM_DISGUISE)) {
				Object subKey = modelSet.get(heldState).createGeometryKey(
						level, platformData.pos(), heldState, random);
				return subKey == null ? null : new PlatformGeometryKey(heldState, subKey);
			}
		}

		return super.createGeometryKey(level, pos, state, random);
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		BlockStateModel model = contextualModel(level, pos, state);
		if (model == null) {
			return super.particleMaterial(level, pos, state);
		}
		PlatformBlockEntity.PlatformData platformData = platformData(level, pos);
		return model.particleMaterial(level,
				platformData == null ? pos : platformData.pos(),
				platformData == null ? state : platformData.state());
	}

	@Override
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		BlockStateModel model = contextualModel(level, pos, state);
		if (model == null) {
			return super.materialFlags(level, pos, state);
		}
		PlatformBlockEntity.PlatformData platformData = platformData(level, pos);
		return model.materialFlags(level,
				platformData == null ? pos : platformData.pos(),
				platformData == null ? state : platformData.state());
	}

	private static @Nullable BlockStateModel contextualModel(
			BlockAndTintGetter level, BlockPos pos, BlockState state) {
		var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
		if (!(state.getBlock() instanceof PlatformBlock)) {
			return modelSet.missingModel();
		}
		PlatformBlockEntity.PlatformData data = platformData(level, pos);
		if (data != null && data.state() != null
				&& !data.state().is(BotaniaTags.Blocks.UNSUPPORTED_PLATFORM_DISGUISE)) {
			return modelSet.get(data.state());
		}
		return null;
	}

	private static @Nullable PlatformBlockEntity.PlatformData platformData(
			BlockAndTintGetter level, BlockPos pos) {
		Object data = ((FabricBlockGetter) level).getBlockEntityRenderData(pos);
		return data instanceof PlatformBlockEntity.PlatformData platformData ? platformData : null;
	}

	private record PlatformGeometryKey(BlockState state, Object subKey) {}
}
