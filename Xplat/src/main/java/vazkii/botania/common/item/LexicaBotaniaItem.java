/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.lib.BotaniaTags;

import java.util.List;

public class LexicaBotaniaItem extends Item implements ItemWithBannerPattern, CustomCreativeTabContents {

	public static final String TAG_ELVEN_UNLOCK = "botania:elven_unlock";

	public LexicaBotaniaItem(Properties settings) {
		super(settings);
	}

	public static boolean isOpen() {
		// Patchouli integration is disabled during the Minecraft 26.1 bootstrap.
		return false;
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		output.accept(this);
		ItemStack creative = new ItemStack(this);
		creative.getOrCreateTag().putBoolean(TAG_ELVEN_UNLOCK, true);
		output.accept(creative);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(getEdition().copy().withStyle(ChatFormatting.GRAY));
	}

	@NotNull
	@Override
	public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
		// Patchouli integration is disabled during the Minecraft 26.1 bootstrap.
		return InteractionResult.PASS;
	}

	public static Component getEdition() {
		// Patchouli integration is disabled during the Minecraft 26.1 bootstrap.
		return Component.literal("");
	}

	public static Component getTitle(ItemStack stack) {
		Component title = stack.getHoverName();

		// Akashic tome tag contains a `text` field, which is a stringified text component
		String akashicTomeNBT = "akashictome:displayName";
		if (stack.hasTag() && stack.getTag().contains(akashicTomeNBT)) {
			CompoundTag nameTextComponent = stack.getTag().getCompound(akashicTomeNBT);
			title = Component.Serializer.fromJson(nameTextComponent.getString("text"));
		}

		return title;
	}

	public static boolean isElven(ItemStack stack) {
		return stack.hasTag() && stack.getTag().getBoolean(TAG_ELVEN_UNLOCK);
	}

	// Random item to expose this as public
	public static BlockHitResult doRayTrace(Level world, Player player, ClipContext.Fluid fluidMode) {
		return Item.getPlayerPOVHitResult(world, player, fluidMode);
	}

	@Override
	public TagKey<BannerPattern> getBannerPattern() {
		return BotaniaTags.BannerPatterns.PATTERN_ITEM_LEXICON;
	}
}
