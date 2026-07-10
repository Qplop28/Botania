/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockModelGenerators.class)
public interface BlockModelGeneratorsAccessor {
	@Invoker("createSlab")
	static BlockStateGenerator makeSlabState(Block block, Identifier bottomModel, Identifier topModel, Identifier doubleModel) {
		throw new IllegalStateException();
	}

	@Invoker("createFenceGate")
	static BlockStateGenerator makeFenceGateState(Block block, Identifier openModel, Identifier closedModel, Identifier openWallModel, Identifier closedWallModel, boolean uvLock) {
		throw new IllegalStateException();
	}

	@Invoker("createFence")
	static BlockStateGenerator makeFenceState(Block block, Identifier postModel, Identifier sideModel) {
		throw new IllegalStateException();
	}

	@Invoker("createAxisAlignedPillarBlock")
	static BlockStateGenerator createAxisAlignedPillarBlock(Block block, Identifier model) {
		throw new IllegalStateException();
	}

	@Invoker("createHorizontalFacingDispatch")
	static PropertyDispatch horizontalDispatch() {
		throw new IllegalStateException();
	}

	@Invoker("createFacingDispatch")
	static PropertyDispatch facingDispatch() {
		throw new IllegalStateException();
	}

	@Invoker("createRotatedVariants")
	static Variant[] createRotatedVariants(Identifier model) {
		throw new IllegalStateException();
	}
}
