/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.handler;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public abstract class EquipmentHandler {
	public static EquipmentHandler instance;

	public static void init() {
		if (instance == null) {
			instance = XplatAbstractions.INSTANCE.tryCreateEquipmentHandler();
		}

		// Fall back to hotbar
		if (instance == null) {
			instance = new InventoryEquipmentHandler();
		}
	}

	public static Container getAllWorn(LivingEntity living) {
		return instance.getAllWornItems(living);
	}

	public static ItemStack findOrEmpty(Item item, LivingEntity living) {
		return instance.findItem(item, living);
	}

	public static ItemStack findOrEmpty(Predicate<ItemStack> pred, LivingEntity living) {
		return instance.findItem(pred, living);
	}

	protected abstract Container getAllWornItems(LivingEntity living);

	protected abstract ItemStack findItem(Item item, LivingEntity living);

	protected abstract ItemStack findItem(Predicate<ItemStack> pred, LivingEntity living);

	public abstract void onInit(Item item);

	public boolean isAccessory(ItemStack stack) {
		return stack.is(BotaniaTags.Items.RODS) || stack.getItem() instanceof BaubleItem || XplatAbstractions.INSTANCE.findManaItem(stack) != null;
	}

	// Fallback equipment handler for curios-less (or baubles-less) installs.
	public static class InventoryEquipmentHandler extends EquipmentHandler {
		private final Map<Player, ItemStack[]> map = new WeakHashMap<>();

		public void onPlayerTick(Player player) {
			var profiler = Profiler.get();
			profiler.push("botania:tick_wearables");

			ItemStack[] oldStacks = map.computeIfAbsent(player, p -> {
				ItemStack[] array = new ItemStack[9];
				Arrays.fill(array, ItemStack.EMPTY);
				return array;
			});

			Inventory inv = player.getInventory();
			for (int i = 0; i < 9; i++) {
				ItemStack old = oldStacks[i];
				ItemStack current = inv.getItem(i);

				if (!ItemStack.matches(old, current)) {
					if (old.getItem() instanceof BaubleItem bauble) {
						player.getAttributes().removeAttributeModifiers(bauble.getEquippedAttributeModifiers(old));
						bauble.onUnequipped(old, player);
					}
					if (canEquip(current, player)) {
						BaubleItem bauble = (BaubleItem) current.getItem();
						player.getAttributes().addTransientAttributeModifiers(bauble.getEquippedAttributeModifiers(current));
						bauble.onEquipped(current, player);
					}
					oldStacks[i] = current.copy(); // shift-clicking mutates the stack we stored,
					// making it empty and failing the equality check - let's avoid that
				}

				if (canEquip(current, player)) {
					((BaubleItem) current.getItem()).onWornTick(current, player);
				}
			}
			profiler.pop();
		}

		@Override
		protected Container getAllWornItems(LivingEntity living) {
			if (!(living instanceof Player player)) {
				return new SimpleContainer(9);
			}
			SimpleContainer worn = new SimpleContainer(9);
			int next = 0;
			for (int i = 0; i < 9; i++) {
				ItemStack stack = player.getInventory().getItem(i);
				if (!stack.isEmpty() && canEquip(stack, living)) {
					worn.setItem(next++, stack.copy());
				}
			}
			return worn;
		}

		@Override
		protected ItemStack findItem(Item item, LivingEntity living) {
			if (living instanceof Player player) {
				Inventory inv = player.getInventory();
				for (int i = 0; i < 9; i++) {
					ItemStack stack = inv.getItem(i);
					if (stack.is(item) && canEquip(stack, living)) {
						return stack;
					}
				}
			}
			return ItemStack.EMPTY;
		}

		@Override
		protected ItemStack findItem(Predicate<ItemStack> pred, LivingEntity living) {
			if (living instanceof Player player) {
				Inventory inv = player.getInventory();
				for (int i = 0; i < 9; i++) {
					ItemStack stack = inv.getItem(i);
					if (pred.test(stack) && canEquip(stack, living)) {
						return stack;
					}
				}
			}
			return ItemStack.EMPTY;
		}

		@Override
		public void onInit(Item item) {}

		private static boolean canEquip(ItemStack stack, LivingEntity living) {
			return stack.getItem() instanceof BaubleItem bauble && bauble.canEquip(stack, living);
		}
	}
}
