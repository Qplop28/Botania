package vazkii.botania.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.level.Level;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.fx.BoltRenderer;
import vazkii.botania.common.item.AssemblyHaloItem;

public final class WorldOverlays {
	public static void renderWorldLast(Camera camera, float tickDelta, PoseStack matrix, RenderBuffers buffers, Level level,
			SubmitNodeCollector submitNodeCollector) {
		BoltRenderer.onWorldRenderLast(camera, tickDelta, matrix, buffers);
		AssemblyHaloItem.Rendering.onRenderWorldLast(camera, tickDelta, matrix, buffers, submitNodeCollector);
		BoundBlockRenderer.onWorldRenderLast(camera, matrix, level);
		AstrolabePreviewHandler.onWorldRenderLast(matrix, level, submitNodeCollector);
		RenderHelper.onWorldRenderLast();
	}

	private WorldOverlays() {}
}
