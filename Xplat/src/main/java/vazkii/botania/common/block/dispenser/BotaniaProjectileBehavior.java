/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.block.DispenserBlock;

public class BotaniaProjectileBehavior
		extends DefaultDispenseItemBehavior {
	private final ProjectileFactory factory;

	private final ProjectileItem.DispenseConfig config =
			ProjectileItem.DispenseConfig.DEFAULT;

	public BotaniaProjectileBehavior(
			ProjectileFactory factory) {
		this.factory = factory;
	}

	@Override
	protected ItemStack execute(
			BlockSource source,
			ItemStack stack) {
		ServerLevel level = source.level();

		Direction direction =
				source.state().getValue(
						DispenserBlock.FACING
				);

		Position position =
				config.positionFunction()
						.getDispensePosition(
								source,
								direction
						);

		Projectile projectile =
				factory.create(
						level,
						position,
						stack
				);

		Projectile.spawnProjectileUsingShoot(
				projectile,
				level,
				stack,
				direction.getStepX(),
				direction.getStepY(),
				direction.getStepZ(),
				config.power(),
				config.uncertainty()
		);

		stack.shrink(1);
		return stack;
	}

	@Override
	protected void playSound(BlockSource source) {
		source.level().levelEvent(
				config.overrideDispenseEvent()
						.orElse(1002),
				source.pos(),
				0
		);
	}

	@FunctionalInterface
	public interface ProjectileFactory {
		Projectile create(
				ServerLevel level,
				Position position,
				ItemStack stack
		);
	}
}