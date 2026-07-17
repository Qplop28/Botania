/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.recipe.RecipeUtils;

import java.util.Arrays;
import java.util.List;

public class RunicAltarRecipe
		implements vazkii.botania.api.recipe.RunicAltarRecipe {
	protected static final MapCodec<RunicAltarRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					ItemStackTemplate.CODEC.fieldOf("output")
							.forGetter(recipe -> recipe.output),
					Codec.INT.fieldOf("mana")
							.forGetter(recipe -> recipe.mana),
					Ingredient.CODEC.listOf().fieldOf("ingredients")
							.forGetter(recipe -> recipe.inputs)
			).apply(instance, RunicAltarRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, RunicAltarRecipe>
			STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<RunicAltarRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	protected final ItemStackTemplate output;
	protected final NonNullList<Ingredient> inputs;
	private final int mana;
	private final PlacementInfo placementInfo;

	public RunicAltarRecipe(
			ItemStackTemplate output,
			int mana,
			List<Ingredient> inputs
	) {
		Preconditions.checkArgument(
				inputs.size() <= 16,
				"Cannot have more than 16 ingredients"
		);
		this.output = output;
		this.inputs = NonNullList.create();
		this.inputs.addAll(inputs);
		this.mana = mana;
		this.placementInfo = PlacementInfo.create(this.inputs);
	}

	/** Transitional constructor for existing data generators and addons. */
	@Deprecated
	public RunicAltarRecipe(
			Identifier ignoredId,
			ItemStack output,
			int mana,
			Ingredient... inputs
	) {
		this(
				ItemStackTemplate.fromNonEmptyStack(output),
				mana,
				Arrays.asList(inputs)
		);
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		return RecipeUtils.matches(this.inputs, input, null);
	}

	@Override
	public ItemStack assemble(RecipeInput input) {
		return this.output.create();
	}

	@Override
	public PlacementInfo placementInfo() {
		return this.placementInfo;
	}

	@Deprecated
	public ItemStack getResultItem(RegistryAccess registries) {
		return this.output.create();
	}

	@Override
	@Deprecated
	public NonNullList<Ingredient> getIngredients() {
		return this.inputs;
	}

	/** Transitional toast icon accessor for older integrations. */
	@Deprecated
	public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.runeAltar);
	}

	@Override
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public int getManaUsage() {
		return this.mana;
	}
}
