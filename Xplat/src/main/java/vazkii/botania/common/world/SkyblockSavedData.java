/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.world;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.util.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import vazkii.botania.xplat.BotaniaConfig;

import java.util.Map;
import java.util.UUID;

public class SkyblockSavedData extends SavedData {
	private static final String NAME = "gog_skyblock_islands";
	private static final SavedDataType<SkyblockSavedData> TYPE = new SavedDataType<>(
			NAME, () -> new SkyblockSavedData(new CompoundTag()),
			CompoundTag.CODEC.xmap(SkyblockSavedData::new, SkyblockSavedData::save), null);

	/** The offset is chosen to put islands under default settings in the center of a chunk region. */
	private static final int OFFSET = 1;

	public final BiMap<IslandPos, UUID> skyblocks;
	private final Spiral spiral;

	public SkyblockSavedData(CompoundTag nbt) {
		HashBiMap<IslandPos, UUID> map = HashBiMap.create();
		for (Tag inbt : nbt.getList("Islands").orElseGet(ListTag::new)) {
			CompoundTag tag = (CompoundTag) inbt;
			tag.read("Player", UUIDUtil.CODEC).ifPresent(player -> map.put(IslandPos.fromTag(tag), player));
		}
		this.skyblocks = map;
		var spiralState = nbt.getIntArray("SpiralState");
		if (spiralState.isPresent() && spiralState.get().length >= 4) {
			this.spiral = Spiral.fromArray(spiralState.get());
		} else {
			this.spiral = new Spiral();
		}
	}

	public static SkyblockSavedData get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(TYPE);
	}

	public IslandPos getSpawn() {
		if (skyblocks.containsValue(Util.NIL_UUID)) {
			return skyblocks.inverse().get(Util.NIL_UUID);
		}
		IslandPos pos = new IslandPos(OFFSET, OFFSET);
		skyblocks.put(pos, Util.NIL_UUID);
		setDirty();
		return pos;
	}

	public IslandPos create(UUID playerId) {
		int scale = BotaniaConfig.common().gogIslandScaleMultiplier();
		IslandPos islandPos;
		do {
			int[] pos = spiral.next();
			islandPos = new IslandPos(pos[0] * scale + OFFSET, pos[1] * scale + OFFSET);
		} while (skyblocks.containsKey(islandPos));

		skyblocks.put(islandPos, playerId);
		setDirty();
		return islandPos;
	}

	private CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		ListTag list = new ListTag();
		for (Map.Entry<IslandPos, UUID> entry : skyblocks.entrySet()) {
			CompoundTag entryTag = entry.getKey().toTag();
			entryTag.store("Player", UUIDUtil.CODEC, entry.getValue());
			list.add(entryTag);
		}
		nbt.putIntArray("SpiralState", spiral.toIntArray());
		nbt.put("Islands", list);
		return nbt;
	}

	// Adapted from https://stackoverflow.com/questions/398299/looping-in-a-spiral
	private static class Spiral {
		private int x = 0;
		private int y = 0;
		private int dx = 0;
		private int dy = -1;

		Spiral() {}

		Spiral(int x, int y, int dx, int dy) {
			this.x = x;
			this.y = y;
			this.dx = dx;
			this.dy = dy;
		}

		int[] next() {
			if (x == y || x < 0 && x == -y || x > 0 && x == 1 - y) {
				int t = dx;
				dx = -dy;
				dy = t;
			}
			x += dx;
			y += dy;
			return new int[] { x, y };
		}

		int[] toIntArray() {
			return new int[] { x, y, dx, dy };
		}

		static Spiral fromArray(int[] ints) {
			return new Spiral(ints[0], ints[1], ints[2], ints[3]);
		}
	}
}
