package vazkii.botania.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomizableContainerBlockEntity.class)
public interface RandomizableContainerBlockEntityAccessor {
	@Accessor
	Identifier getLootTable();
}
