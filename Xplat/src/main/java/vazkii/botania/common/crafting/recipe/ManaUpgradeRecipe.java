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
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;

public final class ManaUpgradeRecipe implements CraftingRecipe {
	private static final MapCodec<ManaUpgradeRecipe> CODEC =
			ShapedRecipe.MAP_CODEC.xmap(
					ManaUpgradeRecipe::new,
					ManaUpgradeRecipe::delegate
			);

	private static final StreamCodec<RegistryFriendlyByteBuf, ManaUpgradeRecipe>
			STREAM_CODEC = new StreamCodec<>() {
				@Override
				public ManaUpgradeRecipe decode(
						RegistryFriendlyByteBuf buffer
				) {
					return new ManaUpgradeRecipe(
							ShapedRecipe.STREAM_CODEC.decode(buffer)
					);
				}

				@Override
				public void encode(
						RegistryFriendlyByteBuf buffer,
						ManaUpgradeRecipe recipe
				) {
					ShapedRecipe.STREAM_CODEC.encode(
							buffer,
							recipe.delegate
					);
				}
			};

	public static final RecipeSerializer<ManaUpgradeRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipe delegate;

	public ManaUpgradeRecipe(ShapedRecipe delegate) {
		this.delegate = delegate;
	}

	private ShapedRecipe delegate() {
		return this.delegate;
	}

	public static ItemStack output(
			ItemStack output,
			CraftingInput input
	) {
		ItemStack result = output.copy();
		var resultManaItem =
				XplatAbstractions.INSTANCE.findManaItem(result);

		if (resultManaItem == null) {
			return result;
		}

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			var manaItem =
					XplatAbstractions.INSTANCE.findManaItem(stack);

			if (manaItem != null) {
				resultManaItem.addMana(manaItem.getMana());
			}
		}

		return result;
	}

	/**
	 * Transitional overload for remaining non-crafting Botania callers.
	 */
	public static ItemStack output(
			ItemStack output,
			Container input
	) {
		ItemStack result = output.copy();
		var resultManaItem =
				XplatAbstractions.INSTANCE.findManaItem(result);

		if (resultManaItem == null) {
			return result;
		}

		for (int i = 0; i < input.getContainerSize(); i++) {
			ItemStack stack = input.getItem(i);

			if (stack.isEmpty()) {
				continue;
			}

			var manaItem =
					XplatAbstractions.INSTANCE.findManaItem(stack);

			if (manaItem != null) {
				resultManaItem.addMana(manaItem.getMana());
			}
		}

		return result;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return this.delegate.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return output(this.delegate.assemble(input), input);
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
	public RecipeSerializer<ManaUpgradeRecipe> getSerializer() {
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