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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public final class ShapelessManaUpgradeRecipe implements CraftingRecipe {
	private static final MapCodec<ShapelessManaUpgradeRecipe> CODEC =
			ShapelessRecipe.MAP_CODEC.xmap(
					ShapelessManaUpgradeRecipe::new,
					ShapelessManaUpgradeRecipe::delegate
			);

	private static final StreamCodec<
			RegistryFriendlyByteBuf,
			ShapelessManaUpgradeRecipe
	> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ShapelessManaUpgradeRecipe decode(
				RegistryFriendlyByteBuf buffer
		) {
			return new ShapelessManaUpgradeRecipe(
					ShapelessRecipe.STREAM_CODEC.decode(buffer)
			);
		}

		@Override
		public void encode(
				RegistryFriendlyByteBuf buffer,
				ShapelessManaUpgradeRecipe recipe
		) {
			ShapelessRecipe.STREAM_CODEC.encode(
					buffer,
					recipe.delegate
			);
		}
	};

	public static final RecipeSerializer<ShapelessManaUpgradeRecipe>
			SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapelessRecipe delegate;

	public ShapelessManaUpgradeRecipe(ShapelessRecipe delegate) {
		this.delegate = delegate;
	}

	private ShapelessRecipe delegate() {
		return this.delegate;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return this.delegate.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return ManaUpgradeRecipe.output(
				this.delegate.assemble(input),
				input
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
	public RecipeSerializer<ShapelessManaUpgradeRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public PlacementInfo placementInfo() {
		return this.delegate.placementInfo();
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

	@Override
	public List<RecipeDisplay> display() {
		return this.delegate.display();
	}
}