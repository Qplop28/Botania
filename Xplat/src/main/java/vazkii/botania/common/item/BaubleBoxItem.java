/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.gui.box.BaubleBoxContainer;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.InventoryHelper;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.stream.IntStream;

public class BaubleBoxItem extends Item {
	public static final int SIZE = 24;
	public static final String TAG_OPEN = "open";

	public BaubleBoxItem(Properties props) {
		super(props);
	}

	public static SimpleContainer getInventory(ItemStack stack) {
		return new ItemBackedInventory(stack, SIZE) {
			@Override
			public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
				return EquipmentHandler.instance.isAccessory(stack);
			}
		};
	}

	@Override
		public static InteractionResult onPlayerInteract(
			Player player,
			Level world,
			InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (stack.isEmpty()
				|| !stack.is(Items.GLASS_BOTTLE)) {
			return InteractionResult.PASS;
		}

		boolean canCollectAmbientAir =
				world.dimension() == Level.END
						&& isClearFromDragonBreath(
								world,
								player.getBoundingBox()
										.inflate(3.5)
						)
						&& notAimingAtFluid(world, player);

		boolean canCollectEntityAir =
				pickupFromEntity(
						world,
						player.getBoundingBox()
								.inflate(1.0)
				);

		if (canCollectAmbientAir || canCollectEntityAir) {
			if (!world.isClientSide()) {
				ItemStack enderAir =
						new ItemStack(
								BotaniaItems.enderAirBottle
						);

				player.getInventory()
						.placeItemBackInInventory(enderAir);

				stack.shrink(1);

				world.playSound(
						null,
						player.blockPosition(),
						SoundEvents.ITEM_PICKUP,
						SoundSource.NEUTRAL,
						0.5F,
						1.0F
				);

				world.gameEvent(
						player,
						GameEvent.FLUID_PICKUP,
						player.position()
				);
			}

			return world.isClientSide()
					? InteractionResult.SUCCESS
					: InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.PASS;
	}

	@NotNull
	@Override
	public InteractionResult use(
			Level world,
			Player player,
			@NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}

		world.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				BotaniaSounds.enderAirThrow,
				SoundSource.PLAYERS,
				1.0F,
				0.4F
						/ (player.getRandom().nextFloat()
								* 0.4F
								+ 0.8F)
		);

		if (!world.isClientSide()) {
			EnderAirBottleEntity bottle =
					new EnderAirBottleEntity(
							player,
							world
					);

			bottle.shootFromRotation(
					player,
					player.getXRot(),
					player.getYRot(),
					0.0F,
					1.5F,
					1.0F
			);

			world.addFreshEntity(bottle);
		}

		return world.isClientSide()
				? InteractionResult.SUCCESS
				: InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public void onDestroyed(@NotNull ItemEntity entity) {
		var container = getInventory(entity.getItem());
		var stream = IntStream.range(0, container.getContainerSize())
				.mapToObj(container::getItem)
				.filter(s -> !s.isEmpty());
		ItemUtils.onContainerDestroyed(entity, stream);
		container.clearContent();
	}

	@Override
	public boolean overrideStackedOnOther(
			@NotNull ItemStack box, @NotNull Slot slot,
			@NotNull ClickAction clickAction, @NotNull Player player) {
		return InventoryHelper.overrideStackedOnOther(
				BaubleBoxItem::getInventory,
				player.containerMenu instanceof BaubleBoxContainer,
				box, slot, clickAction, player);
	}

	@Override
	public boolean overrideOtherStackedOnMe(
			@NotNull ItemStack box, @NotNull ItemStack toInsert,
			@NotNull Slot slot, @NotNull ClickAction clickAction,
			@NotNull Player player, @NotNull SlotAccess cursorAccess) {
		return InventoryHelper.overrideOtherStackedOnMe(
				BaubleBoxItem::getInventory,
				player.containerMenu instanceof BaubleBoxContainer,
				box, toInsert, clickAction, cursorAccess);
	}
}
