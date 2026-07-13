/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.record;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;

import vazkii.botania.common.lib.LibItemNames;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class BotaniaRecordItem extends Item {
	public static final ResourceKey<JukeboxSong> GAIA_1 =
			ResourceKey.create(
					Registries.JUKEBOX_SONG,
					prefix(LibItemNames.RECORD_GAIA1)
			);

	public static final ResourceKey<JukeboxSong> GAIA_2 =
			ResourceKey.create(
					Registries.JUKEBOX_SONG,
					prefix(LibItemNames.RECORD_GAIA2)
			);

	public BotaniaRecordItem(
			ResourceKey<JukeboxSong> song,
			Item.Properties properties) {
		super(properties.jukeboxPlayable(song));
	}
}