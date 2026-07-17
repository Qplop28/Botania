/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.model.armor.ArmorModels;
import vazkii.botania.client.render.entity.state.GaiaGuardianRenderState;
import vazkii.botania.common.entity.GaiaGuardianEntity;
import vazkii.botania.xplat.BotaniaConfig;

public class GaiaGuardianRenderer extends HumanoidMobRenderer<GaiaGuardianEntity, GaiaGuardianRenderState,
		HumanoidModel<GaiaGuardianRenderState>> {
	public static final float DEFAULT_GRAIN_INTENSITY = 0.05F;
	public static final float DEFAULT_DISFIGURATION = 0.025F;

	private final Model normalModel;
	private final Model slimModel;

	public GaiaGuardianRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new Model(ctx.bakeLayer(ModelLayers.PLAYER)), 0F);
		this.normalModel = (Model) this.getModel();
		this.slimModel = new Model(ctx.bakeLayer(ModelLayers.PLAYER_SLIM));
		ArmorModels.init(ctx);
	}

	@Override
	public GaiaGuardianRenderState createRenderState() {
		return new GaiaGuardianRenderState();
	}

	@Override
	public void extractRenderState(GaiaGuardianEntity entity, GaiaGuardianRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		int invulTime = entity.getInvulTime();
		if (invulTime > 0) {
			state.grainIntensity = invulTime > 20 ? 1F : invulTime * 0.05F;
			state.disfiguration = state.grainIntensity * 0.3F;
		} else {
			state.disfiguration = (0.025F + entity.hurtTime * ((1F - 0.15F) / 20F)) / 2F;
			state.grainIntensity = 0.05F + entity.hurtTime * ((1F - 0.15F) / 10F);
		}

		var view = Minecraft.getInstance().getCameraEntity();
		if (view instanceof AbstractClientPlayer player) {
			var skin = player.getSkin();
			state.slimModel = skin.model() == PlayerModelType.SLIM;
			state.texture = skin.body().texturePath();
		} else {
			state.slimModel = false;
			state.texture = DefaultPlayerSkin.get(entity.getUUID())
					.body()
					.texturePath();
		}
	}

	@Override
	public Identifier getTextureLocation(GaiaGuardianRenderState state) {
		return state.texture;
	}

	@Override
	protected boolean isBodyVisible(GaiaGuardianRenderState state) {
		return true;
	}

	@Override
	protected int getModelTint(GaiaGuardianRenderState state) {
		return BotaniaConfig.client().useShaders()
				? shaderValues(state.grainIntensity, state.disfiguration)
				: -1;
	}

	static int shaderValues(float grainIntensity, float disfiguration) {
		return 0xFF000000 | Math.round(grainIntensity * 255F) << 16
				| Math.round(disfiguration * 255F) << 8 | 0xFF;
	}

	@Override
	public void submit(GaiaGuardianRenderState state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		HumanoidModel<GaiaGuardianRenderState> previous = this.model;
		this.model = state.slimModel ? slimModel : normalModel;
		try {
			super.submit(state, poseStack, submitNodeCollector, camera);
		} finally {
			this.model = previous;
		}
	}

	private static class Model extends HumanoidModel<GaiaGuardianRenderState> {
		Model(ModelPart root) {
			super(root, RenderHelper::getDopplegangerLayer);
		}
	}
}
