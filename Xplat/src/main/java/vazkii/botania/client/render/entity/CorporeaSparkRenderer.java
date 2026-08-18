/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;

import vazkii.botania.common.entity.CorporeaSparkEntity;


import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class CorporeaSparkRenderer extends BaseSparkRenderer<CorporeaSparkEntity> {
	private final TextureAtlasSprite corporeaWorldSprite;
	private final TextureAtlasSprite corporeaMasterWorldSprite;
	private final TextureAtlasSprite corporeaCreativeWorldSprite;

	public CorporeaSparkRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		this.corporeaWorldSprite = ctx.getSprites().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("item/corporea_spark")));
		this.corporeaMasterWorldSprite = ctx.getSprites().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("item/corporea_spark_master")));
		this.corporeaCreativeWorldSprite = ctx.getSprites().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("item/corporea_spark_creative")));
	}

	@Override
	public TextureAtlasSprite getBaseIcon(CorporeaSparkEntity entity) {
		if (entity.isCreative()) {
			return this.corporeaCreativeWorldSprite;
		} else if (entity.isMaster()) {
			return this.corporeaMasterWorldSprite;
		} else {
			return this.corporeaWorldSprite;
		}
	}

}
