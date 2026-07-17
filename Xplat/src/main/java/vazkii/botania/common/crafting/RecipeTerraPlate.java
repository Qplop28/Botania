/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.crafting.recipe.RecipeUtils;

import java.util.List;

public class RecipeTerraPlate
		implements TerrestrialAgglomerationRecipe {
	private static final MapCodec<RecipeTerraPlate> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					Codec.INT.fieldOf("mana")
							.forGetter(RecipeTerraPlate::getMana),
					Ingredient.CODEC.listOf()
							.fieldOf("ingredients")
							.forGetter(recipe -> recipe.inputs),
					RecipeCodecs.ITEM_STACK_TEMPLATE
							.fieldOf("result")
							.forGetter(recipe -> recipe.output)
			).apply(instance, RecipeTerraPlate::new));

	private static final StreamCodec<
			RegistryFriendlyByteBuf,
			RecipeTerraPlate
	> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<RecipeTerraPlate> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final int mana;
	private final NonNullList<Ingredient> inputs;
	private final ItemStackTemplate output;
	private final PlacementInfo placementInfo;

	public RecipeTerraPlate(
			int mana,
			List<Ingredient> inputs,
			ItemStackTemplate output
	) {
		this.mana = mana;
		this.inputs = NonNullList.create();
		this.inputs.addAll(inputs);
		this.output = output;
		this.placementInfo = PlacementInfo.create(this.inputs);
	}

	/**
	 * Transitional constructor for existing data generators and addons.
	 *
	 * <p>Recipe identifiers are now stored by {@code RecipeHolder}, so the
	 * supplied identifier is intentionally not retained.</p>
	 */
	@Deprecated
	public RecipeTerraPlate(
			Identifier ignoredId,
			int mana,
			NonNullList<Ingredient> inputs,
			ItemStack output
	) {
		this(
				mana,
				inputs,
				ItemStackTemplate.fromNonEmptyStack(output)
		);
	}

	@Override
	public int getMana() {
		return this.mana;
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		int nonEmptySlots = 0;

		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack stack = input.getItem(slot);

			if (!stack.isEmpty()) {
				if (stack.getCount() > 1) {
					return false;
				}

				nonEmptySlots++;
			}
		}

		IntOpenHashSet usedSlots =
				new IntOpenHashSet(input.size());

		return RecipeUtils.matches(
				this.inputs,
				input,
				usedSlots
		) && usedSlots.size() == nonEmptySlots;
	}

	@Override
	public ItemStack assemble(RecipeInput input) {
		return this.output.create();
	}

	@Override
	public PlacementInfo placementInfo() {
		return this.placementInfo;
	}

	@Override
	public RecipeSerializer<RecipeTerraPlate> getSerializer() {
		return SERIALIZER;
	}

	/**
	 * Transitional accessor for existing Botania integrations.
	 */
	@Deprecated
	public NonNullList<Ingredient> getIngredients() {
		return this.inputs;
	}

	/**
	 * Transitional overload for callers still passing a {@link Container}.
	 */
	@Deprecated
	public boolean matches(Container container, Level level) {
		return matches(asRecipeInput(container), level);
	}

	/**
	 * Transitional overload for callers using the old assembly signature.
	 */
	@Deprecated
	public ItemStack assemble(
			Container container,
			RegistryAccess registries
	) {
		return assemble(asRecipeInput(container));
	}

	/**
	 * Transitional result accessor for older integrations.
	 */
	@Deprecated
	public ItemStack getResultItem(RegistryAccess registries) {
		return this.output.create();
	}

	private static RecipeInput asRecipeInput(Container container) {
		return new RecipeInput() {
			@Override
			public ItemStack getItem(int index) {
				return container.getItem(index);
			}

			@Override
			public int size() {
				return container.getContainerSize();
			}
		};
	}
}
