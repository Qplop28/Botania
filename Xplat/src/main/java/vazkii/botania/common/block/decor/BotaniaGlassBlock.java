/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.decor;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BotaniaGlassBlock
		extends TransparentBlock {
	public static final MapCodec<BotaniaGlassBlock> CODEC =
			simpleCodec(BotaniaGlassBlock::new);

	public BotaniaGlassBlock(
			BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<BotaniaGlassBlock> codec() {
		return CODEC;
	}
}