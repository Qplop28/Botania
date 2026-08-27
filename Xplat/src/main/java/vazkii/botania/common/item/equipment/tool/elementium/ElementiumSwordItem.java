/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool.elementium;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.equipment.CustomDamageItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.function.Consumer;

public class ElementiumSwordItem extends Item implements CustomDamageItem {
	private static final int MANA_PER_DAMAGE = 60;

	public ElementiumSwordItem(Properties props) {
		super(withAttributes(BotaniaAPI.instance().getElementiumItemTier(), props));
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		return ToolCommons.damageItemIfPossible(stack, amount, entity, MANA_PER_DAMAGE);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
		if (entity instanceof Player player
				&& stack.getDamageValue() > 0
				&& ManaItemHandler.instance().requestManaExactForTool(
						stack, player, MANA_PER_DAMAGE * 2, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	private static Properties withAttributes(ToolMaterial material, Properties props) {
		ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
						BASE_ATTACK_DAMAGE_ID, 3 + material.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(
						BASE_ATTACK_SPEED_ID, -2.4F, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(PixieHandler.pixieSpawnChance(),
						PixieHandler.makeModifier(EquipmentSlot.MAINHAND, "Sword modifier", 0.05),
						EquipmentSlotGroup.MAINHAND)
				.build();
		return props.sword(material, 3, -2.4F).attributes(attributes);
	}
}
