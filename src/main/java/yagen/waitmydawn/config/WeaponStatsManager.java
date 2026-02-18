package yagen.waitmydawn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.attribute.DefaultDamageTypeRegistry;
import yagen.waitmydawn.api.attribute.DefaultItemAttributes;
import yagen.waitmydawn.api.mods.RivenModPool;

import java.util.HashMap;
import java.util.Map;

import static yagen.waitmydawn.api.attribute.DefaultDamageTypeRegistry.*;

public class WeaponStatsManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public WeaponStatsManager() {
        super(GSON, "weapon_stats");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        DefaultItemAttributes.clear();
        DefaultDamageTypeRegistry.clear();
        RivenModPool.clear();

        registerBatch(SWORDS, SWORD_DISTR);
        registerBatch(PICKAXES, PICKAXE_DISTR);
        registerBatch(AXES, AXE_DISTR);
        registerBatch(SHOVELS, SHOVEL_DISTR);
        registerBatch(HOES, HOE_DISTR);
        registerBatch(TRIDENTS, TRIDENT_DISTR);
        registerBatch(MACES, MACE_DISTR);
        registerBatch(BOWS, BOW_DISTR);

        object.forEach((location, json) -> {
            try {
                String fullPath = location.getPath();
                int slashIndex = fullPath.indexOf('/');
                if (slashIndex == -1) return;

                String namespace = fullPath.substring(0, slashIndex);
                String path = fullPath.substring(slashIndex + 1);
                ResourceLocation itemRL = ResourceLocation.fromNamespaceAndPath(namespace, path);
                Item item = BuiltInRegistries.ITEM.get(itemRL);
                if (item == Items.AIR //&& !itemRL.equals(BuiltInRegistries.ITEM.getKey(Items.AIR))
                ) {
                    return;
                }

                WeaponStatDefinition def = WeaponStatDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(IllegalStateException::new);

                if (def.rivenDisposition().isPresent()) {
                    RivenModPool.register(item, def.rivenDisposition().get());
                }

                if (def.damageDistribution().isPresent()) {
                    Map<DamageType, Float> configMap = def.damageDistribution().get();

                    DefaultDamageTypeRegistry.registerSingle(item, configMap);
                }

                if (def.attributes().isPresent()) {
                    Map<ResourceLocation, Double> rawAttrs = def.attributes().get();
                    Map<Attribute, Double> resolvedAttrs = new HashMap<>();

                    rawAttrs.forEach((attrRL, value) -> {
                        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(attrRL);
                        if (attr != null) {
                            resolvedAttrs.put(attr, value);
                        } else {
                            System.err.println("Unknown Attribute: " + attrRL);
                        }
                    });

                    DefaultItemAttributes.put(item, resolvedAttrs);
                }

            } catch (Exception e) {
                YagensAttributes.LOGGER.error("Failed to apply weapon_stats: {}", location, e);
            }
        });
        YagensAttributes.LOGGER.info("Applied weapon_stats! Damage Distribution size: {}, Attribute size: {}, RivenModPool size: {}",
                DefaultDamageTypeRegistry.VANILLA_DAMAGE_TYPES.size(),
                DefaultItemAttributes.DEFAULTS.size(),
                RivenModPool.DISPOSITION_MAP.size()
        );
    }
}
