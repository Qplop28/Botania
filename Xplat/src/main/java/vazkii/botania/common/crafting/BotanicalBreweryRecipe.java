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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.BrewContainer;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BotanicalBreweryRecipe
		implements vazkii.botania.api.recipe.BotanicalBreweryRecipe {
	private static final Codec<Brew> BREW_CODEC =
			Identifier.CODEC.comapFlatMap(
					BotanicalBreweryRecipe::decodeBrew,
					brew -> BotaniaAPI.instance()
							.getBrewRegistry().getKey(brew)
			);

	private static final MapCodec<BotanicalBreweryRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					BREW_CODEC.fieldOf("brew")
							.forGetter(recipe -> recipe.brew),
					Ingredient.CODEC.listOf().fieldOf("ingredients")
							.forGetter(recipe -> recipe.inputs)
			).apply(instance, BotanicalBreweryRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, BotanicalBreweryRecipe>
			STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<BotanicalBreweryRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final Brew brew;
	private final NonNullList<Ingredient> inputs;
	private final PlacementInfo placementInfo;

	public BotanicalBreweryRecipe(Brew brew, List<Ingredient> inputs) {
		this.brew = brew;
		this.inputs = NonNullList.create();
		this.inputs.addAll(inputs);
		this.placementInfo = PlacementInfo.create(this.inputs);
	}

	/** Transitional constructor for existing data generators and addons. */
	@Deprecated
	public BotanicalBreweryRecipe(
			Identifier ignoredId,
			Brew brew,
			Ingredient... inputs
	) {
		this(brew, Arrays.asList(inputs));
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		List<Ingredient> inputsMissing = new ArrayList<>(this.inputs);

		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack stack = input.getItem(slot);
			if (stack.isEmpty()) {
				break;
			}

			if (stack.getItem() instanceof BrewContainer) {
				continue;
			}

			boolean matchedOne = false;
			Iterator<Ingredient> iterator = inputsMissing.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().test(stack)) {
					iterator.remove();
					matchedOne = true;
					break;
				}
			}

			if (!matchedOne) {
				return false;
			}
		}

		return inputsMissing.isEmpty();
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
		return new ItemStack(BotaniaBlocks.brewery);
	}

	@Override
	public RecipeSerializer<BotanicalBreweryRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public Brew getBrew() {
		return this.brew;
	}

	@Override
	public int getManaUsage() {
		return this.brew.getManaCost();
	}

	@Override
	public ItemStack getOutput(ItemStack stack) {
		if (stack.isEmpty()
				|| !(stack.getItem() instanceof BrewContainer container)) {
			return new ItemStack(Items.GLASS_BOTTLE);
		}

		return container.getItemForBrew(this.brew, stack);
	}

	@Override
	public int hashCode() {
		return 31 * this.brew.hashCode() ^ this.inputs.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof BotanicalBreweryRecipe recipe
				&& this.brew == recipe.brew
				&& this.inputs.equals(recipe.inputs);
	}

	private static DataResult<Brew> decodeBrew(Identifier id) {
		return BotaniaAPI.instance().getBrewRegistry().getOptional(id)
				.map(DataResult::success)
				.orElseGet(() -> DataResult.error(
						() -> "Unknown brew " + id));
	}
}
