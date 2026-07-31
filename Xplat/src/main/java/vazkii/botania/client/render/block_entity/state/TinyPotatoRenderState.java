/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github: https://github.com/Vazkii/Botania
 */
package vazkii.botania.client.render.block_entity.state;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
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
	public List<BlockStateModelPart> extraModelParts = List.of();
	public final List<ItemStackRenderState> attachedItems = itemStates(6);
	public final Direction[] attachedSides = new Direction[6];
	public final boolean[] attachedPresent = new boolean[6];
	public final boolean[] attachedBlock = new boolean[6];
	public final boolean[] attachedPotato = new boolean[6];
	public final boolean[] attachedSkull = new boolean[6];
	public final boolean[] attachedKing = new boolean[6];
	public final List<ItemStackRenderState> extraItems = itemStates(4);
	public int extraItemCount;

	private static List<ItemStackRenderState> itemStates(int count) {
		List<ItemStackRenderState> states = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			states.add(new ItemStackRenderState());
		}
		return states;
	}
}
