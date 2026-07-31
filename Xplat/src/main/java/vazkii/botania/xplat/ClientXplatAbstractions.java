package vazkii.botania.xplat;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.ServiceUtil;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.network.BotaniaPacket;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public interface ClientXplatAbstractions {
	Identifier FLOATING_FLOWER_MODEL_LOADER_ID = prefix("floating_flower");
	Identifier MANA_GUN_MODEL_LOADER_ID = prefix("mana_gun");

	// Event firing
	void fireRenderTinyPotato(BlockPos pos, Component name, String contributor, float tickDelta,
			PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay);

	// Networking
	void sendToServer(BotaniaPacket packet);

	// Capability access
	@Nullable
	WandHUD findWandHud(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be);

	@Nullable
	WandHUD findWandHud(Entity entity);

	// Rendering stuff
	BlockStateModel wrapPlatformModel(BlockStateModel original);
	void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap);
	void restoreLastFilter(AbstractTexture texture);
	void tessellateBlock(Level level, BlockState state, BlockPos pos, PoseStack ps, MultiBufferSource buffers, int overlay);
	/** Marks an animated sprite as "active" for Sodium, if present. */
	void markSpriteActive(TextureAtlasSprite sprite);

	ClientXplatAbstractions INSTANCE = ServiceUtil.findService(ClientXplatAbstractions.class, null);

	static ClientXplatAbstractions instance() {
		return INSTANCE;
	}
}
