/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.crafting.RunicAltarRecipe;
import vazkii.botania.common.crafting.RecipeCodecs;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadRecipe extends RunicAltarRecipe {
	private static final MapCodec<HeadRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					RecipeCodecs.ITEM_STACK_TEMPLATE.fieldOf("output")
							.forGetter(recipe -> recipe.output),
					Codec.INT.fieldOf("mana")
							.forGetter(HeadRecipe::getManaUsage),
					Ingredient.CODEC.listOf().fieldOf("ingredients")
							.forGetter(recipe -> recipe.inputs)
			).apply(instance, HeadRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, HeadRecipe>
			STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	public static final RecipeSerializer<HeadRecipe> SERIALIZER =
			new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private static final Pattern PROFILE_PATTERN = Pattern.compile(
			"(?<base64>[A-Za-z0-9+/]{100,}={0,2})" +
					"|(?<url>(?=\\S{50,})https?://(?!bugs|education|feedback)\\w+\\.(?:minecraft\\.net|mojang\\.com)/\\S+)" +
					"|(?<hash>[0-9a-f]{64})");
	public static final String TEXTURE_URL_BASE = "https://textures.minecraft.net/texture/";
	private static final Supplier<Gson> gson = Suppliers.memoize(() -> new GsonBuilder()
			.registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create());
	private static final GameProfile PROFILE_VALID_RESULT = new GameProfile(null, "valid");
	private static final LoadingCache<String, UUID> GENERATED_UUID_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.MINUTES).build(
					new CacheLoader<>() {
						@NotNull
						@Override
						public UUID load(@NotNull String key) {
							return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
						}
					});

	public HeadRecipe(Identifier id, ItemStack output, int mana, Ingredient... inputs) {
		super(id, output, mana, inputs);
	}

	public HeadRecipe(
			ItemStackTemplate output,
			int mana,
			List<Ingredient> inputs
	) {
		super(output, mana, inputs);
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		boolean matches = super.matches(input, level);
		boolean foundName = false;

		if (matches) {
			for (int slot = 0; slot < input.size(); slot++) {
				ItemStack stack = input.getItem(slot);
				if (stack.isEmpty()) {
					break;
				}

				// either exactly one name tag or exactly one written book among ingredients
				if (stack.is(Items.NAME_TAG)) {
					if (foundName || !stack.has(DataComponents.CUSTOM_NAME) || stack.getHoverName().getString().isBlank()) {
						return false;
					}
					foundName = true;
				} else if (stack.is(Items.WRITTEN_BOOK)) {
					if (foundName || parseProfileFromBook(stack, true) == null) {
						return false;
					}
					foundName = true;
				}
			}
		}

		return matches && foundName;
	}

	@Override
	public ItemStack assemble(RecipeInput input) {
		ItemStack stack = this.output.create();
		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack ingr = input.getItem(slot);
			if (ingr.is(Items.NAME_TAG)) {
				stack.set(DataComponents.PROFILE,
						ResolvableProfile.createResolved(new GameProfile(null, ingr.getHoverName().getString())));
				break;
			}
			if (ingr.is(Items.WRITTEN_BOOK)) {
				GameProfile profile = parseProfileFromBook(ingr, false);
				if (profile != null) {
					stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
				}
				break;
			}
		}
		return stack;
	}

	private GameProfile parseProfileFromBook(ItemStack stack, boolean validateOnly) {
		WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (content == null) {
			return null;
		}
		String name = content.title().raw();
		if (name.isBlank()) {
			return null;
		}

		// At most the first two pages are scanned.
		int maxPages = Math.min(2, content.pages().size());
		for (int i = 0; i < maxPages; ++i) {
			String pageText = content.pages().get(i).raw().getString();

			Matcher matcher = PROFILE_PATTERN.matcher(pageText);
			if (matcher.matches()) {
				// this appears to be the page we were looking for, figure out the skin texture it encodes
				String textureUrl;
				String hash, base64, url;
				if ((hash = matcher.group("hash")) != null) {
					// simplest case: just the texture hash; complete the URL
					textureUrl = TEXTURE_URL_BASE + hash;
				} else if ((url = matcher.group("url")) != null) {
					// an entire URL was specified; make sure it looks valid
					try {
						// just basic URL validation so we don't potentially spam error logs
						URL validUrl = new URL(url);
						textureUrl = validUrl.toString();
					} catch (Exception e) {
						return null;
					}
				} else if ((base64 = matcher.group("base64")) != null) {
					// complete profile properties; do rudimentary parsing
					try {
						final String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
						MinecraftTexturesPayload result = gson.get().fromJson(json, MinecraftTexturesPayload.class);
						MinecraftProfileTexture skinTexture = result.textures().get(MinecraftProfileTexture.Type.SKIN);
						String skinTextureUrl = skinTexture.getUrl();
						if (!PROFILE_PATTERN.matcher(skinTextureUrl).matches()) {
							return null;
						}
						URL validUrl = new URL(skinTextureUrl);
						textureUrl = validUrl.toString();
					} catch (Exception e) {
						return null;
					}
				} else {
					return null;
				}
				if (validateOnly) {
					return PROFILE_VALID_RESULT;
				}
				// we got something that looks like a valid skin texture URL, now build rudimentary profile data
				String profileTextureJson = "{textures:{SKIN:{url:\"%s\"}}}".formatted(textureUrl);
				String propertyBase64 = Base64.getEncoder().encodeToString(profileTextureJson.getBytes(StandardCharsets.UTF_8));
				var profile = new GameProfile(GENERATED_UUID_CACHE.getUnchecked(propertyBase64), name);
				profile.properties().put("textures", new Property("textures", propertyBase64));
				return profile;
			}
		}
		return null;
	}

	@Override
	public RecipeSerializer<HeadRecipe> getSerializer() {
		return SERIALIZER;
	}
}
