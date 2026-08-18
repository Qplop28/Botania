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

import vazkii.botania.common.entity.ManaSparkEntity;


import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ManaSparkRenderer extends BaseSparkRenderer<ManaSparkEntity> {
	private final TextureAtlasSprite dispersiveIcon;
	private final TextureAtlasSprite dominantIcon;
	private final TextureAtlasSprite recessiveIcon;
	private final TextureAtlasSprite isolatedIcon;

	public ManaSparkRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		this.dispersiveIcon = ctx.getSprites().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("item/spark_upgrade_rune_dispersive")));
		this.dominantIcon = ctx.getSprites().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("item/spark_upgrade_rune_dominant")));
		this.recessiveIcon = ctx.getSprites().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("item/spark_upgrade_rune_recessive")));
		this.isolatedIcon = ctx.getSprites().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, prefix("item/spark_upgrade_rune_isolated")));
	}

	@Override
	public TextureAtlasSprite getSpinningIcon(ManaSparkEntity entity) {
		return switch (entity.getUpgrade()) {
			case NONE -> null;
			case DISPERSIVE -> this.dispersiveIcon;
			case DOMINANT -> this.dominantIcon;
			case RECESSIVE -> this.recessiveIcon;
			case ISOLATED -> this.isolatedIcon;
		};
	}

}
