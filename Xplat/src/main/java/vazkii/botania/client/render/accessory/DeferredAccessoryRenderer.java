package vazkii.botania.client.render.accessory;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface DeferredAccessoryRenderer {
	default AccessoryRenderData createData() {
		return new AccessoryRenderData();
	}

	void extract(AccessoryRenderData data, ItemStack stack, Player player, float partialTicks,
			AccessoryExtractionContext context);

	void submit(AccessoryRenderData data, ItemStack stack, PlayerModel model, AvatarRenderState state,
			PoseStack poseStack, SubmitNodeCollector collector, int lightCoords);
}
