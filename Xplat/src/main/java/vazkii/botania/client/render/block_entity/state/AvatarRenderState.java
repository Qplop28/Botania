/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

public class AvatarRenderState extends BlockEntityRenderState {
	public float rotationDegrees;
	public final ItemStackRenderState heldItem = new ItemStackRenderState();
	public boolean hasHeldItem;
	public boolean hasOverlay;
	public @Nullable Identifier overlayTexture;
	public float overlayAlpha;
}
