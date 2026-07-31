package vazkii.botania.client.render.accessory;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class BlockAccessoryRenderData extends AccessoryRenderData {
	public List<BlockStateModelPart> partsA = List.of();
	public List<BlockStateModelPart> partsB = List.of();
	public List<BlockStateModelPart> partsC = List.of();
	public final BlockModelRenderState blockState = new BlockModelRenderState();
	public boolean chestArmor;
	public double animationTime;
	public int tintColor = 0xFFFFFFFF;

	public static List<BlockStateModelPart> collect(BlockStateModel model) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(42), parts);
		return List.copyOf(parts);
	}
}
