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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.PetalApothecaryRecipe;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PetalsRecipe implements PetalApothecaryRecipe {
	private static final MapCodec<PetalsRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					ItemStackTemplate.CODEC.fieldOf("output")
							.forGetter(recipe -> recipe.output),
					Ingredient.CODEC.fieldOf("reagent")
							.forGetter(recipe -> recipe.reagent),
					Ingredient.CODEC.listOf().fieldOf("ingredients")
							.forGetter(recipe -> recipe.inputs)
			).apply(instance, PetalsRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, PetalsRecipe>
			STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<PetalsRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ItemStackTemplate output;
	private final Ingredient reagent;
	private final NonNullList<Ingredient> inputs;
	private final PlacementInfo placementInfo;

	public PetalsRecipe(
			ItemStackTemplate output,
			Ingredient reagent,
			List<Ingredient> inputs
	) {
		Preconditions.checkArgument(
				inputs.size() <= 16,
				"Cannot have more than 16 ingredients"
		);
		this.output = output;
		this.reagent = reagent;
		this.inputs = NonNullList.create();
		this.inputs.addAll(inputs);
		this.placementInfo = PlacementInfo.create(this.inputs);
	}

	/** Transitional constructor for existing data generators and addons. */
	@Deprecated
	public PetalsRecipe(
			Identifier ignoredId,
			ItemStack output,
			Ingredient reagent,
			Ingredient... inputs
	) {
		this(
				ItemStackTemplate.fromNonEmptyStack(output),
				reagent,
				Arrays.asList(inputs)
		);
	}

	@Override
	public Ingredient getReagent() {
		return this.reagent;
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		List<Ingredient> ingredientsMissing = new ArrayList<>(this.inputs);

		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack stack = input.getItem(slot);
			if (stack.isEmpty()) {
				break;
			}

			int ingredientIndex = -1;
			for (int index = 0; index < ingredientsMissing.size(); index++) {
				if (ingredientsMissing.get(index).test(stack)) {
					ingredientIndex = index;
					break;
				}
			}

			if (ingredientIndex < 0) {
				return false;
			}
			ingredientsMissing.remove(ingredientIndex);
		}

		return ingredientsMissing.isEmpty();
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
		return new ItemStack(BotaniaBlocks.defaultAltar);
	}

	@Override
	public RecipeSerializer<PetalsRecipe> getSerializer() {
		return SERIALIZER;
	}
}
