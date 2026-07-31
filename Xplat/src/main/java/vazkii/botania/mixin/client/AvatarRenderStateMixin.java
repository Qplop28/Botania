package vazkii.botania.mixin.client;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import vazkii.botania.client.render.accessory.AccessoryRenderEntry;
import vazkii.botania.client.render.accessory.BotaniaAvatarRenderState;

import java.util.ArrayList;
import java.util.List;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements BotaniaAvatarRenderState {
	@Unique
	private final List<AccessoryRenderEntry> botania$accessoryEntries = new ArrayList<>();

	@Override
	public List<AccessoryRenderEntry> botania$getAccessoryEntries() {
		return botania$accessoryEntries;
	}
}
