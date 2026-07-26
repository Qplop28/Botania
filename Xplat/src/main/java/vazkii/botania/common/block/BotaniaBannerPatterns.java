/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BannerPattern;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public final class BotaniaBannerPatterns {
	public static final ResourceKey<BannerPattern> FLOWER = make("flower");
	public static final ResourceKey<BannerPattern> LEXICON = make("lexicon");
	public static final ResourceKey<BannerPattern> LOGO = make("logo");
	public static final ResourceKey<BannerPattern> SAPLING = make("sapling");
	public static final ResourceKey<BannerPattern> TINY_POTATO = make("tiny_potato");
	public static final ResourceKey<BannerPattern> SPARK_DISPERSIVE = make("spark_dispersive");
	public static final ResourceKey<BannerPattern> SPARK_DOMINANT = make("spark_dominant");
	public static final ResourceKey<BannerPattern> SPARK_RECESSIVE = make("spark_recessive");
	public static final ResourceKey<BannerPattern> SPARK_ISOLATED = make("spark_isolated");

	public static final ResourceKey<BannerPattern> FISH = make("fish");
	public static final ResourceKey<BannerPattern> AXE = make("axe");
	public static final ResourceKey<BannerPattern> HOE = make("hoe");
	public static final ResourceKey<BannerPattern> PICKAXE = make("pickaxe");
	public static final ResourceKey<BannerPattern> SHOVEL = make("shovel");
	public static final ResourceKey<BannerPattern> SWORD = make("sword");

	public static void bootstrap(BootstapContext<BannerPattern> context) {
		register(context, FLOWER, "flower");
		register(context, LEXICON, "lexicon");
		register(context, LOGO, "logo");
		register(context, SAPLING, "sapling");
		register(context, TINY_POTATO, "tiny_potato");
		register(context, SPARK_DISPERSIVE, "spark_dispersive");
		register(context, SPARK_DOMINANT, "spark_dominant");
		register(context, SPARK_RECESSIVE, "spark_recessive");
		register(context, SPARK_ISOLATED, "spark_isolated");
		register(context, FISH, "fish");
		register(context, AXE, "axe");
		register(context, HOE, "hoe");
		register(context, PICKAXE, "pickaxe");
		register(context, SHOVEL, "shovel");
		register(context, SWORD, "sword");
	}

	private static void register(BootstapContext<BannerPattern> context,
			ResourceKey<BannerPattern> key, String name) {
		context.register(key, new BannerPattern(
				prefix(name),
				"block.minecraft.banner.botania." + name));
	}

	private static ResourceKey<BannerPattern> make(String name) {
		return ResourceKey.create(
				Registries.BANNER_PATTERN,
				prefix(name)
		);
	}
}
