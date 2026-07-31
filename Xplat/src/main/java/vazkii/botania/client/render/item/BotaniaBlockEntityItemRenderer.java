/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.joml.Quaternionf;
import org.joml.Vector3fc;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.*;
import vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer;
import vazkii.botania.client.render.block_entity.state.BellowsRenderState;
import vazkii.botania.client.render.block_entity.state.BotanicalBreweryRenderState;
import vazkii.botania.client.render.block_entity.state.HoveringHourglassRenderState;
import vazkii.botania.client.render.block_entity.state.TeruTeruBozuRenderState;
import vazkii.botania.client.render.entity.EntityRenderers;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.PylonBlock;
import vazkii.botania.common.helper.VecHelper;

import java.util.List;
import java.util.function.Consumer;

public class BotaniaBlockEntityItemRenderer implements SpecialModelRenderer<ItemStack> {
	private static final float CORPOREA_ANGLE = (float) Math.sin(Math.toRadians(45));

	private final ItemRenderDelegate delegate;

	private BotaniaBlockEntityItemRenderer(ItemRenderDelegate delegate) {
		this.delegate = delegate;
	}

	@Override
	public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			int light, int overlay, boolean hasFoil, int outlineColor) {
		delegate.submit(stack, poseStack, submitNodeCollector, light, overlay, hasFoil, outlineColor);
	}

	@Override
	public ItemStack extractArgument(ItemStack stack) {
		return stack;
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		delegate.getExtents(output);
	}

	private interface ItemRenderDelegate {
		void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
				int light, int overlay, boolean hasFoil, int outlineColor);

		void getExtents(Consumer<Vector3fc> output);
	}

	private abstract static class RootDelegate implements ItemRenderDelegate {
		protected final ModelPart root;

		private RootDelegate(ModelPart root) {
			this.root = root;
		}
	}

	public record Unbaked(Identifier block, boolean direct) implements SpecialModelRenderer.Unbaked<ItemStack> {
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Identifier.CODEC.fieldOf("block").forGetter(Unbaked::block),
				Codec.BOOL.optionalFieldOf("direct", false).forGetter(Unbaked::direct)
		).apply(instance, Unbaked::new));

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
			Block resolved = BuiltInRegistries.BLOCK.getValue(block);
			if (resolved == Blocks.AIR || !EntityRenderers.BE_ITEM_RENDERER_BLOCKS.contains(resolved)) {
				throw new IllegalArgumentException("Unsupported Botania block entity item renderer: " + block);
			}

			if (resolved instanceof PylonBlock pylon) {
				return new BotaniaBlockEntityItemRenderer(bakePylon(context, pylon, direct));
			}
			if (resolved == BotaniaBlocks.teruTeruBozu) {
				return new BotaniaBlockEntityItemRenderer(bakeTeruTeruBozu(context));
			}
			if (resolved == BotaniaBlocks.avatar) {
				return new BotaniaBlockEntityItemRenderer(bakeAvatar(context));
			}
			if (resolved == BotaniaBlocks.bellows) {
				return new BotaniaBlockEntityItemRenderer(bakeBellows(context));
			}
			if (resolved == BotaniaBlocks.brewery) {
				return new BotaniaBlockEntityItemRenderer(bakeBrewery(context));
			}
			if (resolved == BotaniaBlocks.corporeaIndex) {
				return new BotaniaBlockEntityItemRenderer(bakeCorporeaIndex(context));
			}
			if (resolved == BotaniaBlocks.hourglass) {
				return new BotaniaBlockEntityItemRenderer(bakeHourglass(context));
			}
			throw new IllegalArgumentException("No item renderer delegate for supported block: " + block);
		}
	}

	private static ItemRenderDelegate bakePylon(SpecialModelRenderer.BakingContext context,
			PylonBlock pylon, boolean direct) {
		ModelPart root;
		PylonModel model;
		Identifier texture;
		RenderType glow;
		switch (pylon.variant) {
			case MANA -> {
				root = context.entityModelSet().bakeLayer(BotaniaModelLayers.PYLON_MANA);
				model = new ManaPylonModel(root);
				texture = PylonBlockEntityRenderer.MANA_TEXTURE;
				glow = direct ? RenderHelper.MANA_PYLON_GLOW_DIRECT : RenderHelper.MANA_PYLON_GLOW;
			}
			case NATURA -> {
				root = context.entityModelSet().bakeLayer(BotaniaModelLayers.PYLON_NATURA);
				model = new NaturaPylonModel(root);
				texture = PylonBlockEntityRenderer.NATURA_TEXTURE;
				glow = direct ? RenderHelper.NATURA_PYLON_GLOW_DIRECT : RenderHelper.NATURA_PYLON_GLOW;
			}
			case GAIA -> {
				root = context.entityModelSet().bakeLayer(BotaniaModelLayers.PYLON_GAIA);
				model = new GaiaPylonModel(root);
				texture = PylonBlockEntityRenderer.GAIA_TEXTURE;
				glow = direct ? RenderHelper.GAIA_PYLON_GLOW_DIRECT : RenderHelper.GAIA_PYLON_GLOW;
			}
			default -> throw new IllegalArgumentException("Unsupported pylon variant: " + pylon.variant);
		}

		return new RootDelegate(root) {
			@Override
			public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
					int light, int overlay, boolean hasFoil, int outlineColor) {
				poseStack.pushPose();
				poseStack.translate(0, 1.35, 0);
				poseStack.scale(1, -1, -1);
				poseStack.translate(0.5F, 0, -0.5F);
				model.submitRing(poseStack, collector, RenderTypes.entityTranslucent(texture),
						light, overlay, hasFoil, outlineColor);
				model.submitCrystal(poseStack, collector, glow, light, overlay, hasFoil, outlineColor);
				poseStack.popPose();
			}

			@Override
			public void getExtents(Consumer<Vector3fc> output) {
				PoseStack poseStack = new PoseStack();
				poseStack.translate(0, 1.35, 0);
				poseStack.scale(1, -1, -1);
				poseStack.translate(0.5F, 0, -0.5F);
				model.collectExtents(poseStack, output);
			}
		};
	}

	private static ItemRenderDelegate bakeTeruTeruBozu(SpecialModelRenderer.BakingContext context) {
		ModelPart root = context.entityModelSet().bakeLayer(BotaniaModelLayers.TERU_TERU_BOZU);
		TeruTeruBozuModel model = new TeruTeruBozuModel(root);
		return new RootDelegate(root) {
			@Override
			public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
					int light, int overlay, boolean hasFoil, int outlineColor) {
				TeruTeruBozuRenderState state = new TeruTeruBozuRenderState();
				state.animationTime = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
				state.raining = false;
				state.halloween = ClientProxy.dootDoot;
				state.lightCoords = light;
				poseStack.pushPose();
				poseStack.mulPose(VecHelper.rotateX(180));
				poseStack.translate(0.5F, -1.25F + (float) Math.sin(state.animationTime * 0.01F) * 0.05F, -0.5F);
				poseStack.mulPose(VecHelper.rotateY((float) (state.animationTime * 0.3)));
				poseStack.mulPose(VecHelper.rotateZ(4F * (float) Math.sin(state.animationTime * 0.05F)));
				poseStack.scale(0.75F, 0.75F, 0.75F);
				Identifier texture = Identifier.parse(state.halloween
						? ResourcesLib.MODEL_TERU_TERU_BOZU_HALLOWEEN : ResourcesLib.MODEL_TERU_TERU_BOZU);
				model.submit(state, poseStack, collector, texture, overlay, hasFoil, outlineColor);
				poseStack.popPose();
			}

			@Override
			public void getExtents(Consumer<Vector3fc> output) {
				for (int sample = 0; sample < 16; sample++) {
					float rotation = sample * 360F / 16F;
					for (float rock : new float[] { -4F, 4F }) {
						for (float bob : new float[] { -0.05F, 0.05F }) {
							PoseStack poseStack = new PoseStack();
							poseStack.mulPose(VecHelper.rotateX(180));
							poseStack.translate(0.5F, -1.25F + bob, -0.5F);
							poseStack.mulPose(VecHelper.rotateY(rotation));
							poseStack.mulPose(VecHelper.rotateZ(rock));
							poseStack.scale(0.75F, 0.75F, 0.75F);
							model.collectItemExtents(poseStack, output);
						}
					}
				}
			}
		};
	}

	private static ItemRenderDelegate bakeAvatar(SpecialModelRenderer.BakingContext context) {
		ModelPart root = context.entityModelSet().bakeLayer(BotaniaModelLayers.AVATAR);
		AvatarModel model = new AvatarModel(root);
		Identifier texture = Identifier.parse(ResourcesLib.MODEL_AVATAR);
		return new RootDelegate(root) {
			@Override
			public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
					int light, int overlay, boolean hasFoil, int outlineColor) {
				poseStack.pushPose();
				poseStack.translate(0.5F, 1.6F, 0.5F);
				poseStack.scale(1, -1, -1);
				poseStack.mulPose(VecHelper.rotateY(180));
				collector.submitModelPart(model.root(), poseStack, model.renderType(texture), light, overlay,
						null, false, hasFoil, -1, null, outlineColor);
				poseStack.popPose();
			}

			@Override
			public void getExtents(Consumer<Vector3fc> output) {
				PoseStack poseStack = new PoseStack();
				poseStack.translate(0.5F, 1.6F, 0.5F);
				poseStack.scale(1, -1, -1);
				poseStack.mulPose(VecHelper.rotateY(180));
				model.root().getExtentsForGui(poseStack, output);
			}
		};
	}

	private static ItemRenderDelegate bakeBellows(SpecialModelRenderer.BakingContext context) {
		ModelPart root = context.entityModelSet().bakeLayer(BotaniaModelLayers.BELLOWS);
		BellowsModel model = new BellowsModel(root);
		Identifier texture = Identifier.parse(ResourcesLib.MODEL_BELLOWS);
		return new RootDelegate(root) {
			@Override
			public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
					int light, int overlay, boolean hasFoil, int outlineColor) {
				BellowsRenderState state = new BellowsRenderState();
				state.rotationDegrees = 0;
				state.contractionFraction = 1;
				state.lightCoords = light;
				poseStack.pushPose();
				poseStack.translate(0.5F, 1.5F, 0.5F);
				poseStack.scale(1, -1, -1);
				model.submit(state, poseStack, collector, texture, overlay, hasFoil, outlineColor);
				poseStack.popPose();
			}

			@Override
			public void getExtents(Consumer<Vector3fc> output) {
				PoseStack poseStack = new PoseStack();
				poseStack.translate(0.5F, 1.5F, 0.5F);
				poseStack.scale(1, -1, -1);
				model.collectItemExtents(poseStack, output);
			}
		};
	}

	private static ItemRenderDelegate bakeBrewery(SpecialModelRenderer.BakingContext context) {
		ModelPart root = context.entityModelSet().bakeLayer(BotaniaModelLayers.BREWERY);
		BotanicalBreweryModel model = new BotanicalBreweryModel(root);
		Identifier texture = Identifier.parse(ResourcesLib.MODEL_BREWERY);
		return new RootDelegate(root) {
			@Override
			public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
					int light, int overlay, boolean hasFoil, int outlineColor) {
				BotanicalBreweryRenderState state = new BotanicalBreweryRenderState();
				state.animationTime = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
				state.plateCount = 0;
				state.items = List.of();
				state.lightCoords = light;
				poseStack.pushPose();
				poseStack.scale(1, -1, -1);
				poseStack.translate(0.5F, -1.5F, -0.5F);
				model.submit(state, poseStack, collector, texture, overlay, hasFoil, outlineColor);
				poseStack.popPose();
			}

			@Override
			public void getExtents(Consumer<Vector3fc> output) {
				PoseStack poseStack = new PoseStack();
				poseStack.scale(1, -1, -1);
				poseStack.translate(0.5F, -1.5F, -0.5F);
				model.collectItemExtents(poseStack, output);
			}
		};
	}

	private static ItemRenderDelegate bakeCorporeaIndex(SpecialModelRenderer.BakingContext context) {
		ModelPart root = context.entityModelSet().bakeLayer(BotaniaModelLayers.CORPOREA_INDEX);
		ModelPart ring = root.getChild("ring");
		ModelPart cube = root.getChild("cube");
		RenderType renderType = RenderTypes.entityCutoutNoCull(Identifier.parse(ResourcesLib.MODEL_CORPOREA_INDEX));
		return new RootDelegate(root) {
			@Override
			public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
					int light, int overlay, boolean hasFoil, int outlineColor) {
				float rotation = (ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks) * 2;
				poseStack.pushPose();
				poseStack.translate(0.5, 0, 0.5);
				poseStack.scale(1.3F, 1.3F, 1.3F);
				poseStack.translate(0, -0.1, 0);
				poseStack.translate(0, -1, 0);
				poseStack.mulPose(VecHelper.rotateY(rotation));
				poseStack.translate(0, 1.5F, 0);
				poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), CORPOREA_ANGLE, 0, CORPOREA_ANGLE));
				submitPart(ring, poseStack, collector, renderType, light, overlay, hasFoil, outlineColor);
				poseStack.scale(0.875F, 0.875F, 0.875F);
				poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), CORPOREA_ANGLE, 0, CORPOREA_ANGLE));
				poseStack.mulPose(VecHelper.rotateY(rotation));
				submitPart(ring, poseStack, collector, renderType, light, overlay, hasFoil, outlineColor);
				poseStack.scale(0.875F, 0.875F, 0.875F);
				poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), CORPOREA_ANGLE, 0, CORPOREA_ANGLE));
				poseStack.mulPose(VecHelper.rotateY(rotation));
				submitPart(cube, poseStack, collector, renderType, light, overlay, hasFoil, outlineColor);
				poseStack.popPose();
			}

			@Override
			public void getExtents(Consumer<Vector3fc> output) {
				for (int sample = 0; sample < 16; sample++) {
					float rotation = sample * 360F / 16F;
					PoseStack poseStack = new PoseStack();
					poseStack.translate(0.5, 0, 0.5);
					poseStack.scale(1.3F, 1.3F, 1.3F);
					poseStack.translate(0, -0.1, 0);
					poseStack.translate(0, -1, 0);
					poseStack.mulPose(VecHelper.rotateY(rotation));
					poseStack.translate(0, 1.5F, 0);
					poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), CORPOREA_ANGLE, 0, CORPOREA_ANGLE));
					ring.getExtentsForGui(poseStack, output);
					poseStack.scale(0.875F, 0.875F, 0.875F);
					poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), CORPOREA_ANGLE, 0, CORPOREA_ANGLE));
					poseStack.mulPose(VecHelper.rotateY(rotation));
					ring.getExtentsForGui(poseStack, output);
					poseStack.scale(0.875F, 0.875F, 0.875F);
					poseStack.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(60), CORPOREA_ANGLE, 0, CORPOREA_ANGLE));
					poseStack.mulPose(VecHelper.rotateY(rotation));
					cube.getExtentsForGui(poseStack, output);
				}
			}
		};
	}

	private static ItemRenderDelegate bakeHourglass(SpecialModelRenderer.BakingContext context) {
		ModelPart root = context.entityModelSet().bakeLayer(BotaniaModelLayers.HOURGLASS);
		HourglassModel model = new HourglassModel(root);
		Identifier texture = Identifier.parse(ResourcesLib.MODEL_HOURGLASS);
		return new RootDelegate(root) {
			@Override
			public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
					int light, int overlay, boolean hasFoil, int outlineColor) {
				HoveringHourglassRenderState state = new HoveringHourglassRenderState();
				state.animationTime = 0;
				state.upperSandFraction = 0;
				state.lowerSandFraction = 0;
				state.flip = false;
				state.flipRotationDegrees = 1;
				state.sandColor = 0;
				state.lightCoords = light;
				poseStack.pushPose();
				poseStack.translate(0.55F, 0.6F, 0.5F);
				poseStack.mulPose(VecHelper.rotateZ(state.flipRotationDegrees));
				poseStack.scale(1, -1, -1);
				model.submit(state, poseStack, collector, texture, overlay, hasFoil, outlineColor);
				poseStack.popPose();
			}

			@Override
			public void getExtents(Consumer<Vector3fc> output) {
				PoseStack poseStack = new PoseStack();
				poseStack.translate(0.55F, 0.6F, 0.5F);
				poseStack.mulPose(VecHelper.rotateZ(1));
				poseStack.scale(1, -1, -1);
				model.collectItemExtents(poseStack, output);
			}
		};
	}

	private static void submitPart(ModelPart part, PoseStack poseStack, SubmitNodeCollector collector,
			RenderType renderType, int light, int overlay, boolean hasFoil, int outlineColor) {
		collector.submitModelPart(part, poseStack, renderType, light, overlay, null, false,
				hasFoil, -1, null, outlineColor);
	}
}
