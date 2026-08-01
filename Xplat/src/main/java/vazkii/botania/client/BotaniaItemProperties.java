package vazkii.botania.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BaubleBoxItem;
import vazkii.botania.common.item.BlackHoleTalismanItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.BottledManaItem;
import vazkii.botania.common.item.LexicaBotaniaItem;
import vazkii.botania.common.item.LifeAggregatorItem;
import vazkii.botania.common.item.ManufactoryHaloItem;
import vazkii.botania.common.item.SlimeInABottleItem;
import vazkii.botania.common.item.StoneOfTemperanceItem;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.common.item.brew.BaseBrewItem;
import vazkii.botania.common.item.equipment.bauble.RingOfMagnetizationItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraTruncatorItem;
import vazkii.botania.common.item.relic.FruitOfGrisaiaItem;
import vazkii.botania.common.item.rod.SkiesRodItem;

import java.util.Locale;

public final class BotaniaItemProperties {
	public record Conditional(Kind kind) implements ConditionalItemModelProperty {
		public static final MapCodec<Conditional> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Kind.CODEC.fieldOf("kind").forGetter(Conditional::kind)
		).apply(instance, Conditional::new));

		@Override
		public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity,
				int seed, ItemDisplayContext displayContext) {
			return switch (kind) {
				case OPEN -> ItemNBTHelper.getBoolean(stack, BaubleBoxItem.TAG_OPEN, false);
				case ACTIVE -> isActive(stack, entity);
				case VUVUZELA -> normalizedName(stack).contains("vuvuzela");
				case ELVEN -> LexicaBotaniaItem.isElven(stack);
				case TOTALBISCUIT -> normalizedName(stack).contains("totalbiscuit");
				case FULL -> isFull(stack);
				case BINDMODE -> WandOfTheForestItem.getBindMode(stack);
				case HOLIDAY -> ClientProxy.jingleTheBells;
				case REDDIT -> stack.getHoverName().getString().equalsIgnoreCase("dammit reddit");
				case ELUCIDATOR -> normalizedName(stack).trim().equals("the elucidator");
				case TIPPED -> TerraShattererItem.isTipped(stack);
				case BOOT -> FruitOfGrisaiaItem.isBoot(stack);
			};
		}

		@Override
		public MapCodec<? extends ConditionalItemModelProperty> type() {
			return MAP_CODEC;
		}
	}

	public record SwigsTaken() implements RangeSelectItemModelProperty {
		public static final MapCodec<SwigsTaken> MAP_CODEC = MapCodec.unit(new SwigsTaken());

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			int swigsLeft;
			int totalSwigs;
			if (stack.is(BotaniaItems.manaBottle)) {
				swigsLeft = BottledManaItem.getSwigsLeft(stack) - 1;
				totalSwigs = BottledManaItem.SWIGS - 1;
			} else if (stack.getItem() instanceof BaseBrewItem item) {
				swigsLeft = item.getSwigsLeft(stack) - 1;
				totalSwigs = item.getSwigs() - 1;
			} else {
				return 0;
			}
			return swigsLeft == totalSwigs
					? 0
					: Math.nextUp((totalSwigs - swigsLeft) / (float) totalSwigs);
		}

		@Override
		public MapCodec<? extends RangeSelectItemModelProperty> type() {
			return MAP_CODEC;
		}
	}

	public enum Kind implements StringRepresentable {
		OPEN("open"),
		ACTIVE("active"),
		VUVUZELA("vuvuzela"),
		ELVEN("elven"),
		TOTALBISCUIT("totalbiscuit"),
		FULL("full"),
		BINDMODE("bindmode"),
		HOLIDAY("holiday"),
		REDDIT("reddit"),
		ELUCIDATOR("elucidator"),
		TIPPED("tipped"),
		BOOT("boot");

		private static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
		private final String serializedName;

		Kind(String serializedName) {
			this.serializedName = serializedName;
		}

		@Override
		public String getSerializedName() {
			return serializedName;
		}
	}

	private static boolean isActive(ItemStack stack, @Nullable LivingEntity entity) {
		if (stack.is(BotaniaItems.blackHoleTalisman)) {
			return ItemNBTHelper.getBoolean(stack, BlackHoleTalismanItem.TAG_ACTIVE, false);
		} else if (stack.is(BotaniaItems.slimeBottle)) {
			return ItemNBTHelper.getBoolean(stack, SlimeInABottleItem.TAG_ACTIVE, false);
		} else if (stack.is(BotaniaItems.temperanceStone)) {
			return ItemNBTHelper.getBoolean(stack, StoneOfTemperanceItem.TAG_ACTIVE, false);
		} else if (stack.is(BotaniaItems.autocraftingHalo)) {
			return ItemNBTHelper.getBoolean(stack, ManufactoryHaloItem.TAG_ACTIVE, true);
		} else if (stack.is(BotaniaItems.magnetRing) || stack.is(BotaniaItems.magnetRingGreater)) {
			return RingOfMagnetizationItem.getCooldown(stack) <= 0;
		} else if (stack.is(BotaniaItems.terraAxe)) {
			return !(entity instanceof Player player) || TerraTruncatorItem.shouldBreak(player);
		} else if (stack.is(BotaniaItems.terraPick)) {
			return TerraShattererItem.isEnabled(stack);
		} else if (stack.is(BotaniaItems.tornadoRod)) {
			return SkiesRodItem.isFlying(stack);
		}
		return false;
	}

	private static boolean isFull(ItemStack stack) {
		if (stack.is(BotaniaItems.spawnerMover)) {
			return LifeAggregatorItem.hasData(stack);
		}
		return ItemNBTHelper.getBoolean(stack, "RenderFull", false);
	}

	private static String normalizedName(ItemStack stack) {
		return stack.getHoverName().getString().toLowerCase(Locale.ROOT);
	}

	private BotaniaItemProperties() {}
}
