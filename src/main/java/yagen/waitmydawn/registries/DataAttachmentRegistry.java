package yagen.waitmydawn.registries;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import yagen.waitmydawn.YagensAttributes;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;


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

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Combo>> COMBO =
            ATTACHMENT_TYPES.register("combo",
                    () -> AttachmentType.builder(() -> Combo.EMPTY)
                            .serialize(Combo.CODEC)
                            .copyOnDeath()
                            .build());

    public static int getComboLevel(int comboCount) {
        return Math.min(comboCount / 10, 14);
    }
}
