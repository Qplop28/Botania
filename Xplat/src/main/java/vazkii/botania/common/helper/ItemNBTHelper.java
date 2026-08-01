/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.helper;

import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.DataFixers;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.xplat.XplatAbstractions;

public final class ItemNBTHelper {

	private static final int[] EMPTY_INT_ARRAY = new int[0];
	private static final long[] EMPTY_LONG_ARRAY = new long[0];
	/** Minecraft 1.20.1, the version that wrote Botania's pre-component stack payloads. */
	private static final int LEGACY_ITEM_STACK_DATA_VERSION = 3465;

	// SETTERS ///////////////////////////////////////////////////////////////////

	public static void set(ItemStack stack, String tag, Tag nbt) {
		update(stack, data -> data.put(tag, nbt));
	}

	public static void setBoolean(ItemStack stack, String tag, boolean b) {
		update(stack, data -> data.putBoolean(tag, b));
	}

	public static void setByte(ItemStack stack, String tag, byte b) {
		update(stack, data -> data.putByte(tag, b));
	}

	public static void setShort(ItemStack stack, String tag, short s) {
		update(stack, data -> data.putShort(tag, s));
	}

	public static void setInt(ItemStack stack, String tag, int i) {
		update(stack, data -> data.putInt(tag, i));
	}

	public static void setIntArray(ItemStack stack, String tag, int[] val) {
		update(stack, data -> data.putIntArray(tag, val));
	}

	public static void setLong(ItemStack stack, String tag, long l) {
		update(stack, data -> data.putLong(tag, l));
	}

	public static void setLongArray(ItemStack stack, String tag, long[] val) {
		update(stack, data -> data.putLongArray(tag, val));
	}

	public static void setFloat(ItemStack stack, String tag, float f) {
		update(stack, data -> data.putFloat(tag, f));
	}

	public static void setDouble(ItemStack stack, String tag, double d) {
		update(stack, data -> data.putDouble(tag, d));
	}

	public static void setCompound(ItemStack stack, String tag, CompoundTag cmp) {
		if (!tag.equalsIgnoreCase("ench")) // not override the enchantments
		{
			update(stack, data -> data.put(tag, cmp));
		}
	}

	public static void setString(ItemStack stack, String tag, String s) {
		update(stack, data -> data.putString(tag, s));
	}

	public static void setList(ItemStack stack, String tag, ListTag list) {
		update(stack, data -> data.put(tag, list));
	}

	public static void removeEntry(ItemStack stack, String tag) {
		CompoundTag data = copyCustomData(stack);
		data.remove(tag);
		if (data.isEmpty()) {
			stack.remove(DataComponents.CUSTOM_DATA);
		} else {
			CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
		}
	}

	// GETTERS ///////////////////////////////////////////////////////////////////

	public static boolean verifyExistance(ItemStack stack, String tag) {
		return !stack.isEmpty() && copyCustomData(stack).contains(tag);
	}

	public static boolean verifyType(ItemStack stack, String tag, Class<? extends Tag> tagClass) {
		return !stack.isEmpty() && tagClass.isInstance(copyCustomData(stack).get(tag));
	}

	@Nullable
	public static Tag get(ItemStack stack, String tag) {
		return copyCustomData(stack).get(tag);
	}

	public static boolean getBoolean(ItemStack stack, String tag, boolean defaultExpected) {
		return copyCustomData(stack).getBoolean(tag).orElse(defaultExpected);
	}

	public static byte getByte(ItemStack stack, String tag, byte defaultExpected) {
		return copyCustomData(stack).getByte(tag).orElse(defaultExpected);
	}

	public static short getShort(ItemStack stack, String tag, short defaultExpected) {
		return copyCustomData(stack).getShort(tag).orElse(defaultExpected);
	}

	public static int getInt(ItemStack stack, String tag, int defaultExpected) {
		return copyCustomData(stack).getInt(tag).orElse(defaultExpected);
	}

	public static int[] getIntArray(ItemStack stack, String tag) {
		return copyCustomData(stack).getIntArray(tag).orElse(EMPTY_INT_ARRAY);
	}

	public static long getLong(ItemStack stack, String tag, long defaultExpected) {
		return copyCustomData(stack).getLong(tag).orElse(defaultExpected);
	}

	public static long[] getLongArray(ItemStack stack, String tag) {
		return copyCustomData(stack).getLongArray(tag).orElse(EMPTY_LONG_ARRAY);
	}

	public static float getFloat(ItemStack stack, String tag, float defaultExpected) {
		return copyCustomData(stack).getFloat(tag).orElse(defaultExpected);
	}

