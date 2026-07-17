/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity.state;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

import java.util.List;

public class BabylonWeaponRenderState extends EntityRenderState {
	public float weaponRotation;
	public float charge;
	public float chargeMultiplier;
	public float iconScale;
	public float iconYOffset;
	public float iconRotation;
	public List<BlockStateModelPart> weaponModelParts = List.of();
}
