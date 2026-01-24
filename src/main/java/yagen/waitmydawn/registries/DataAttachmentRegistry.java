package yagen.waitmydawn.registries;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import yagen.waitmydawn.YagensAttributes;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.*;


public class DataAttachmentRegistry {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, YagensAttributes.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public record Combo(int count, int leftDuration) {

        public static final Codec<Combo> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("count").forGetter(Combo::count),
                Codec.INT.fieldOf("leftDuration").forGetter(Combo::leftDuration)
        ).apply(i, Combo::new));

        public static final Combo EMPTY = new Combo(0, 0);

        public int getComboLevel() {
            return Math.min(count / ServerConfigs.COMBO_LEVEL_NEEDED_COUNT.get(), ServerConfigs.MAX_COMBO_LEVEL.get());
        }

        public Combo withCount(int newCount) {
            return new Combo(newCount, this.leftDuration);
        }

        public Combo withDuration(int newDuration) {
            return new Combo(this.count, newDuration);
        }

        public Combo decrement() {
            return leftDuration <= 0 ? this : new Combo(count, leftDuration - 1);
        }
    }

    public record PreShoot(int count, int painLevel, int morePainLevel) {

        public static final Codec<PreShoot> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("count").forGetter(PreShoot::count),
                Codec.INT.fieldOf("painLevel").forGetter(PreShoot::painLevel),
                Codec.INT.fieldOf("morePainLevel").forGetter(PreShoot::morePainLevel)
        ).apply(i, PreShoot::new));

        public static final PreShoot EMPTY = new PreShoot(0, 0, 0);

        public int getPainLevel() {
            return painLevel + morePainLevel;
        }

        public PreShoot withCount(int newCount) {
            return new PreShoot(newCount, painLevel, morePainLevel);
        }

        public PreShoot modifierCount(int modifierCount) {
            if (modifierCount == 0) return new PreShoot(0, 0, morePainLevel);

            int newCount = Math.min(72000, count + modifierCount);
            int newPainLevel = 0;
            if (newCount >= 3600 && newCount <= 36000) newPainLevel = 1;
            else if (newCount > 36000) newPainLevel = 2;

            return new PreShoot(newCount, newPainLevel, morePainLevel);
        }

        public PreShoot modifierMorePainLevel(int modifierPainLevel) {
            if (modifierPainLevel == 0) return new PreShoot(count, painLevel, 0);
            return new PreShoot(count, painLevel, Math.min(morePainLevel + modifierPainLevel, 2));
        }

        public PreShoot setMorePainLevel(int morePainLevel) {
            return new PreShoot(count, painLevel, Math.min(morePainLevel, 2));
        }
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Combo>> COMBO =
            ATTACHMENT_TYPES.register("combo",
                    () -> AttachmentType.builder(() -> Combo.EMPTY)
                            .serialize(Combo.CODEC)
                            .copyOnDeath()
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PreShoot>> PRE_SHOOT_COUNT =
            ATTACHMENT_TYPES.register("pre_shoot_count",
                    () -> AttachmentType.builder(() -> PreShoot.EMPTY)
                            .serialize(PreShoot.CODEC)
                            .copyOnDeath()
                            .build());

    public record ReservoirBuffs(Map<Holder<MobEffect>, List<ModifierData>> buffMap) {

        public record ModifierData(Holder<Attribute> attribute, ResourceLocation id) {
            public static final Codec<ModifierData> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute")
                                    .forGetter(ModifierData::attribute),
                            ResourceLocation.CODEC.fieldOf("id").forGetter(ModifierData::id)
                    ).apply(instance, ModifierData::new));
        }

        public static final ReservoirBuffs EMPTY = new ReservoirBuffs(Map.of());

        public static final Codec<ReservoirBuffs> CODEC = Codec.unboundedMap(
                BuiltInRegistries.MOB_EFFECT.holderByNameCodec(),
                ModifierData.CODEC.listOf()
        ).xmap(ReservoirBuffs::new, ReservoirBuffs::buffMap);

        public ReservoirBuffs add(Holder<MobEffect> effect, Holder<Attribute> attr, ResourceLocation id) {
            List<ModifierData> oldList = this.buffMap.getOrDefault(effect, List.of());
            for (ModifierData data : oldList) {
                if (data.id().equals(id) && data.attribute().equals(attr)) {
                    return this;
                }
            }
            Map<Holder<MobEffect>, List<ModifierData>> newMap = new HashMap<>(this.buffMap);
            List<ModifierData> newList = new ArrayList<>(oldList);
            newList.add(new ModifierData(attr, id));
            newMap.put(effect, newList);
            return new ReservoirBuffs(newMap);
        }

        public List<ModifierData> getModifiers(Holder<MobEffect> effect) {
            return this.buffMap.getOrDefault(effect, List.of());
        }

        public ReservoirBuffs remove(Holder<MobEffect> effect) {
            if (!this.buffMap.containsKey(effect)) return this;

            Map<Holder<MobEffect>, List<ModifierData>> newMap = new HashMap<>(this.buffMap);
            newMap.remove(effect);
            return new ReservoirBuffs(newMap);
        }
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReservoirBuffs>> RESERVOIR_BUFFS =
            ATTACHMENT_TYPES.register("reservoir_buffs",
                    () -> AttachmentType.builder(() -> ReservoirBuffs.EMPTY)
                            .serialize(ReservoirBuffs.CODEC)
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>>
            SHOULD_HEAL = ATTACHMENT_TYPES.register("should_heal",
            () -> AttachmentType.builder(() -> true)
                    .serialize(Codec.BOOL)
                    .build());

    public static boolean getShouldHeal(Entity entity) {
        return entity.getData(SHOULD_HEAL.get());
    }

    public static void setShouldHeal(Entity entity, boolean value) {
        entity.setData(SHOULD_HEAL.get(), value);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Double>>
            ENERGY = ATTACHMENT_TYPES.register("energy",
            () -> AttachmentType.builder(() -> 25.0)
                    .serialize(Codec.DOUBLE)
//                    .copyOnDeath()
                    .build());

    public static double getEnergy(Entity entity) {
        return entity.getData(ENERGY.get());
    }

    public static void setEnergy(Entity entity, double value) {
        entity.setData(ENERGY.get(), value);
    }

    public static void reduceEnergy(Entity entity, double value) {
        entity.setData(ENERGY.get(), Math.max(getEnergy(entity) + value, 0.0));
    }
}
