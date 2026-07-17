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
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.Optional;

public class ManaInfusionRecipe
		implements vazkii.botania.api.recipe.ManaInfusionRecipe {
	private static final MapCodec<ManaInfusionRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					Ingredient.CODEC.fieldOf("input")
							.forGetter(recipe -> recipe.input),
					ItemStackTemplate.CODEC.fieldOf("output")
							.forGetter(recipe -> recipe.output),
					Codec.INT.fieldOf("mana")
							.forGetter(recipe -> recipe.mana),
					Codec.STRING.optionalFieldOf("group", "")
							.forGetter(recipe -> recipe.group),
					StateIngredientHelper.CODEC.optionalFieldOf("catalyst")
							.forGetter(recipe -> Optional.ofNullable(recipe.catalyst))
			).apply(instance, (input, output, mana, group, catalyst) ->
					new ManaInfusionRecipe(
							output, input, mana, group, catalyst.orElse(null))));

	private static final StreamCodec<RegistryFriendlyByteBuf, ManaInfusionRecipe>
			STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<ManaInfusionRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ItemStackTemplate output;
	private final Ingredient input;
	private final int mana;
	@Nullable
	private final StateIngredient catalyst;
	private final String group;
	private final PlacementInfo placementInfo;

	public ManaInfusionRecipe(
			ItemStackTemplate output,
			Ingredient input,
			int mana,
			String group,
			@Nullable StateIngredient catalyst
	) {
		Preconditions.checkArgument(mana > 0, "Mana cost must be positive");
		Preconditions.checkArgument(
				mana <= 1_000_001,
				"Mana cost must be at most a pool"
		);
		this.output = output;
		this.input = input;
		this.mana = mana;
		this.group = group;
		this.catalyst = catalyst;
		this.placementInfo = PlacementInfo.create(input);
	}

	/** Transitional constructor for existing data generators and addons. */
	@Deprecated
	public ManaInfusionRecipe(
			Identifier ignoredId,
			ItemStack output,
			Ingredient input,
			int mana,
			@Nullable String group,
			@Nullable StateIngredient catalyst
	) {
		this(
				ItemStackTemplate.fromNonEmptyStack(output),
				input,
				mana,
				group == null ? "" : group,
				catalyst
		);
	}

	@Override
	public RecipeSerializer<ManaInfusionRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(ItemStack stack) {
		return this.input.test(stack);
	}

	@Override
	@Nullable
	public StateIngredient getRecipeCatalyst() {
		return this.catalyst;
	}

	@Override
	public int getManaToConsume() {
		return this.mana;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registries) {
		return this.output.create();
	}

	@Override
	public PlacementInfo placementInfo() {
		return this.placementInfo;
	}

	@Override
	public String group() {
		return this.group;
	}

	/** Transitional accessor for existing integrations. */
	@Deprecated
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		ingredients.add(this.input);
		return ingredients;
	}

	/** Transitional accessor for callers using the old recipe API. */
	@Deprecated
	public String getGroup() {
		return this.group;
	}

	/** Transitional toast icon accessor for older integrations. */
	@Deprecated
	public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.manaPool);
	}
}
