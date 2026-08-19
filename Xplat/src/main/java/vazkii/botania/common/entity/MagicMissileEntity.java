/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.helper.VecHelper;

import java.util.List;
import java.util.function.Predicate;

public class MagicMissileEntity
		extends ThrowableProjectile {
	private static final String TAG_TIME = "time";

	private static final EntityDataAccessor<Boolean> EVIL =
			SynchedEntityData.defineId(
					MagicMissileEntity.class,
					EntityDataSerializers.BOOLEAN
			);

	private static final EntityDataAccessor<Integer> TARGET =
			SynchedEntityData.defineId(
					MagicMissileEntity.class,
					EntityDataSerializers.INT
			);

	double lockX;
	double lockY = Integer.MIN_VALUE;
	double lockZ;

	int time = 0;

	public MagicMissileEntity(
			EntityType<MagicMissileEntity> type,
			Level world) {
		super(type, world);
	}

	public MagicMissileEntity(
			LivingEntity owner,
			boolean evil) {
		super(
				BotaniaEntities.MAGIC_MISSILE,
				owner.getX(),
				owner.getEyeY() - 0.1,
				owner.getZ(),
				owner.level()
		);

		setOwner(owner);
		setEvil(evil);
	}

	@Override
	protected void defineSynchedData(
			SynchedEntityData.Builder entityData) {
		entityData.define(EVIL, false);
		entityData.define(TARGET, 0);
	}

	public void setEvil(boolean evil) {
		entityData.set(EVIL, evil);
	}

	public boolean isEvil() {
		return entityData.get(EVIL);
	}

	public void setTarget(LivingEntity entity) {
		entityData.set(
				TARGET,
				entity == null ? -1 : entity.getId()
		);
	}

	public LivingEntity getTargetEntity() {
		int id = entityData.get(TARGET);
		Entity entity = level().getEntity(id);

		if (entity instanceof LivingEntity living) {
			return living;
		}

		return null;
	}

	@Override
	public void tick() {
		double lastTickPosX = xOld;
		double lastTickPosY = yOld;
		double lastTickPosZ = zOld;

		super.tick();

		if (!level().isClientSide()
				&& (!findTarget() || time > 40)) {
			discard();
			return;
		}

		boolean evil = isEvil();

		Vec3 currentPosition =
				VecHelper.fromEntityCenter(this);

		Vec3 oldPosition =
				new Vec3(
						lastTickPosX,
						lastTickPosY,
						lastTickPosZ
				);

		Vec3 difference =
				currentPosition.subtract(oldPosition);

		Vec3 step =
				difference.normalize().scale(0.05);

		int steps =
				(int) (difference.length()
						/ step.length());

		Vec3 particlePosition = oldPosition;

		SparkleParticleData data =
				evil
						? SparkleParticleData.corrupt(
								0.8F,
								1.0F,
								0.0F,
								1.0F,
								2
						)
						: SparkleParticleData.sparkle(
								0.8F,
								1.0F,
								0.4F,
								1.0F,
								2
						);

		for (int i = 0; i < steps; i++) {
			level().addParticle(
					data,
					particlePosition.x,
					particlePosition.y,
					particlePosition.z,
					0.0,
					0.0,
					0.0
			);

			if (getRandom().nextInt(steps) <= 1) {
				level().addParticle(
						data,
						particlePosition.x
								+ (Math.random() - 0.5)
								* 0.4,
						particlePosition.y
								+ (Math.random() - 0.5)
								* 0.4,
						particlePosition.z
								+ (Math.random() - 0.5)
								* 0.4,
						0.0,
						0.0,
						0.0
				);
			}

			particlePosition =
					particlePosition.add(step);
		}

		LivingEntity target = getTargetEntity();

		if (target != null) {
			if (lockY == Integer.MIN_VALUE) {
				lockX = target.getX();
				lockY = target.getY();
				lockZ = target.getZ();
			}

			Vec3 targetPosition =
					evil
							? new Vec3(
									lockX,
									lockY,
									lockZ
							)
							: VecHelper.fromEntityCenter(
									target
							);

			Vec3 targetDifference =
					targetPosition.subtract(
							currentPosition
					);

			Vec3 motion =
					targetDifference
							.normalize()
							.scale(
									evil
											? 0.5
											: 0.6
							);

			setDeltaMovement(motion);

			if (time < 10) {
				setDeltaMovement(
						getDeltaMovement().x(),
						Math.abs(
								getDeltaMovement().y()
						),
						getDeltaMovement().z()
				);
			}

			List<LivingEntity> nearbyEntities =
					level().getEntitiesOfClass(
							LivingEntity.class,
							new AABB(
									getX() - 0.5,
									getY() - 0.5,
									getZ() - 0.5,
									getX() + 0.5,
									getY() + 0.5,
									getZ() + 0.5
							)
					);

			if (nearbyEntities.contains(target)) {
				target.hurtOrSimulate(
						getDamageSource(),
						evil ? 12.0F : 7.0F
				);

				discard();
			}

			if (evil
					&& targetDifference.length() < 1.0) {
				discard();
			}
		}

		time++;
	}

	private DamageSource getDamageSource() {
		Entity owner = getOwner();

		if (owner instanceof LivingEntity livingOwner) {
			return owner instanceof Player playerOwner
					? damageSources().playerAttack(
							playerOwner
					)
					: damageSources().mobAttack(
							livingOwner
					);
		}

		return damageSources().generic();
	}

	@Override
	protected void addAdditionalSaveData(
			ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt(TAG_TIME, time);
	}

	@Override
	protected void readAdditionalSaveData(
			ValueInput input) {
		super.readAdditionalSaveData(input);
		time = input.getIntOr(TAG_TIME, 0);
	}

	public boolean findTarget() {
		LivingEntity target = getTargetEntity();

		if (target != null) {
			if (target.isAlive()) {
				return true;
			}

			target = null;
			setTarget(null);
		}

		if (!(level() instanceof ServerLevel serverLevel)) {
			return false;
		}

		double range = 12.0;

		AABB bounds =
				new AABB(
						getX() - range,
						getY() - range,
						getZ() - range,
						getX() + range,
						getY() + range,
						getZ() + range
				);

		DamageSource source = getDamageSource();

		Predicate<Entity> vulnerableTo =
				entity ->
						!entity.isInvulnerableTo(
								serverLevel,
								source
						);

		List<? extends LivingEntity> entities;

		if (isEvil()) {
			entities =
					level().getEntitiesOfClass(
							Player.class,
							bounds,
							EntitySelector
									.LIVING_ENTITY_STILL_ALIVE
									.and(vulnerableTo)
					);
		} else {
			Entity owner = getOwner();

			Predicate<Entity> predicate =
					EntitySelector
							.LIVING_ENTITY_STILL_ALIVE
							.and(
									targetPredicate(
											owner
									)
							)
							.and(vulnerableTo);

			entities =
					level().getEntitiesOfClass(
							LivingEntity.class,
							bounds,
							predicate
					);
		}

		if (!entities.isEmpty()) {
			target =
					entities.get(
							level().random.nextInt(
									entities.size()
							)
					);

			setTarget(target);
		}

		return target != null;
	}

	public static Predicate<Entity> targetPredicate(
			Entity owner) {
		return target ->
				target instanceof LivingEntity living
						&& shouldTarget(
								owner,
								living
						);
	}

	public static boolean shouldTarget(
			Entity owner,
			LivingEntity entity) {
		// Always defend yourself.
		if (entity instanceof Mob mob
				&& isHostile(
						owner,
						mob.getTarget()
				)) {
			return true;
		}

		// Do not target tamed creatures.
		if (entity instanceof TamableAnimal animal
				&& animal.isTame()
				|| entity instanceof AbstractHorse horse
				&& horse.isTamed()) {
			return false;
		}

		// Other hostile mobs are valid targets.
		return entity instanceof Enemy;
	}

	public static boolean isHostile(
			Entity owner,
			Entity attackTarget) {
		/*
		 * If the owner can attack the target through
		 * PvP, only defend against the owner's own
		 * attacker.
		 */
		if (owner instanceof Player ownerPlayer
				&& attackTarget
						instanceof Player targetedPlayer
				&& ownerPlayer.canHarmPlayer(
						targetedPlayer
				)) {
			return owner == attackTarget;
		}

		return attackTarget instanceof Player;
	}

	@Override
	protected void onHitBlock(
			@NotNull BlockHitResult hit) {
		super.onHitBlock(hit);

		BlockState state =
				level().getBlockState(
						hit.getBlockPos()
				);

		if (!level().isClientSide()
				&& !(state.getBlock()
						instanceof BushBlock)
				&& !state.is(BlockTags.LEAVES)) {
			discard();
		}
	}

	@Override
	protected void onHitEntity(
			@NotNull EntityHitResult hit) {
		super.onHitEntity(hit);

		if (!level().isClientSide()
				&& hit.getEntity()
						== getTargetEntity()) {
			discard();
		}
	}
}
