/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.ForcePushHelper;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.common.item.lens.ForceLens;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ForceRelayBlock extends BotaniaBlock {

	public final Map<UUID, GlobalPos> activeBindingAttempts = new HashMap<>();

	public ForceRelayBlock(Properties builder) {
		super(builder.pushReaction(PushReaction.PUSH_ONLY));
	}

	@Override
	protected void affectNeighborsAfterRemoval(@NotNull BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, boolean isMoving) {
		BlockState newState = world.getBlockState(pos);
		var data = WorldData.get(world);

		Direction movementContextDirection = ForcePushHelper.getMovementContextDirection();
		if (isMoving && (movementContextDirection != null || newState.is(Blocks.MOVING_PISTON))) {
			var pistonDirection = movementContextDirection != null
					? movementContextDirection
					: newState.getValue(MovingPistonBlock.FACING);
			// if being moved as part of a retracting sticky piston's block structure, reverse movement direction
			var moveDirection = ForcePushHelper.isExtendingMovementContext() ? pistonDirection : pistonDirection.getOpposite();

			var destPos = data.mapping.get(pos);
			if (destPos != null) {
				BlockPos newSrcPos = pos.relative(moveDirection);

				{
					// Move source side of our binding along
					data.mapping.remove(pos);
					data.mapping.put(newSrcPos, destPos);
					data.setDirty();
				}

				if (!newState.is(Blocks.MOVING_PISTON) || newState.getValue(MovingPistonBlock.TYPE) == PistonType.DEFAULT) {
					// Move the actual bound blocks
					if (ForceLens.moveBlocks(world, destPos.relative(moveDirection.getOpposite()), moveDirection, pos)) {
						// Move dest side of our binding
						data.mapping.put(newSrcPos, data.mapping.get(newSrcPos).relative(moveDirection));
					}
				}
			}
		} else {
			if (data.mapping.remove(pos) != null) {
				data.setDirty();
			}
		}
		super.affectNeighborsAfterRemoval(state, world, pos, isMoving);
	}

	public boolean onUsedByWand(Player player, ItemStack stack, Level world, BlockPos pos) {
		if (world.isClientSide()) {
			return false;
		}

		if (player == null || player.isShiftKeyDown()) {
			world.destroyBlock(pos, true);
		} else {
			GlobalPos clicked = GlobalPos.of(world.dimension(), pos.immutable());
			if (WandOfTheForestItem.getBindMode(stack)) {
				activeBindingAttempts.put(player.getUUID(), clicked);
				world.playSound(null, pos, BotaniaSounds.ding, SoundSource.BLOCKS, 0.5F, 1F);
		} else {
				var data = WorldData.get(world);
				if (XplatAbstractions.INSTANCE.isDevEnvironment()) {
					BotaniaAPI.LOGGER.info("PistonRelay pairs");
					for (var e : data.mapping.entrySet()) {
						BotaniaAPI.LOGGER.info("{} -> {}", e.getKey(), e.getValue());
					}
			}
				BlockPos dest = data.mapping.get(pos);
				if (dest != null) {
					XplatAbstractions.INSTANCE.sendToNear(world, pos, new BotaniaEffectPacket(EffectType.PARTICLE_BEAM,
							pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							dest.getX(), dest.getY(), dest.getZ()));
			}
			}
		}

		return true;
	}

	public static class WorldData extends SavedData {

		private static final Identifier ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "piston_relay_pairs");
		private static final String LEGACY_ID = "PistonRelayPairs";
		private static final SavedDataType<WorldData> TYPE = new SavedDataType<>(
				ID, () -> new WorldData(new CompoundTag()),
				CompoundTag.CODEC.xmap(WorldData::new, WorldData::save), null);
		public final Map<BlockPos, BlockPos> mapping = new HashMap<>();

		public WorldData(@NotNull CompoundTag cmp) {
			ListTag list = cmp.getList("list").orElseGet(ListTag::new);
			for (int i = 0; i < list.size(); i += 2) {
				Tag from = list.get(i);
				Tag to = list.get(i + 1);
				BlockPos fromPos = BlockPos.CODEC.decode(NbtOps.INSTANCE, from).result().get().getFirst();
				BlockPos toPos = BlockPos.CODEC.decode(NbtOps.INSTANCE, to).result().get().getFirst();

				mapping.put(fromPos, toPos);
			}
		}

		private CompoundTag save() {
			CompoundTag cmp = new CompoundTag();
			ListTag list = new ListTag();
			for (Map.Entry<BlockPos, BlockPos> e : mapping.entrySet()) {
				Tag from = BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, e.getKey()).result().get();
				Tag to = BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, e.getValue()).result().get();
				list.add(from);
				list.add(to);
			}
			cmp.put("list", list);
			return cmp;
		}

		public static WorldData get(Level world) {
			ServerLevel level = (ServerLevel) world;
			migrateLegacyFile(level);
			return level.getDataStorage().computeIfAbsent(TYPE);
		}

		private static void migrateLegacyFile(ServerLevel level) {
			var dimensionFolder = DimensionType.getStorageFolder(level.dimension(),
					level.getServer().getWorldPath(LevelResource.ROOT));
			var dataFolder = dimensionFolder.resolve("data");
			var legacyFile = dataFolder.resolve(LEGACY_ID + ".dat");
			var currentFile = dataFolder.resolve(ID.toDebugFileName() + ".dat");
			if (Files.exists(legacyFile) && Files.notExists(currentFile)) {
				try {
					Files.move(legacyFile, currentFile);
				} catch (IOException e) {
					BotaniaAPI.LOGGER.error("Could not migrate legacy Force Relay data {} to {}", legacyFile, currentFile, e);
				}
			}
		}
	}
}
