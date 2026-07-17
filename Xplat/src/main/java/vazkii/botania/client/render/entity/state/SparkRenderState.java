/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.jetbrains.annotations.Nullable;

public class SparkRenderState extends EntityRenderState {
	public TextureAtlasSprite baseSprite;
	@Nullable
	public TextureAtlasSprite spinningSprite;
	public double animationTime;
	public int networkColor;
}
