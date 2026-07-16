/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public final class WaterBottleMatchingRecipe implements CraftingRecipe {
	private static final MapCodec<WaterBottleMatchingRecipe> CODEC =
			ShapedRecipe.MAP_CODEC.xmap(
					WaterBottleMatchingRecipe::new,
					WaterBottleMatchingRecipe::delegate
			);

	private static final StreamCodec<
			RegistryFriendlyByteBuf,
			WaterBottleMatchingRecipe
	> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public WaterBottleMatchingRecipe decode(
				RegistryFriendlyByteBuf buffer
		) {
			return new WaterBottleMatchingRecipe(
					ShapedRecipe.STREAM_CODEC.decode(buffer)
			);
		}

		@Override
		public void encode(
				RegistryFriendlyByteBuf buffer,
				WaterBottleMatchingRecipe recipe
		) {
			ShapedRecipe.STREAM_CODEC.encode(
					buffer,
					recipe.delegate
			);
		}
	};

	public static final RecipeSerializer<WaterBottleMatchingRecipe>
			SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipe delegate;

	public WaterBottleMatchingRecipe(ShapedRecipe delegate) {
		this.delegate = delegate;
	}

	private ShapedRecipe delegate() {
		return this.delegate;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (!this.delegate.matches(input, level)) {
			return false;
		}

		for (ItemStack stack : input.items()) {
			if (stack.is(Items.POTION)
					&& !stack.getOrDefault(
							DataComponents.POTION_CONTENTS,
							PotionContents.EMPTY
					).is(Potions.WATER)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return this.delegate.assemble(input);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(
			CraftingInput input
	) {
		return this.delegate.getRemainingItems(input);
	}

	@Override
	public boolean showNotification() {
		return this.delegate.showNotification();
	}

	@Override
	public String group() {
		return this.delegate.group();
	}

	@Override
	public RecipeSerializer<WaterBottleMatchingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public PlacementInfo placementInfo() {
		return this.delegate.placementInfo();
	}

	@Override
	public CraftingBookCategory category() {
		return this.delegate.category();
	}

	@Override
	public List<RecipeDisplay> display() {
		return this.delegate.display();
	}
}