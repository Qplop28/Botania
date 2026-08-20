/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelPickaxeItem;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.xplat.XplatAbstractions;

public class VitreousPickaxeItem extends ManasteelPickaxeItem {
	private static final String TAG_SILK_HACK = "botania:silk_hack";
	private static final int MANA_PER_DAMAGE = 160;
	private static final ToolMaterial MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_WOODEN_TOOL,
			125,
			4.8F,
			0.0F,
			10,
			BotaniaTags.Items.VITREOUS_PICKAXE_REPAIR_ITEMS
	);

	public VitreousPickaxeItem(Properties props) {
		super(MATERIAL, props, -1);
	}

	/*
	* No way to modify the loot context so we're gonna go braindead hack workaround here:
	* - When block starting to break, if the tool doesn't have silktouch already, add it and add a "temp silk touch" flag
	* - Every tick, if the "temp silk touch" flag is present, remove it and remove any silk touch enchants from the stack
	*/

	@SoftImplement("IForgeItem")
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
		BlockState state = player.level().getBlockState(pos);
		var silkTouch = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.SILK_TOUCH);
		boolean hasSilk = EnchantmentHelper.getItemEnchantmentLevel(silkTouch, itemstack) > 0;
		if (hasSilk || !isGlass(state)) {
			return false;
		}

		itemstack.enchant(silkTouch, 1);
		CustomData.update(DataComponents.CUSTOM_DATA, itemstack, tag -> tag.putBoolean(TAG_SILK_HACK, true));

		return false;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, world, entity, slot);
		CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		if (customData.contains(TAG_SILK_HACK)) {
			CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(TAG_SILK_HACK));
			var silkTouch = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
					.getOrThrow(Enchantments.SILK_TOUCH);
			EnchantmentHelper.updateEnchantments(stack, enchantments -> enchantments.removeIf(silkTouch::equals));
		}
	}

	private boolean isGlass(BlockState state) {
		return XplatAbstractions.INSTANCE.isInGlassTag(state);
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public int getSortingPriority(ItemStack stack, BlockState state) {
		return isGlass(state) ? Integer.MAX_VALUE : 0;
	}

}
