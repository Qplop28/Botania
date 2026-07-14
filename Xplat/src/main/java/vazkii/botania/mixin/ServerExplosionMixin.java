package vazkii.botania.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.ServerExplosion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.common.item.equipment.bauble.TectonicGirdleItem;

@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {
	@WrapOperation(
			method = "hurtEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;getAttributeValue(Lnet/minecraft/core/Holder;)D"
			)
	)
	private double botania$negateExplosionKnockback(
			LivingEntity living,
			Holder<Attribute> attribute,
			Operation<Double> original
	) {
		if (TectonicGirdleItem.negateExplosionKnockback(living)) {
			return 1.0;
		}

		return original.call(living, attribute);
	}
}