/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelArmorItem;

@Mixin(PiglinAi.class)
public class PiglinAiFabricMixin {
	@Inject(at = @At("HEAD"), method = "isWearingSafeArmor(Lnet/minecraft/world/entity/LivingEntity;)Z", cancellable = true)
	private static void terrasteelNeutral(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
			ItemStack stack = entity.getItemBySlot(slot);

			if (stack.getItem() instanceof TerrasteelArmorItem armor
					&& armor.makesPiglinsNeutral(stack, entity)) {
				cir.setReturnValue(true);
				break;
			}
		}
	}
}
