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
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.render.block_entity.TinyPotatoBlockEntityRenderer;

/** Baked item model which substitutes the contributor Tiny Potato geometry when named. */
public final class TinyPotatoModel extends WrapperBakedItemModel {
	public TinyPotatoModel(ItemModel wrapped) {
		super(wrapped);
	}

	@Override
	public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
			ItemDisplayContext displayContext, ClientLevel level, ItemOwner itemOwner, int seed) {
		if (!stack.has(DataComponents.CUSTOM_NAME) && !ClientProxy.dootDoot) {
			super.update(renderState, stack, resolver, displayContext, level, itemOwner, seed);
			return;
		}

		TinyPotatoBlockEntityRenderer.getItemModelFromDisplayName(stack.getHoverName())
				.update(renderState, stack, resolver, displayContext, level, itemOwner, seed);
	}
}
