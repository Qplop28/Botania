/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockModelGenerators.class)
public interface BlockModelGeneratorsAccessor {
	@Invoker("createSlab")
	static BlockModelDefinitionGenerator makeSlabState(
			Block block,
			MultiVariant bottomModel,
			MultiVariant topModel,
			MultiVariant doubleModel
	) {
		throw new IllegalStateException();
	}

	@Invoker("createFenceGate")
	static BlockModelDefinitionGenerator makeFenceGateState(
			Block block,
			MultiVariant openModel,
			MultiVariant closedModel,
			MultiVariant openWallModel,
			MultiVariant closedWallModel,
			boolean uvLock
	) {
		throw new IllegalStateException();
	}

	@Invoker("createFence")
	static BlockModelDefinitionGenerator makeFenceState(
			Block block,
			MultiVariant postModel,
			MultiVariant sideModel
	) {
		throw new IllegalStateException();
	}

	@Invoker("createAxisAlignedPillarBlock")
	static BlockModelDefinitionGenerator createAxisAlignedPillarBlock(
			Block block,
			MultiVariant model
	) {
		throw new IllegalStateException();
	}

	@Accessor("ROTATION_HORIZONTAL_FACING")
	static PropertyDispatch<VariantMutator> horizontalDispatch() {
		throw new IllegalStateException();
	}

	@Accessor("ROTATION_FACING")
	static PropertyDispatch<VariantMutator> facingDispatch() {
		throw new IllegalStateException();
	}

	@Invoker("createRotatedVariants")
	static MultiVariant createRotatedVariants(Variant model) {
		throw new IllegalStateException();
	}
}