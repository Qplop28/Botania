package vazkii.botania.fabric.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.tool.elementium.ElementiumAxeItem;

@Mixin(Enchantment.class)
public class EnchantmentFabricMixin {
	@Inject(method = "canEnchant", cancellable = true, at = @At("HEAD"))
	public void onEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		Enchantment self = (Enchantment) (Object) this;
		if (ElementiumAxeItem.isLooting(self) && stack.is(BotaniaItems.elementiumAxe)) {
			cir.setReturnValue(true);
		}
	}
}
