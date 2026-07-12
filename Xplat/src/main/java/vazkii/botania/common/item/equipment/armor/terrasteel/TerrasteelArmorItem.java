/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.armor.terrasteel;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;

import java.util.List;
import java.util.function.Supplier;

public class TerrasteelArmorItem extends ManasteelArmorItem {

	public TerrasteelArmorItem(Type type, Properties props) {
		super(
				type,
				withKnockbackResistance(
						BotaniaAPI.instance()
								.getTerrasteelArmorMaterial(),
						type
				),
				props
		);
	}

	private static ArmorMaterial withKnockbackResistance(
			ArmorMaterial material,
			Type type) {
		float resistance =
				material.defense()
						.getOrDefault(type.vanillaType(), 0)
						/ 20.0F;

		return new ArmorMaterial(
				material.durability(),
				material.defense(),
				material.enchantmentValue(),
				material.equipSound(),
				material.toughness(),
				resistance,
				material.repairIngredient(),
				material.assetId()
		);
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return ResourcesLib.MODEL_TERRASTEEL_NEW;
	}

	private static final Supplier<ItemStack[]> armorSet = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(BotaniaItems.terrasteelHelm),
			new ItemStack(BotaniaItems.terrasteelChest),
			new ItemStack(BotaniaItems.terrasteelLegs),
			new ItemStack(BotaniaItems.terrasteelBoots)
	});

	@Override
	public ItemStack[] getArmorSetStacks() {
		return armorSet.get();
	}

	@Override
	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null) {
			return false;
		}

		ItemStack stack = player.getItemBySlot(slot);
		if (stack.isEmpty()) {
			return false;
		}

		return switch (slot) {
			case HEAD -> stack.is(BotaniaItems.terrasteelHelm);
			case CHEST -> stack.is(BotaniaItems.terrasteelChest);
			case LEGS -> stack.is(BotaniaItems.terrasteelLegs);
			case FEET -> stack.is(BotaniaItems.terrasteelBoots);
			default -> false;
		};

	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("botania.armorset.terrasteel.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list) {
		list.add(Component.translatable("botania.armorset.terrasteel.desc0").withStyle(ChatFormatting.GRAY));
		list.add(Component.translatable("botania.armorset.terrasteel.desc1").withStyle(ChatFormatting.GRAY));
		list.add(Component.translatable("botania.armorset.terrasteel.desc2").withStyle(ChatFormatting.GRAY));
	}

	@SoftImplement("IForgeItem")
	public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
		return true;
	}
}
