/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import it.unimi.dsi.fastutil.ints.IntSet;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class RecipeUtils {
	private RecipeUtils() {}

	/**
	 * Checks whether every ingredient in {@code inputs} is satisfied by
	 * {@code input}.
	 *
	 * <p>If supplied, {@code usedSlots} receives the indexes used to satisfy
	 * the ingredients.</p>
	 */
	public static boolean matches(
			List<Ingredient> inputs,
			RecipeInput input,
			@Nullable IntSet usedSlots
	) {
		return matches(
				inputs,
				input.size(),
				input::getItem,
				usedSlots
		);
	}

	/**
	 * Transitional overload for custom recipe families that still use
	 * {@link Container}.
	 */
	public static boolean matches(
			List<Ingredient> inputs,
			Container container,
			@Nullable IntSet usedSlots
	) {
		return matches(
				inputs,
				container.getContainerSize(),
				container::getItem,
				usedSlots
		);
	}

	private static boolean matches(
			List<Ingredient> inputs,
			int size,
			IntFunction<ItemStack> getItem,
			@Nullable IntSet usedSlots
	) {
		List<Ingredient> ingredientsMissing =
				new ArrayList<>(inputs);

		for (int slot = 0; slot < size; slot++) {
			ItemStack stack = getItem.apply(slot);

			if (stack.isEmpty()) {
				break;
			}

			int matchingIngredient = -1;

			for (int ingredientIndex = 0;
					ingredientIndex < ingredientsMissing.size();
					ingredientIndex++) {
				Ingredient ingredient =
						ingredientsMissing.get(ingredientIndex);

				if (ingredient.test(stack)) {
					matchingIngredient = ingredientIndex;

					if (usedSlots != null) {
						usedSlots.add(slot);
					}

					break;
				}
			}

			if (matchingIngredient == -1) {
				return false;
			}

			ingredientsMissing.remove(matchingIngredient);
		}

		return ingredientsMissing.isEmpty();
	}

	/**
	 * Returns crafting remainders, applying {@code specialHandler} before the
	 * item's standard crafting remainder.
	 */
	public static NonNullList<ItemStack> getRemainingItemsSub(
			RecipeInput input,
			Function<ItemStack, ItemStack> specialHandler
	) {
		return getRemainingItemsSub(
				input.size(),
				input::getItem,
				specialHandler
		);
	}

	/**
	 * Transitional overload for custom recipe families that still use
	 * {@link Container}.
	 */
	public static NonNullList<ItemStack> getRemainingItemsSub(
			Container container,
			Function<ItemStack, ItemStack> specialHandler
	) {
		return getRemainingItemsSub(
				container.getContainerSize(),
				container::getItem,
				specialHandler
		);
	}

	private static NonNullList<ItemStack> getRemainingItemsSub(
			int size,
			IntFunction<ItemStack> getItem,
			Function<ItemStack, ItemStack> specialHandler
	) {
		NonNullList<ItemStack> remaining =
				NonNullList.withSize(size, ItemStack.EMPTY);

		for (int slot = 0; slot < size; slot++) {
			ItemStack stack = getItem.apply(slot);
			ItemStack special = specialHandler.apply(stack);

			if (special != null) {
				remaining.set(slot, special);
				continue;
			}

			ItemStackTemplate remainder =
					stack.getItem().getCraftingRemainder();

			if (remainder != null) {
				remaining.set(slot, remainder.create());
			}
		}

		return remaining;
	}
}