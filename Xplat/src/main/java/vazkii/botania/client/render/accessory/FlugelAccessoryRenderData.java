package vazkii.botania.client.render.accessory;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import java.util.List;

public class FlugelAccessoryRenderData extends AccessoryRenderData {
	public List<BlockStateModelPart> wingParts = List.of();
	public int variant;
	public boolean flying;
	public float animationTime;
	public float flap;
	public int tintColor = 0xFFFFFFFF;
}
