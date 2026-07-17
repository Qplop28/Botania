package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.PixieModel;
import vazkii.botania.client.render.entity.state.PixieRenderState;
import vazkii.botania.common.entity.PixieEntity;
import vazkii.botania.xplat.BotaniaConfig;

public class PixieRenderer extends MobRenderer<PixieEntity, PixieRenderState, PixieModel> {
	public PixieRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new PixieModel(ctx.bakeLayer(BotaniaModelLayers.PIXIE)), 0.0F);
	}

	@Override
	public PixieRenderState createRenderState() {
		return new PixieRenderState();
	}

	@Override
	public void extractRenderState(PixieEntity entity, PixieRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.evil = entity.getPixieType() == 1;
		state.texture = ClientProxy.dootDoot ? new Identifier(ResourcesLib.MODEL_PIXIE_HALLOWEEN)
				: new Identifier(ResourcesLib.MODEL_PIXIE);
		state.grainIntensity = GaiaGuardianRenderer.DEFAULT_GRAIN_INTENSITY;
		state.disfiguration = GaiaGuardianRenderer.DEFAULT_DISFIGURATION;
	}

	@Override
	public Identifier getTextureLocation(PixieRenderState state) {
		return state.texture;
	}

	@Override
	protected RenderType getRenderType(PixieRenderState state, boolean bodyVisible,
			boolean forceTransparent, boolean glowing) {
		if (state.evil && bodyVisible && !forceTransparent) {
			return RenderHelper.getDopplegangerLayer(state.texture);
		}

		return super.getRenderType(state, bodyVisible, forceTransparent, glowing);
	}

	@Override
	protected int getModelTint(PixieRenderState state) {
		return state.evil && !state.isInvisible && BotaniaConfig.client().useShaders()
				? GaiaGuardianRenderer.shaderValues(state.grainIntensity, state.disfiguration)
				: -1;
	}
}
