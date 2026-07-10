/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric;

import vazkii.botania.xplat.BotaniaConfig;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;

/**
 * Temporary configuration provider used during the Minecraft 26.1 port.
 *
 * <p>The old Fiber configuration library has not been retained for this port.
 * This provider preserves Botania's previous default configuration values until
 * a replacement persistent configuration system is implemented.</p>
 */
public final class FiberBotaniaConfig {
	private FiberBotaniaConfig() {}

	public static void setup() {
		BotaniaConfig.setCommon(new Common());

		if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
			BotaniaConfig.setClient(new Client());
		}
	}

	private static final class Client implements BotaniaConfig.ClientConfigAccess {
		@Override
		public boolean lexiconRotatingItems() {
			return true;
		}

		@Override
		public boolean subtlePowerSystem() {
			return false;
		}

		@Override
		public boolean staticWandBeam() {
			return false;
		}

		@Override
		public boolean boundBlockWireframe() {
			return true;
		}

		@Override
		public boolean lexicon3dModel() {
			return true;
		}

		@Override
		public double flowerParticleFrequency() {
			return 0.75;
		}

		@Override
		public boolean elfPortalParticlesEnabled() {
			return true;
		}

		@Override
		public boolean renderAccessories() {
			return true;
		}

		@Override
		public boolean enableSeasonalFeatures() {
			return true;
		}

		@Override
		public boolean enableFancySkybox() {
			return true;
		}

		@Override
		public boolean enableFancySkyboxInNormalWorlds() {
			return false;
		}

		@Override
		public int manaBarHeight() {
			return 29;
		}

		@Override
		public boolean staticFloaters() {
			return false;
		}

		@Override
		public boolean debugInfo() {
			return true;
		}

		@Override
		public boolean referencesEnabled() {
			return true;
		}

		@Override
		public boolean splashesEnabled() {
			return true;
		}

		@Override
		public boolean useShaders() {
			return true;
		}
	}

	private static final class Common implements BotaniaConfig.ConfigAccess {
		@Override
		public boolean blockBreakParticles() {
			return true;
		}

		@Override
		public boolean blockBreakParticlesTool() {
			return true;
		}

		@Override
		public boolean chargingAnimationEnabled() {
			return true;
		}

		@Override
		public boolean silentSpreaders() {
			return false;
		}

		@Override
		public int spreaderTraceTime() {
			return 400;
		}

		@Override
		public boolean enderPickpocketEnabled() {
			return true;
		}

		@Override
		public boolean enchanterEnabled() {
			return true;
		}

		@Override
		public boolean relicsEnabled() {
			return true;
		}

		@Override
		public boolean invertMagnetRing() {
			return false;
		}

		@Override
		public int harvestLevelWeight() {
			return 2;
		}

		@Override
		public int harvestLevelBore() {
			return 3;
		}

		@Override
		public boolean gogSpawnWithLexicon() {
			return true;
		}

		@Override
		public int gogIslandScaleMultiplier() {
			return 8;
		}

		@Override
		public List<String> rannuncarpusItemBlacklist() {
			return List.of();
		}

		@Override
		public List<String> rannuncarpusModBlacklist() {
			return List.of("storagedrawers");
		}
	}
}