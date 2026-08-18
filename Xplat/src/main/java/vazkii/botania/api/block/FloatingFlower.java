/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.block;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface FloatingFlower {

	/**
	 * @return The itemstack to display on top of the island
	 */
	ItemStack getDisplayStack();

	IslandType getIslandType();

	void setIslandType(IslandType type);

	Tag writeNBT();

	void readNBT(CompoundTag nbt);

	class IslandType {
		private static final Map<String, IslandType> registry = new HashMap<>();

		public static final IslandType GRASS = register("GRASS");
		public static final IslandType PODZOL = register("PODZOL");
		public static final IslandType MYCEL = register("MYCEL");
		public static final IslandType SNOW = register("SNOW");
		public static final IslandType DRY = register("DRY");
		public static final IslandType GOLDEN = register("GOLDEN");
		public static final IslandType VIVID = register("VIVID");
		public static final IslandType SCORCHED = register("SCORCHED");
		public static final IslandType INFUSED = register("INFUSED");
		public static final IslandType MUTATED = register("MUTATED");

		private final String typeName;

		/**
		 * Instantiates a floating flower island type. Use {@link #register(String)} for a plain
		 * island type, or {@link #register(String, Function)} for a subclass.
		 * Note that you need to register the model for this island type, see BotaniaAPIClient
		 * 
		 * @param name The name of this floating flower island type
		 */
		protected IslandType(String name) {
			typeName = name;
		}

		public static IslandType register(String name) {
			return register(name, IslandType::new);
		}

		public static <T extends IslandType> T register(String name, Function<String, T> factory) {
			synchronized (registry) {
				if (registry.containsKey(name)) {
					throw new IllegalArgumentException(name + " already registered!");
				}
				T type = factory.apply(name);
				registry.put(name, type);
				return type;
			}
		}

		public static IslandType ofType(String typeStr) {
			synchronized (registry) {
				IslandType type = registry.get(typeStr);
				return type == null ? GRASS : type;
			}
		}

		@Override
		public String toString() {
			return typeName;
		}

	}

}
