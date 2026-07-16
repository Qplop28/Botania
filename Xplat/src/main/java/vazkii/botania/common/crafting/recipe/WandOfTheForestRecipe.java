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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import vazkii.botania.common.block.decor.BotaniaMushroomBlock;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.common.item.material.MysticalPetalItem;

import java.util.List;

public final class WandOfTheForestRecipe implements CraftingRecipe {
	private static final MapCodec<WandOfTheForestRecipe> CODEC =
			ShapedRecipe.MAP_CODEC.xmap(
					WandOfTheForestRecipe::new,
					WandOfTheForestRecipe::delegate
			);

	private static final StreamCodec<
			RegistryFriendlyByteBuf,
			WandOfTheForestRecipe
	> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public WandOfTheForestRecipe decode(
				RegistryFriendlyByteBuf buffer
		) {
			return new WandOfTheForestRecipe(
					ShapedRecipe.STREAM_CODEC.decode(buffer)
			);
		}

		@Override
		public void encode(
				RegistryFriendlyByteBuf buffer,
				WandOfTheForestRecipe recipe
		) {
			ShapedRecipe.STREAM_CODEC.encode(
					buffer,
					recipe.delegate
			);
		}
	};

	public static final RecipeSerializer<WandOfTheForestRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipe delegate;

	public WandOfTheForestRecipe(ShapedRecipe delegate) {
		this.delegate = delegate;
	}

	private ShapedRecipe delegate() {
		return this.delegate;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return this.delegate.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack result = this.delegate.assemble(input);
		int firstColor = -1;

		for (ItemStack stack : input.items()) {
			Item item = stack.getItem();
			int color;

			if (item instanceof MysticalPetalItem petal) {
				color = petal.color.getId();
			} else if (item instanceof BlockItem blockItem
					&& blockItem.getBlock()
							instanceof BotaniaMushroomBlock mushroom) {
				color = mushroom.color.getId();
			} else {
				continue;
			}

			if (firstColor == -1) {
				firstColor = color;
			} else {
				return WandOfTheForestItem.setColors(
						result,
						firstColor,
						color
				);
			}
		}

		return WandOfTheForestItem.setColors(
				result,
				firstColor != -1 ? firstColor : 0,
				0
		);
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
	public RecipeSerializer<WandOfTheForestRecipe> getSerializer() {
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