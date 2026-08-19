/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.handler;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.entity.PixieEntity;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.armor.elementium.ElementiumHelmItem;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public final class PixieHandler {

	private PixieHandler() {}

	private static final Attribute PIXIE_SPAWN_CHANCE_ATTRIBUTE = new RangedAttribute("attribute.name.botania.pixieSpawnChance", 0, 0, 1);
	public static final Holder<Attribute> PIXIE_SPAWN_CHANCE = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(PIXIE_SPAWN_CHANCE_ATTRIBUTE);


	private static final List<Supplier<MobEffectInstance>> effectSuppliers = List.of(
			() -> new MobEffectInstance(MobEffects.BLINDNESS, 40, 0),
			() -> new MobEffectInstance(MobEffects.WITHER, 50, 0),
			() -> new MobEffectInstance(MobEffects.SLOWNESS, 40, 0),
			() -> new MobEffectInstance(MobEffects.WEAKNESS, 40, 0)
	);

	public static void registerAttribute(BiConsumer<Attribute, Identifier> r) {
		r.accept(PIXIE_SPAWN_CHANCE_ATTRIBUTE, prefix("pixie_spawn_chance"));
	}

	public static AttributeModifier makeModifier(EquipmentSlot slot, String name, double amount) {
		return new AttributeModifier(prefix("pixie_spawn_chance/" + slot.getName()), amount, AttributeModifier.Operation.ADD_VALUE);
	}

	public static void onDamageTaken(Player player, DamageSource source) {
		if (player.level() instanceof ServerLevel serverLevel && source.getEntity() instanceof LivingEntity livingSource) {
			// Sometimes the player doesn't have the attribute, not sure why.
			// Could be badly-written mixins on Fabric.
			double chance = player.getAttributes().hasAttribute(PIXIE_SPAWN_CHANCE)
					? player.getAttributeValue(PIXIE_SPAWN_CHANCE) : 0;
			ItemStack sword = PlayerHelper.getFirstHeldItem(player, s -> s.is(BotaniaItems.elementiumSword));

			if (Math.random() < chance) {
				PixieEntity pixie = new PixieEntity(serverLevel);
				pixie.setPos(player.getX(), player.getY() + 2, player.getZ());

				if (((ElementiumHelmItem) BotaniaItems.elementiumHelm).hasArmorSet(player)) {
					pixie.setApplyPotionEffect(effectSuppliers.get(serverLevel.getRandom().nextInt(effectSuppliers.size())).get());
				}

				float dmg = 4;
				if (!sword.isEmpty()) {
					dmg += 2;
				}

				pixie.setProps(livingSource, player, 0, dmg);
				pixie.finalizeSpawn(
					serverLevel,
					serverLevel.getCurrentDifficultyAt(pixie.blockPosition()),
					EntitySpawnReason.EVENT,
					null
				);
				serverLevel.addFreshEntity(pixie);
			}
		}
	}
}
