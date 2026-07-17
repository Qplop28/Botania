/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.fabric.client;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

/**
 * Marks platform block-state models as Fabric wrappers. Platform disguise submission is handled
 * by the platform block entity's render data rather than by the vanilla model registry.
 */
public final class FabricPlatformModel extends WrapperBlockStateModel {
	public FabricPlatformModel(BlockStateModel wrapped) {
		super(wrapped);
	}
}
