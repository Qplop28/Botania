package vazkii.botania.client.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public record BotaniaItemTintSource(int tintIndex) implements ItemTintSource {
	public static final MapCodec<BotaniaItemTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("tint_index").forGetter(BotaniaItemTintSource::tintIndex)
	).apply(instance, BotaniaItemTintSource::new));

	@Override
	public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
		return ColorHandler.getItemColor(stack, tintIndex);
	}

	@Override
	public MapCodec<? extends ItemTintSource> type() {
		return MAP_CODEC;
	}
}
