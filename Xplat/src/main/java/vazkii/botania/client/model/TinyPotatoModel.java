/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.render.block_entity.TinyPotatoBlockEntityRenderer;

/** Baked item model which substitutes the contributor Tiny Potato geometry when named. */
public final class TinyPotatoModel extends WrapperBakedItemModel {
	public TinyPotatoModel(ItemModel wrapped) {
		super(wrapped);
	}

	@Override
	public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
			ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		if (!stack.hasCustomHoverName() && !ClientProxy.dootDoot) {
			super.update(renderState, stack, resolver, displayContext, level, entity, seed);
			return;
		}

		var model = TinyPotatoBlockEntityRenderer.getModelFromDisplayName(stack.getHoverName());
		renderState.appendModelIdentityElement(model);
		renderState.newLayer().setModel(model);
	}
}
