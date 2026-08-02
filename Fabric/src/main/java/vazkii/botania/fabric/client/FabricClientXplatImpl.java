package vazkii.botania.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaFabricClientCapabilities;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.item.TinyPotatoRenderCallback;
import vazkii.botania.network.BotaniaPacket;
import vazkii.botania.xplat.ClientXplatAbstractions;

public class FabricClientXplatImpl implements ClientXplatAbstractions {
	@Override
	public void fireRenderTinyPotato(BlockPos pos, Component name, String contributor, float tickDelta,
			PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay) {
		TinyPotatoRenderCallback.EVENT.invoker().onRender(pos, name, contributor, tickDelta,
				poseStack, collector, light, overlay);
	}

	@Override
	public void sendToServer(BotaniaPacket packet) {
		ClientPlayNetworking.send(packet);
	}

	@Nullable
	@Override
	public WandHUD findWandHud(Level level, BlockPos pos, BlockState state, BlockEntity be) {
		return BotaniaFabricClientCapabilities.WAND_HUD.find(level, pos, state, be, Unit.INSTANCE);
	}

	@Nullable
	@Override
	public WandHUD findWandHud(Entity entity) {
		return BotaniaFabricClientCapabilities.ENTITY_WAND_HUD.find(entity, Unit.INSTANCE);
	}

	@Override
	public BlockStateModel wrapPlatformModel(BlockStateModel original) {
		return new FabricPlatformModel(original);
	}

	@Override
	public void tessellateBlock(
			Level level,
			BlockState state,
			BlockPos pos,
			PoseStack ps,
			MultiBufferSource buffers,
			int overlay
	) {
		// Block model submission must be migrated to the Minecraft 26.1
		// render-state pipeline.
	}

	@Override
	public void markSpriteActive(TextureAtlasSprite sprite) {
		// Sodium integration is disabled during the Minecraft 26.1 bootstrap.
	}
}
