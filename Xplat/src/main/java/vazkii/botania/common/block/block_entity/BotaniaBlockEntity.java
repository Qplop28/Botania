/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BotaniaBlockEntity extends BlockEntity {
	private static final String TAG_DATA = "BotaniaData";

	public BotaniaBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		var tag = new CompoundTag();
		writePacketNBT(tag);
		output.store(TAG_DATA, CompoundTag.CODEC, tag);
		writePersistentData(output);
	}

	@NotNull
	@Override
	public final CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		var tag = new CompoundTag();
		var data = new CompoundTag();
		writePacketNBT(data, registryLookup);
		tag.put(TAG_DATA, data);
		return tag;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		input.read(TAG_DATA, CompoundTag.CODEC).ifPresent(tag -> readPacketNBT(tag, input.lookup()));
		readPersistentData(input);
		readLegacyPersistentData(input);
	}

	/** Writes data which belongs on disk, but must not be sent in an update tag. */
	protected void writePersistentData(ValueOutput output) {}

	/** Reads data written by {@link #writePersistentData(ValueOutput)}. */
	protected void readPersistentData(ValueInput input) {}

	/** Reads data that older versions stored outside {@value #TAG_DATA}. */
	protected void readLegacyPersistentData(ValueInput input) {}

	public void writePacketNBT(CompoundTag cmp) {}

	public void readPacketNBT(CompoundTag cmp) {}

	/** Registry-aware save hook; legacy subclasses continue through the plain-tag method. */
	protected void writePacketNBT(CompoundTag cmp, HolderLookup.Provider registryLookup) {
		writePacketNBT(cmp);
	}

	/** Registry-aware load hook; legacy subclasses continue through the plain-tag method. */
	protected void readPacketNBT(CompoundTag cmp, HolderLookup.Provider registryLookup) {
		readPacketNBT(cmp);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
