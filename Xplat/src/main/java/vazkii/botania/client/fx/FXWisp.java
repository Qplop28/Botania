/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class FXWisp extends SingleQuadParticle {
	private final boolean depthTest;
	private final float moteParticleScale;
	private final int moteHalfLife;

	public FXWisp(ClientLevel world, double d, double d1, double d2, double xSpeed, double ySpeed, double zSpeed,
			float size, float red, float green, float blue, boolean depthTest, float maxAgeMul, boolean noClip, float g,
			TextureAtlasSprite sprite) {
		super(world, d, d1, d2, sprite);
		// Set the supplied motion after the position-only super constructor.
		xd = xSpeed;
		yd = ySpeed;
		zd = zSpeed;
		rCol = red;
		gCol = green;
		bCol = blue;
		alpha = 0.375F;
		gravity = g;
		quadSize = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F * size;
		moteParticleScale = quadSize;
		lifetime = (int) (28D / (Math.random() * 0.3D + 0.7D) * maxAgeMul);
		this.depthTest = depthTest;

		moteHalfLife = lifetime / 2;
		setSize(0.01F, 0.01F);

		xo = x;
		yo = y;
		zo = z;
		this.hasPhysics = !noClip;
	}

	@Override
	public float getQuadSize(float partialTicks) {
		float agescale = (float) age / (float) moteHalfLife;
		if (agescale > 1F) {
			agescale = 2 - agescale;
		}

		quadSize = moteParticleScale * agescale * 0.5F;
		return quadSize;
	}

	@Override
	protected int getLightCoords(float partialTicks) {
		return 0xF000F0;
	}

	@Override
	protected Layer getLayer() {
		return BotaniaParticleRenderTypes.wisp(depthTest);
	}

	// [VanillaCopy] of super, without drag when onGround is true
	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		if (this.age++ >= this.lifetime) {
			this.remove();
		}

		this.yd -= this.gravity;
		this.move(this.xd, this.yd, this.zd);
		if (gravity == 0.0f) {
			this.xd *= 0.98;
			this.yd *= 0.98;
			this.zd *= 0.98;
		}
	}

	public void setGravity(float value) {
		gravity = value;
	}
}
