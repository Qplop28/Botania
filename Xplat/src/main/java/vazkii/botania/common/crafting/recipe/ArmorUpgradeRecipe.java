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

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

import java.util.List;

public final class ArmorUpgradeRecipe implements CraftingRecipe {
	private static final MapCodec<ArmorUpgradeRecipe> CODEC =
			ShapedRecipe.MAP_CODEC.xmap(
					ArmorUpgradeRecipe::new,
					ArmorUpgradeRecipe::delegate
			);

	private static final StreamCodec<
			RegistryFriendlyByteBuf,
			ArmorUpgradeRecipe
	> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ArmorUpgradeRecipe decode(
				RegistryFriendlyByteBuf buffer
		) {
			return new ArmorUpgradeRecipe(
					ShapedRecipe.STREAM_CODEC.decode(buffer)
			);
		}

		@Override
		public void encode(
				RegistryFriendlyByteBuf buffer,
				ArmorUpgradeRecipe recipe
		) {
			ShapedRecipe.STREAM_CODEC.encode(
					buffer,
					recipe.delegate
			);
		}
	};

	public static final RecipeSerializer<ArmorUpgradeRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipe delegate;

	public ArmorUpgradeRecipe(ShapedRecipe delegate) {
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

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			Equippable equippable =
					stack.get(DataComponents.EQUIPPABLE);

			if (equippable != null
					&& equippable.slot().getType()
							== EquipmentSlot.Type.HUMANOID_ARMOR) {
				result.applyComponentsAndValidate(
						stack.getComponentsPatch()
				);
				break;
			}
		}

		return result;
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
	public RecipeSerializer<ArmorUpgradeRecipe> getSerializer() {
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