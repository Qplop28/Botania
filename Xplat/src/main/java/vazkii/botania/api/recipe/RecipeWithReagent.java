package vazkii.botania.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public interface RecipeWithReagent extends Recipe<RecipeInput> {
	/**
	 * @return Ingredient matching the final item that needs to be thrown into the apothecary
	 *         to perform a craft after a matching recipe is in.
	 */
	Ingredient getReagent();

	/** Transitional result accessor for integrations that do not assemble a live recipe input. */
	@Deprecated
	ItemStack getResultItem(RegistryAccess registries);

	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.create(List.<Ingredient>of());
	}

	@Override
	default boolean showNotification() {
		return false;
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	/** Transitional ingredient accessor for existing integrations. */
	@Deprecated
	NonNullList<Ingredient> getIngredients();

	@Override
	default boolean isSpecial() {
		return true;
	}
}
