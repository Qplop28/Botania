/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TerrestrialAgglomerationPlateBlockEntity
		extends BotaniaBlockEntity
		implements SparkAttachable, ManaReceiver {
	public static final Supplier<IMultiblock> MULTIBLOCK =
			Suppliers.memoize(() -> PatchouliAPI.get().makeMultiblock(
					new String[][] {
							{
									"___",
									"_P_",
									"___"
							},
							{
									"RLR",
									"L0L",
									"RLR"
							}
					},
					'P',
					BotaniaBlocks.terraPlate,
					'R',
					PatchouliAPI.get().tagMatcher(
							BotaniaTags.Blocks.TERRA_PLATE_BASE
					),
					'0',
					PatchouliAPI.get().tagMatcher(
							BotaniaTags.Blocks.TERRA_PLATE_BASE
					),
					'L',
					PatchouliAPI.get().tagMatcher(
							XplatAbstractions.INSTANCE.isFabric()
									? TagKey.create(
											Registries.BLOCK,
											new Identifier(
													"c",
													"lapis_blocks"
											)
									)
									: TagKey.create(
											Registries.BLOCK,
											new Identifier(
													"forge",
													"storage_blocks/lapis"
											)
									)
					)
			));

	private static final String TAG_MANA = "mana";

	private int mana;

	public TerrestrialAgglomerationPlateBlockEntity(
			BlockPos pos,
			BlockState state
	) {
		super(BotaniaBlockEntities.TERRA_PLATE, pos, state);
	}

	public static void serverTick(
			Level level,
			BlockPos worldPosition,
			BlockState state,
			TerrestrialAgglomerationPlateBlockEntity self
	) {
		boolean removeMana = true;

		if (self.hasValidPlatform()) {
			List<ItemEntity> itemEntities =
					self.getItemEntities();
			List<ItemStack> items =
					self.getItems(itemEntities);
			RecipeInput input = self.createInput(items);

			RecipeHolder<TerrestrialAgglomerationRecipe>
					recipeHolder =
					self.getCurrentRecipe(input);

			if (recipeHolder != null) {
				TerrestrialAgglomerationRecipe recipe =
						recipeHolder.value();
				removeMana = false;

				ManaSpark spark = self.getAttachedSpark();

				if (spark != null) {
					var otherSparks =
							SparkHelper.getSparksAround(
									level,
									worldPosition.getX() + 0.5,
									worldPosition.getY() + 0.5,
									worldPosition.getZ() + 0.5,
									spark.getNetwork()
							);

					for (var otherSpark : otherSparks) {
						if (spark != otherSpark
								&& otherSpark
										.getAttachedManaReceiver()
										instanceof ManaPool) {
							otherSpark.registerTransfer(spark);
						}
					}
				}

				if (self.mana > 0) {
					VanillaPacketDispatcher
							.dispatchTEToNearbyPlayers(self);

					int proportion =
							Float.floatToIntBits(
									self.getCompletion()
							);

					XplatAbstractions.INSTANCE.sendToNear(
							level,
							worldPosition,
							new BotaniaEffectPacket(
									EffectType.TERRA_PLATE,
									worldPosition.getX(),
									worldPosition.getY(),
									worldPosition.getZ(),
									proportion
							)
					);
				}

				if (self.mana >= recipe.getMana()) {
					Player player =
							getCraftingPlayer(itemEntities);
					ItemStack result =
							recipe.assemble(input);

					if (player != null) {
						player.triggerRecipeCrafted(
								recipeHolder,
								List.of(result)
						);
						result.getItem()
								.onCraftedBy(result, player);
					}

					for (ItemStack item : items) {
						item.setCount(0);
					}

					ItemEntity item = new ItemEntity(
							level,
							worldPosition.getX() + 0.5,
							worldPosition.getY() + 0.2,
							worldPosition.getZ() + 0.5,
							result
					);

					item.setDeltaMovement(Vec3.ZERO);
					level.addFreshEntity(item);
					level.playSound(
							null,
							item.getX(),
							item.getY(),
							item.getZ(),
							BotaniaSounds.terrasteelCraft,
							SoundSource.BLOCKS,
							1F,
							1F
					);

					self.mana = 0;
					level.updateNeighbourForOutputSignal(
							worldPosition,
							state.getBlock()
					);
					VanillaPacketDispatcher
							.dispatchTEToNearbyPlayers(self);
				}
			}
		}

		if (removeMana) {
			self.receiveMana(-1000);
		}
	}

	@Nullable
	private static Player getCraftingPlayer(
			List<ItemEntity> itemEntities
	) {
		Player player = null;
		int minAge = Integer.MAX_VALUE;

		for (ItemEntity entity : itemEntities) {
			if (entity.getOwner() instanceof Player owner
					&& entity.getAge() < minAge) {
				player = owner;
				minAge = entity.getAge();
			}
		}

		return player;
	}

	private List<ItemStack> getItems(
			List<ItemEntity> itemEntities
	) {
		List<ItemStack> stacks = new ArrayList<>();

		for (ItemEntity entity : itemEntities) {
			if (!entity.getItem().isEmpty()) {
				stacks.add(entity.getItem());
			}
		}

		return stacks;
	}

	private List<ItemEntity> getItemEntities() {
		return level.getEntitiesOfClass(
				ItemEntity.class,
				new AABB(
						worldPosition,
						worldPosition.offset(1, 1, 1)
				),
				EntitySelector.ENTITY_STILL_ALIVE
		);
	}

	private RecipeInput createInput(List<ItemStack> items) {
		return new TerraPlateInput(
				List.of(flattenStacks(items))
		);
	}

	/**
	 * Flattens the list of stacks into stacks with a count of one for recipe
	 * matching. If the total item count exceeds 64, an empty input is
	 * returned.
	 */
	private static ItemStack[] flattenStacks(
			List<ItemStack> items
	) {
		int totalCount = 0;

		for (ItemStack item : items) {
			totalCount += item.getCount();
		}

		if (totalCount > 64) {
			return new ItemStack[0];
		}

		ItemStack[] stacks = new ItemStack[totalCount];
		int index = 0;

		for (ItemStack item : items) {
			if (item.getCount() > 1) {
				ItemStack single = item.copyWithCount(1);

				for (int count = 0;
						count < item.getCount();
						count++) {
					stacks[index++] = single.copy();
				}
			} else {
				stacks[index++] = item;
			}
		}

		return stacks;
	}

	@Nullable
	private RecipeHolder<TerrestrialAgglomerationRecipe>
			getCurrentRecipe(RecipeInput input) {
		if (input.isEmpty()
				|| !(level instanceof ServerLevel serverLevel)) {
			return null;
		}

		RecipeManager recipeManager =
				serverLevel.getServer().getRecipeManager();

		return recipeManager.getRecipeFor(
				BotaniaRecipeTypes.TERRA_PLATE_TYPE,
				input,
				serverLevel
		).orElse(null);
	}

	private RecipeHolder<TerrestrialAgglomerationRecipe>
			getCurrentRecipe() {
		return getCurrentRecipe(
				createInput(getItems(getItemEntities()))
		);
	}

	private boolean isActive() {
		return getCurrentRecipe() != null;
	}

	private boolean hasValidPlatform() {
		return MULTIBLOCK.get()
				.validate(level, getBlockPos().below()) != null;
	}

	@Override
	public void writePacketNBT(CompoundTag tag) {
		tag.putInt(TAG_MANA, mana);
	}

	@Override
	public void readPacketNBT(CompoundTag tag) {
		mana = tag.getIntOr(TAG_MANA, 0);
	}

	@Override
	public Level getManaReceiverLevel() {
		return getLevel();
	}

	@Override
	public BlockPos getManaReceiverPos() {
		return getBlockPos();
	}

	@Override
	public int getCurrentMana() {
		return mana;
	}

	@Override
	public boolean isFull() {
		RecipeHolder<TerrestrialAgglomerationRecipe>
				recipeHolder = getCurrentRecipe();

		return recipeHolder == null
				|| getCurrentMana()
						>= recipeHolder.value().getMana();
	}

	@Override
	public void receiveMana(int mana) {
		this.mana = Math.max(0, this.mana + mana);
		level.updateNeighbourForOutputSignal(
				worldPosition,
				getBlockState().getBlock()
		);
	}

	@Override
	public boolean canReceiveManaFromBursts() {
		return isActive();
	}

	@Override
	public boolean canAttachSpark(ItemStack stack) {
		return true;
	}

	@Override
	public ManaSpark getAttachedSpark() {
		List<Entity> sparks = level.getEntitiesOfClass(
				Entity.class,
				new AABB(
						worldPosition.above(),
						worldPosition.above().offset(1, 1, 1)
				),
				Predicates.instanceOf(ManaSpark.class)
		);

		if (sparks.size() == 1) {
			return (ManaSpark) sparks.getFirst();
		}

		return null;
	}

	@Override
	public boolean areIncomingTranfersDone() {
		return !isActive();
	}

	@Override
	public int getAvailableSpaceForMana() {
		RecipeHolder<TerrestrialAgglomerationRecipe>
				recipeHolder = getCurrentRecipe();

		return recipeHolder == null
				? 0
				: Math.max(
						0,
						recipeHolder.value().getMana()
								- getCurrentMana()
				);
	}

	public float getCompletion() {
		RecipeHolder<TerrestrialAgglomerationRecipe>
				recipeHolder = getCurrentRecipe();

		if (recipeHolder == null) {
			return 0;
		}

		return (float) getCurrentMana()
				/ recipeHolder.value().getMana();
	}

	public int getComparatorLevel() {
		int value = (int) (getCompletion() * 15.0);

		if (getCurrentMana() > 0) {
			value = Math.max(value, 1);
		}

		return value;
	}

	private record TerraPlateInput(
			List<ItemStack> items
	) implements RecipeInput {
		private TerraPlateInput {
			items = List.copyOf(items);
		}

		@Override
		public ItemStack getItem(int index) {
			return items.get(index);
		}

		@Override
		public int size() {
			return items.size();
		}
	}
}