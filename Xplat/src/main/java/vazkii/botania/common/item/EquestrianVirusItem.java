/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.mixin.AbstractHorseAccessor;

public class EquestrianVirusItem extends Item {
	private static final Identifier VIRUS_MODIFIER_ID =
			Identifier.fromNamespaceAndPath(
					"botania",
					"equestrian_virus"
			);

	public EquestrianVirusItem(Properties builder) {
		super(builder);
	}

	@Override
	public InteractionResult interactLivingEntity(
			ItemStack stack,
			Player player,
			LivingEntity living,
			InteractionHand hand) {
		if (!(living instanceof Horse horse)
				|| !horse.isAlive()) {
			return InteractionResult.PASS;
		}

		if (player.level().isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (!horse.isTamed()) {
			return InteractionResult.PASS;
		}

		if (!(player.level()
				instanceof ServerLevel serverLevel)) {
			return InteractionResult.PASS;
		}

		AbstractHorse newHorse =
				stack.is(BotaniaItems.necroVirus)
						? EntityType.ZOMBIE_HORSE.create(
								serverLevel,
								EntitySpawnReason.CONVERSION
						)
						: EntityType.SKELETON_HORSE.create(
								serverLevel,
								EntitySpawnReason.CONVERSION
						);

		if (newHorse == null) {
			return InteractionResult.FAIL;
		}

		SimpleContainer inventory =
				((AbstractHorseAccessor) horse)
						.getInventory();

		ItemStack saddle = inventory.getItem(0);

		/*
		 * Not every AbstractHorse implementation uses
		 * inventory slot zero for a saddle.
		 */
		if (!saddle.isEmpty()
				&& !saddle.is(Items.SADDLE)) {
			horse.spawnAtLocation(
					serverLevel,
					saddle,
					0.0F
			);

			saddle = ItemStack.EMPTY;
		}

		for (int i = 1;
				i < inventory.getContainerSize();
				i++) {
			ItemStack inventoryStack =
					inventory.getItem(i);

			if (!inventoryStack.isEmpty()) {
				horse.spawnAtLocation(
						serverLevel,
						inventoryStack,
						0.0F
				);
			}
		}

		newHorse.snapTo(
				horse.getX(),
				horse.getY(),
				horse.getZ(),
				horse.getYRot(),
				horse.getXRot()
		);

		newHorse.finalizeSpawn(
				serverLevel,
				serverLevel.getCurrentDifficultyAt(
						newHorse.blockPosition()
				),
				EntitySpawnReason.CONVERSION,
				null
		);

		newHorse.tameWithName(player);
		newHorse.setAge(horse.getAge());

		if (!saddle.isEmpty()) {
			SimpleContainer newInventory =
					((AbstractHorseAccessor) newHorse)
							.getInventory();

			newInventory.setItem(0, saddle);
		}

		AttributeInstance movementSpeed =
				newHorse.getAttribute(
						Attributes.MOVEMENT_SPEED
				);

		AttributeInstance oldMovementSpeed =
				horse.getAttribute(
						Attributes.MOVEMENT_SPEED
				);

		if (movementSpeed != null
				&& oldMovementSpeed != null) {
			movementSpeed.setBaseValue(
					oldMovementSpeed.getBaseValue()
			);

			movementSpeed.addPermanentModifier(
					new AttributeModifier(
							VIRUS_MODIFIER_ID,
							movementSpeed.getBaseValue(),
							AttributeModifier.Operation.ADD_VALUE
					)
			);
		}

		AttributeInstance health =
				newHorse.getAttribute(
						Attributes.MAX_HEALTH
				);

		AttributeInstance oldHealth =
				horse.getAttribute(
						Attributes.MAX_HEALTH
				);

		if (health != null && oldHealth != null) {
			health.setBaseValue(
					oldHealth.getBaseValue()
			);

			health.addPermanentModifier(
					new AttributeModifier(
							VIRUS_MODIFIER_ID,
							health.getBaseValue(),
							AttributeModifier.Operation.ADD_VALUE
					)
			);
		}

		AttributeInstance jumpStrength =
				newHorse.getAttribute(
						Attributes.JUMP_STRENGTH
				);

		AttributeInstance oldJumpStrength =
				horse.getAttribute(
						Attributes.JUMP_STRENGTH
				);

		if (jumpStrength != null
				&& oldJumpStrength != null) {
			jumpStrength.setBaseValue(
					oldJumpStrength.getBaseValue()
			);

			jumpStrength.addPermanentModifier(
					new AttributeModifier(
							VIRUS_MODIFIER_ID,
							jumpStrength.getBaseValue()
									* 0.5,
							AttributeModifier.Operation.ADD_VALUE
					)
			);
		}

		newHorse.playSound(
				BotaniaSounds.virusInfect,
				1.0F
						+ living.level()
								.random
								.nextFloat(),
				living.level()
						.random
						.nextFloat()
						* 0.7F
						+ 1.3F
		);

		horse.discard();

		serverLevel.addFreshEntity(newHorse);
		newHorse.spawnAnim();

		stack.shrink(1);

		return InteractionResult.SUCCESS_SERVER;
	}

	public static boolean onLivingHurt(
			LivingEntity entity,
			DamageSource source) {
		if (entity.isPassenger()
				&& entity.getVehicle()
						instanceof LivingEntity vehicle) {
			entity = vehicle;
		}

		return entity instanceof AbstractHorse horse
				&& (horse instanceof ZombieHorse
						|| horse instanceof SkeletonHorse)
				&& source.is(DamageTypes.FALL)
				&& horse.isTamed();
	}
}