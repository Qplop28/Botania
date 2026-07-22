package vazkii.botania.client.model;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import vazkii.botania.client.render.BotaniaItemTintSource;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.ManaBlasterItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ManaBlasterItemModel(ItemModel gunNoClip, ItemModel gunClip, ItemModel missingLens,
		Map<Item, ItemModel> lensModels) implements ItemModel {
	private static final Transformation LENS_TRANSFORMATION = new Transformation(
			new Vector3f(-0.4F, 0.2F, 0.0F),
			VecHelper.rotateY(90),
			new Vector3f(0.625F, 0.625F, 0.625F),
			null);
	private static final List<ItemTintSource> TINTS = List.of(
			new BotaniaItemTintSource(0),
			new BotaniaItemTintSource(1),
			new BotaniaItemTintSource(2));

	@Override
	public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
			@Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		state.appendModelIdentityElement(this);
		boolean clip = ManaBlasterItem.hasClip(stack);
		state.appendModelIdentityElement(clip);
		(clip ? this.gunClip : this.gunNoClip).update(state, stack, resolver, displayContext, level, entity, seed);

		ItemStack lens = ManaBlasterItem.getLens(stack);
		if (!lens.isEmpty()) {
			state.appendModelIdentityElement(lens.getItem());
			this.lensModels.getOrDefault(lens.getItem(), this.missingLens).update(state, lens, resolver, displayContext, level, entity, seed);
		}
	}

	public record Unbaked(Identifier gunNoClip, Identifier gunClip, List<ItemTintSource> tints) implements ItemModel.Unbaked {
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Identifier.CODEC.fieldOf("gun_noclip").forGetter(Unbaked::gunNoClip),
				Identifier.CODEC.fieldOf("gun_clip").forGetter(Unbaked::gunClip),
				ItemTintSources.CODEC.listOf().optionalFieldOf("tints", TINTS).forGetter(Unbaked::tints)
		).apply(instance, Unbaked::new));

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(this.gunNoClip);
			resolver.markDependency(this.gunClip);
			for (Item item : BuiltInRegistries.ITEM) {
				ItemStack lens = item.getDefaultInstance();
				if (ManaBlasterItem.isValidLens(lens)) {
					resolver.markDependency(BuiltInRegistries.ITEM.getKey(item).withPrefix("item/"));
				}
			}
		}

		@Override
		public ItemModel bake(ItemModel.BakingContext context) {
			Map<Item, ItemModel> lenses = new java.util.HashMap<>();
			for (Item item : BuiltInRegistries.ITEM) {
				ItemStack lens = item.getDefaultInstance();
				if (ManaBlasterItem.isValidLens(lens)) {
					lenses.put(item, new CuboidItemModelWrapper.Unbaked(
							BuiltInRegistries.ITEM.getKey(item).withPrefix("item/"), Optional.of(LENS_TRANSFORMATION), this.tints).bake(context));
				}
			}
			return new ManaBlasterItemModel(
					new CuboidItemModelWrapper.Unbaked(this.gunNoClip, Optional.empty(), this.tints).bake(context),
					new CuboidItemModelWrapper.Unbaked(this.gunClip, Optional.empty(), this.tints).bake(context),
					new CuboidItemModelWrapper.Unbaked(Identifier.withDefaultNamespace("builtin/missing"), Optional.of(LENS_TRANSFORMATION), this.tints).bake(context),
					Map.copyOf(lenses));
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
