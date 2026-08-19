/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.api.state.enums.CraftyCratePattern;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.mixin.RecipeManagerAccessor;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class CraftyCrateBlockEntity extends OpenCrateBlockEntity implements Wandable {
	private static int recipeEpoch = 0;

	private int signal = 0;
	private ItemStack craftResult = ItemStack.EMPTY;

	private final Queue<Identifier> lastRecipes = new ArrayDeque<>();
	private boolean dirty;
	private boolean matchFailed;
	private int lastRecipeEpoch = recipeEpoch;

	public static void registerListener() {
		XplatAbstractions.INSTANCE.registerReloadListener(
				PackType.SERVER_DATA,
				prefix("craft_crate_epoch_counter"),
				(ResourceManagerReloadListener) manager -> recipeEpoch++
		);
	}

	public CraftyCrateBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.CRAFT_CRATE, pos, state);
	}

	@Override
	protected SimpleContainer createItemHandler() {
		return new SimpleContainer(9) {
			@Override
			public int getMaxStackSize() {
				return 1;
			}

			@Override
			public boolean canPlaceItem(int slot, ItemStack stack) {
				return !isLocked(slot);
			}
		};
	}

	public CraftyCratePattern getPattern() {
		BlockState state = getBlockState();

		if (!state.is(BotaniaBlocks.craftCrate)) {
			return CraftyCratePattern.NONE;
		}

		return state.getValue(BotaniaStateProperties.CRATE_PATTERN);
	}

	private boolean isLocked(int slot) {
		return !getPattern().openSlots.get(slot);
	}

	public static void serverTick(
			Level level,
			BlockPos worldPosition,
			BlockState state,
			CraftyCrateBlockEntity self
	) {
		if (recipeEpoch != self.lastRecipeEpoch) {
			self.lastRecipeEpoch = recipeEpoch;
			self.matchFailed = false;
		}

		if (!self.matchFailed
				&& self.canEject()
				&& self.isFull()
				&& self.craft(true, null)) {
			self.ejectAll();
		}

		int newSignal = 0;

		for (; newSignal < 9; newSignal++) {
			if (!self.isLocked(newSignal)
					&& self.getItemHandler()
							.getItem(newSignal)
							.isEmpty()) {
				break;
			}
		}

		if (newSignal != self.signal) {
			self.signal = newSignal;
			level.updateNeighbourForOutputSignal(
					worldPosition,
					state.getBlock()
			);
		}

		if (self.dirty) {
			self.dirty = false;
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
		}
	}

	private boolean craft(
			boolean fullCheck,
			@Nullable Player player
	) {
		var profiler = Profiler.get();
		profiler.push("craft");

		if (fullCheck && !isFull()) {
			profiler.pop();
			return false;
		}

		CraftingContainer craft = new TransientCraftingContainer(
				new AbstractContainerMenu(MenuType.CRAFTING, -1) {
					@NotNull
					@Override
					public ItemStack quickMoveStack(
							@NotNull Player player,
							int slot
					) {
						return ItemStack.EMPTY;
					}

					@Override
					public boolean stillValid(
							@NotNull Player player
					) {
						return false;
					}
				},
				3,
				3
		);

		for (int i = 0; i < craft.getContainerSize(); i++) {
			ItemStack stack = getItemHandler().getItem(i);

			if (stack.isEmpty()
					|| isLocked(i)
					|| stack.is(BotaniaItems.placeholder)) {
				continue;
			}

			craft.setItem(i, stack);
		}

		CraftingInput input = craft.asCraftInput();
		Optional<RecipeHolder<CraftingRecipe>> matchingRecipe =
				getMatchingRecipe(input);

		matchingRecipe.ifPresent(holder -> {
			CraftingRecipe recipe = holder.value();
			ItemStack result = recipe.assemble(input);

			if (result.isEmpty()) {
				matchFailed = true;
				return;
			}

			if (player != null) {
				player.triggerRecipeCrafted(
						holder,
						List.of(result)
				);
				result.getItem().onCraftedBy(result, player);
			}

			Container handler = getItemHandler();
			List<ItemStack> remainders =
					recipe.getRemainingItems(input);

			for (int i = 0; i < craft.getContainerSize(); i++) {
				ItemStack remainder = remainders.get(i);
				ItemStack inSlot = handler.getItem(i);

				if ((inSlot.isEmpty() && remainder.isEmpty())
						|| (!inSlot.isEmpty()
								&& inSlot.is(
										BotaniaItems.placeholder
								))) {
					continue;
				}

				handler.setItem(i, remainder);
			}

			craftResult = result;
		});

		if (matchingRecipe.isEmpty()) {
			matchFailed = true;
		}

		profiler.pop();

		return matchingRecipe.isPresent()
				&& !craftResult.isEmpty();
	}

	private Optional<RecipeHolder<CraftingRecipe>>
			getMatchingRecipe(CraftingInput input) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return Optional.empty();
		}

		RecipeManager recipeManager =
				serverLevel.getServer().getRecipeManager();

		var recipes = ((RecipeManagerAccessor) recipeManager)
				.botania_getRecipeMap()
				.byType(RecipeType.CRAFTING);

		for (Identifier currentRecipe : lastRecipes) {
			for (RecipeHolder<CraftingRecipe> holder : recipes) {
				if (holder.id().identifier().equals(currentRecipe)
						&& holder.value().matches(input, level)) {
					return Optional.of(holder);
				}
			}
		}

		Optional<RecipeHolder<CraftingRecipe>> recipe =
				recipeManager.getRecipeFor(
						RecipeType.CRAFTING,
						input,
						serverLevel
				);

		if (recipe.isPresent()) {
			if (lastRecipes.size() >= 8) {
				lastRecipes.remove();
			}

			lastRecipes.add(
					recipe.get().id().identifier()
			);
		}

		return recipe;
	}

	boolean isFull() {
		for (int i = 0;
				i < getItemHandler().getContainerSize();
				i++) {
			if (!isLocked(i)
					&& getItemHandler().getItem(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	private void ejectAll() {
		for (int i = 0; i < inventorySize(); ++i) {
			ItemStack stack = getItemHandler().getItem(i);

			if (!stack.isEmpty()) {
				eject(stack, false);
				getItemHandler().setItem(i, ItemStack.EMPTY);
			}
		}

		if (!craftResult.isEmpty()) {
			eject(craftResult, false);
			craftResult = ItemStack.EMPTY;
		}
	}

	public void ejectLocked() {
		for (int i = 0; i < inventorySize(); ++i) {
			if (!isLocked(i)) {
				continue;
			}

			ItemStack stack = getItemHandler().getItem(i);

			if (!stack.isEmpty()) {
				eject(stack, false);
				getItemHandler().setItem(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public boolean onUsedByWand(
			@Nullable Player player,
			ItemStack stack,
			Direction side
	) {
		if (!getLevel().isClientSide() && canEject()) {
			craft(false, player);
			ejectAll();
		}

		return true;
	}

	@Override
	public void setChanged() {
		super.setChanged();

		if (level != null && !level.isClientSide()) {
			this.dirty = true;
			this.matchFailed = false;
		}
	}

	public int getSignal() {
		return signal;
	}

	public static class WandHud implements WandHUD {
		private final CraftyCrateBlockEntity crate;

		public WandHud(CraftyCrateBlockEntity crate) {
			this.crate = crate;
		}

		@Override
		public void renderHUD(
				GuiGraphicsExtractor gui,
				Minecraft minecraft
		) {
			int width = 52;
			int height = 52;
			int xCenter =
					minecraft.getWindow().getGuiScaledWidth() / 2 + 12;
			int yCenter =
					minecraft.getWindow().getGuiScaledHeight() / 2
							- height / 2;

			RenderHelper.renderHUDBox(
					gui,
					xCenter - 4,
					yCenter - 4,
					xCenter + width + 4,
					yCenter + height + 4
			);

			for (int row = 0; row < 3; row++) {
				for (int column = 0; column < 3; column++) {
					int index = row * 3 + column;
					int x = xCenter + column * 18;
					int y = yCenter + row * 18;

					boolean enabled = true;

					if (crate.getPattern()
							!= CraftyCratePattern.NONE) {
						enabled =
								crate.getPattern()
										.openSlots
										.get(index);
					}

					gui.fill(
							x,
							y,
							x + 16,
							y + 16,
							enabled
									? 0x22FFFFFF
									: 0x22FF0000
					);

					ItemStack item =
							crate.getItemHandler().getItem(index);
					gui.item(item, x, y);
				}
			}
		}
	}
}