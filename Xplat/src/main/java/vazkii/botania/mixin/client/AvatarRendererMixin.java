package vazkii.botania.mixin.client;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.render.accessory.AccessoryExtractionContext;
import vazkii.botania.client.render.accessory.AccessoryRenderExtraction;
import vazkii.botania.client.render.accessory.AccessoryRenderLayer;
import vazkii.botania.client.render.accessory.BotaniaAvatarRenderState;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity>
		extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {
	@Unique
	private AccessoryExtractionContext botania$accessoryContext;

	protected AvatarRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
		super(context, model, shadowRadius);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void botania$addAccessoryLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
		botania$accessoryContext = new AccessoryExtractionContext(
				context.getItemModelResolver(), context.getBlockModelResolver(), context.getModelSet());
		addLayer(new AccessoryRenderLayer(this));
	}

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("TAIL"))
	private void botania$extractAccessories(AvatarlikeEntity entity, AvatarRenderState state,
			float partialTicks, CallbackInfo ci) {
		var output = ((BotaniaAvatarRenderState) (Object) state).botania$getAccessoryEntries();
		output.clear();
		if (entity instanceof Player player) {
			AccessoryRenderExtraction.extract(player, partialTicks, botania$accessoryContext, output);
		}
	}
}