	public static double getDouble(ItemStack stack, String tag, double defaultExpected) {
		return copyCustomData(stack).getDouble(tag).orElse(defaultExpected);
	}

	/**
	 * If nullifyOnFail is true it'll return null if it doesn't find any
	 * compounds, otherwise it'll return a new one.
	 **/
	@Nullable
	@Contract("_, _, false -> !null")
	public static CompoundTag getCompound(ItemStack stack, String tag, boolean nullifyOnFail) {
		return copyCustomData(stack).getCompound(tag).orElseGet(() -> nullifyOnFail ? null : new CompoundTag());
	}

	@Nullable
	@Contract("_, _, !null -> !null")
	public static String getString(ItemStack stack, String tag, String defaultExpected) {
		return copyCustomData(stack).getString(tag).orElse(defaultExpected);
	}

	@Nullable
	@Contract("_, _, _, false -> !null")
	public static ListTag getList(ItemStack stack, String tag, int objtype, boolean nullifyOnFail) {
		return copyCustomData(stack).getList(tag).filter(list -> list.isEmpty() || list.getElementType() == objtype)
				.orElseGet(() -> nullifyOnFail ? null : new ListTag());
	}

	private static CompoundTag copyCustomData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	private static void update(ItemStack stack, java.util.function.Consumer<CompoundTag> writer) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, writer);
	}

	/**
	 * Decodes either a current component-format stack or a Botania 1.20.1 stack.
	 * Legacy payloads are passed through Mojang's ITEM_STACK data fixes so nested
	 * NBT is translated into its proper data components rather than renamed.
	 */
	public static DataResult<ItemStack> decodeStoredStack(CompoundTag stored) {
		if (stored.contains("components") || stored.contains("count")) {
			return ItemStack.CODEC.parse(NbtOps.INSTANCE, stored);
		}
		if (!stored.contains("Count") && !stored.contains("tag")) {
			return ItemStack.CODEC.parse(NbtOps.INSTANCE, stored);
		}

		Dynamic<Tag> legacy = new Dynamic<>(NbtOps.INSTANCE, stored.copy());
		Dynamic<Tag> fixed = DataFixers.getDataFixer().update(References.ITEM_STACK, legacy,
				LEGACY_ITEM_STACK_DATA_VERSION, SharedConstants.getCurrentVersion().dataVersion().version());
		return fixed.getValue() instanceof CompoundTag corrected
				? ItemStack.CODEC.parse(NbtOps.INSTANCE, corrected)
				: DataResult.error(() -> "ITEM_STACK data fixer did not return a compound: " + fixed.getValue());
	}

	// OTHER ///////////////////////////////////////////////////////////////////

	/**
	 * Returns the fullness of the mana item:
	 * 0 if empty, 1 if partially full, 2 if full.
	 */
	public static int getFullness(ManaItem item) {
		int mana = item.getMana();
		if (mana == 0) {
			return 0;
		} else if (mana == item.getMaxMana()) {
			return 2;
		} else {
			return 1;
		}
	}

	public static ItemStack duplicateAndClearMana(ItemStack stack) {
		ItemStack copy = stack.copy();
		ManaItem manaItem = XplatAbstractions.INSTANCE.findManaItem(copy);
		if (manaItem != null) {
			manaItem.addMana(-manaItem.getMana());
		}
		return copy;
	}

	/**
	 * Checks if two items are the same and have the same NBT. If they are `IManaItems`, their mana property is matched
	 * on whether they are empty, partially full, or full.
	 */
	public static boolean matchTagAndManaFullness(ItemStack stack1, ItemStack stack2) {
		if (!ItemStack.isSameItem(stack1, stack2)) {
			return false;
		}
		ManaItem manaItem1 = XplatAbstractions.INSTANCE.findManaItem(stack1);
		ManaItem manaItem2 = XplatAbstractions.INSTANCE.findManaItem(stack2);
		if (manaItem1 != null && manaItem2 != null) {
			if (getFullness(manaItem1) != getFullness(manaItem2)) {
				return false;
			} else {
				return ItemStack.matches(duplicateAndClearMana(stack1), duplicateAndClearMana(stack2));
			}
		}
		return ItemStack.isSameItemSameComponents(stack1, stack2);
	}

	/**
	 * Serializes a stack in the current item-stack JSON codec format.
	 */
	public static JsonObject serializeStack(ItemStack stack) {
		return ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).getOrThrow().getAsJsonObject();
	}

	public static void renameTag(CompoundTag nbt, String oldName, String newName) {
		Tag tag = nbt.get(oldName);
		if (tag != null) {
			nbt.remove(oldName);
			nbt.put(newName, tag);
		}
	}
}
