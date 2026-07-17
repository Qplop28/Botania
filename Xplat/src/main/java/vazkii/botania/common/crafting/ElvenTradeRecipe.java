/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;

import vazkii.botania.common.block.BotaniaBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ElvenTradeRecipe
		implements vazkii.botania.api.recipe.ElvenTradeRecipe {
	private static final Codec<List<ItemStackTemplate>> OUTPUT_CODEC =
			Codec.either(
					ItemStackTemplate.CODEC.listOf(),
					ItemStackTemplate.CODEC
			).xmap(
					either -> either.map(
							outputs -> outputs,
							List::of
					),
					Either::left
			);

	private static final MapCodec<ElvenTradeRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					OUTPUT_CODEC.fieldOf("output")
							.forGetter(recipe -> recipe.outputs),
					Ingredient.CODEC.listOf().fieldOf("ingredients")
							.forGetter(recipe -> recipe.inputs)
			).apply(instance, ElvenTradeRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, ElvenTradeRecipe>
			STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<ElvenTradeRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final List<ItemStackTemplate> outputs;
	private final NonNullList<Ingredient> inputs;
	private final PlacementInfo placementInfo;

	public ElvenTradeRecipe(
			List<ItemStackTemplate> outputs,
			List<Ingredient> inputs
	) {
		this.outputs = List.copyOf(outputs);
		this.inputs = NonNullList.create();
		this.inputs.addAll(inputs);
		this.placementInfo = PlacementInfo.create(this.inputs);
	}

	/** Transitional constructor for existing data generators and addons. */
	@Deprecated
	public ElvenTradeRecipe(
			Identifier ignoredId,
			ItemStack[] outputs,
			Ingredient... inputs
	) {
		this(
				Arrays.stream(outputs)
						.map(ItemStackTemplate::fromNonEmptyStack)
						.toList(),
				List.of(inputs)
		);
	}

	@Override
	public Optional<List<ItemStack>> match(List<ItemStack> stacks) {
		List<Ingredient> inputsMissing = new ArrayList<>(this.inputs);
		List<ItemStack> stacksToRemove = new ArrayList<>();

		for (ItemStack stack : stacks) {
			if (stack.isEmpty()) {
				continue;
			}
			if (inputsMissing.isEmpty()) {
				break;
			}

			for (int index = 0; index < inputsMissing.size(); index++) {
				if (inputsMissing.get(index).test(stack)) {
					if (!stacksToRemove.contains(stack)) {
						stacksToRemove.add(stack);
					}
					inputsMissing.remove(index);
					break;
				}
			}
		}

		return inputsMissing.isEmpty()
				? Optional.of(stacksToRemove)
				: Optional.empty();
	}

	@Override
	public boolean containsItem(ItemStack stack) {
		return this.inputs.stream().anyMatch(input -> input.test(stack));
	}

	@Override
	public RecipeSerializer<ElvenTradeRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public PlacementInfo placementInfo() {
		return this.placementInfo;
	}

	@Override
	@Deprecated
	public NonNullList<Ingredient> getIngredients() {
		return this.inputs;
	}

	/** Transitional toast icon accessor for older integrations. */
	@Deprecated
	public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.alfPortal);
	}

	@Override
	public List<ItemStack> getOutputs() {
		return this.outputs.stream().map(ItemStackTemplate::create).toList();
	}

	@Override
	public List<ItemStack> getOutputs(List<ItemStack> inputs) {
		return getOutputs();
	}
}
