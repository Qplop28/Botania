package vazkii.botania.client.render.accessory;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public class AccessoryRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	public AccessoryRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state,
			float yRot, float xRot) {
		var entries = ((BotaniaAvatarRenderState) (Object) state).botania$getAccessoryEntries();
		for (AccessoryRenderEntry entry : entries) {
			poseStack.pushPose();
			try {
				entry.renderer().submit(entry.data(), entry.stack(), getParentModel(), state,
						poseStack, collector, lightCoords);
			} finally {
				poseStack.popPose();
			}
		}
	}
}
