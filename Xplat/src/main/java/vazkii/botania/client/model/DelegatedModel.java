/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.client.renderer.item.ItemModel;

/**
 * @deprecated Extend Fabric's typed item-model wrapper directly. Kept temporarily for the mana
 * blaster model, whose composite item-model migration is a separate compiler checkpoint.
 */
@Deprecated(forRemoval = true)
public class DelegatedModel extends WrapperBakedItemModel {
	public DelegatedModel(ItemModel wrapped) {
		super(wrapped);
	}
}
