/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.List;

public class FallingStarEntity
		extends ThrowableCopyEntity {
	private static final String TAG_HAS_BEEN_IN_AIR =
			"hasBeenInAir";

	/*
	 * Prevent the star from being discarded on block
	 * collisions before its first exposure to an air block.
	 */
	private boolean hasBeenInAir;

	public FallingStarEntity(
			EntityType<FallingStarEntity> type,
			Level level) {
		super(type, level);
	}

	public FallingStarEntity(
			LivingEntity owner,
			Level level) {
		super(
				BotaniaEntities.FALLING_STAR,
				owner,
				level
		);
	}

	@Override
	protected void defineSynchedData(
			SynchedEntityData.Builder entityData) {
	}

	@Override
	public void tick() {
		super.tick();

		if (!hasBeenInAir
				&& !level().isClientSide()) {
			BlockState feetState =
					getFeetBlockState();

			hasBeenInAir =
					feetState.isAir()
							|| isInWater()
							|| isInLava();
		}

		float distance = 1.5F;

		SparkleParticleData data =
				SparkleParticleData.sparkle(
						2.0F,
						1.0F,
						0.4F,
						1.0F,
						6
				);

		for (int i = 0; i < 10; i++) {
			float xOffset =
					(float) (Math.random() - 0.5)
							* distance;

			float yOffset =
					(float) (Math.random() - 0.5)
							* distance;

			float zOffset =
					(float) (Math.random() - 0.5)
							* distance;

			level().addAlwaysVisibleParticle(
					data,
					getX() + xOffset,
					getY() + yOffset,
					getZ() + zOffset,
					0.0,
					0.0,
					0.0
			);
		}

		Entity owner = getOwner();

		if (!level().isClientSide()
				&& owner != null) {
			AABB bounds =
					new AABB(
							getX(),
							getY(),
							getZ(),
							xOld,
							yOld,
							zOld
					).inflate(2.0);

			List<LivingEntity> entities =
					level().getEntitiesOfClass(
							LivingEntity.class,
							bounds
					);

			for (LivingEntity living : entities) {
				if (living == owner) {
					continue;
				}

				if (living.hurtTime == 0) {
					onHit(
							new EntityHitResult(
									living
							)
					);

					return;
				}
			}
		}

		if (tickCount > 200) {
			discard();
		}
	}

	@Override
	protected void onHitEntity(
			@NotNull EntityHitResult hit) {
		super.onHitEntity(hit);

		Entity target = hit.getEntity();

		// Trading with villagers counts as a swing and
		// must not summon a damaging star.
		if (target instanceof Villager) {
			return;
		}

		if (!level().isClientSide()) {
			Entity owner = getOwner();

			if (target != owner
					&& target.isAlive()) {
				DamageSource source;

				if (owner instanceof Player player) {
					source =
							player.damageSources()
									.playerAttack(
											player
									);
				} else {
					source = damageSources().generic();
				}

				float damage =
						Math.random() < 0.25
								? 10.0F
								: 5.0F;

				target.hurtOrSimulate(
						source,
						damage
				);
			}

			discard();
		}
	}

	@Override
	protected void onHitBlock(
			BlockHitResult hit) {
		super.onHitBlock(hit);

		if (!level().isClientSide()) {
			BlockPos position =
					hit.getBlockPos();

			BlockState state =
					level().getBlockState(position);

			if (hasBeenInAir) {
				if (BotaniaConfig.common()
						.blockBreakParticles()
						&& !state.isAir()) {
					level().levelEvent(
							2001,
							position,
							Block.getId(state)
					);
				}

				discard();
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(
			ValueOutput output) {
		super.addAdditionalSaveData(output);

		output.putBoolean(
				TAG_HAS_BEEN_IN_AIR,
				hasBeenInAir
		);
	}

	@Override
	protected void readAdditionalSaveData(
			ValueInput input) {
		super.readAdditionalSaveData(input);

		hasBeenInAir =
				input.getBooleanOr(
						TAG_HAS_BEEN_IN_AIR,
						false
				);
	}
}