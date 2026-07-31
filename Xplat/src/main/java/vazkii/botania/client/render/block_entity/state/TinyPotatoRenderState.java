/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.client.render.block_entity.state;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TinyPotatoRenderState extends BlockEntityRenderState {
	public String name = "";
	public Component displayName = Component.empty();
	public boolean enchanted;
	public float rotationY;
	public float jumpUp;
	public float jumpWiggle;
	public float rotationZ;
	public float partialTicks;
	public boolean renderBody;
	public boolean renderName;
	public Component sublabel = Component.empty();
	public int nameWidth;
	public int sublabelWidth;
	public int nameBackground;
	public BlockPos blockPos = BlockPos.ZERO;
	public Direction facing = Direction.NORTH;
	public List<BlockStateModelPart> bodyParts = List.of();
	public final ItemStackRenderState bodyItem = new ItemStackRenderState();
	public List<BlockStateModelPart> phiFlowerParts = List.of();
	public List<BlockStateModelPart> nerfBatParts = List.of();
	public List<BlockStateModelPart> goldfishParts = List.of();
	public boolean showPhiFlower;
	public boolean showNerfBat;
	public boolean showGoldfish;
	public boolean showMartyBoot;
	public boolean showJibrilHalo;
	public boolean showKingDaddyExtras;
	public boolean showDefaultFlower;
	public final ItemStackRenderState martyBoot = new ItemStackRenderState();
	public final ItemStackRenderState manaRing1 = new ItemStackRenderState();
	public final ItemStackRenderState manaRing2 = new ItemStackRenderState();
	public final ItemStackRenderState defaultFlower = new ItemStackRenderState();
	public final BlockModelRenderState cake = new BlockModelRenderState();
	public final List<ItemStackRenderState> attachedItems = itemStates(6);
	public final Direction[] attachedSides = new Direction[6];
	public final boolean[] attachedPresent = new boolean[6];
	public final boolean[] attachedBlock = new boolean[6];
	public final boolean[] attachedPotato = new boolean[6];
	public final boolean[] attachedSkull = new boolean[6];
	public final boolean[] attachedKing = new boolean[6];

	private static List<ItemStackRenderState> itemStates(int count) {
		List<ItemStackRenderState> states = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			states.add(new ItemStackRenderState());
		}
		return states;
	}
}
