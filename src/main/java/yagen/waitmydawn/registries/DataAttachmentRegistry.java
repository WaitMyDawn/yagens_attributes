package yagen.waitmydawn.registries;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import yagen.waitmydawn.YagensAttributes;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yagen.waitmydawn.config.ServerConfigs;


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

    public static int getComboLevel(int comboCount) {
        return Math.min(comboCount / ServerConfigs.COMBO_LEVEL_NEEDED_COUNT.get(), ServerConfigs.MAX_COMBO_LEVEL.get());
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>>
            SHOULD_HEAL = ATTACHMENT_TYPES.register("should_heal",
            () -> AttachmentType.builder(() -> true)
                    .serialize(Codec.BOOL)
//                    .copyOnDeath()
                    .build());

    public static boolean getShouldHeal(Entity entity) {
        return entity.getData(SHOULD_HEAL.get());
    }
    public static void setShouldHeal(Entity entity, boolean value) {
        entity.setData(SHOULD_HEAL.get(), value);
    }
}
