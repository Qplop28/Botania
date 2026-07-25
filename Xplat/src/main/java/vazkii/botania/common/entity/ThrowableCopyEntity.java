/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

// [VanillaCopy] ThrowableProjectile
public abstract class ThrowableCopyEntity extends ThrowableProjectile {
	protected ThrowableCopyEntity(EntityType<? extends ThrowableCopyEntity> entityType, Level level) {
		super(entityType, level);
	}

	protected ThrowableCopyEntity(EntityType<? extends ThrowableCopyEntity> entityType, double d, double e, double f, Level level) {
		this(entityType, level);
		this.setPos(d, e, f);
	}

	protected ThrowableCopyEntity(EntityType<? extends ThrowableCopyEntity> entityType, LivingEntity livingEntity, Level level) {
		this(entityType, livingEntity.getX(), livingEntity.getEyeY() - 0.10000000149011612D, livingEntity.getZ(), level);
		this.setOwner(livingEntity);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 4.0D;
		if (Double.isNaN(e)) {
			e = 4.0D;
		}

		e *= 64.0D;
		return d < e * e;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.03D;
	}
}
