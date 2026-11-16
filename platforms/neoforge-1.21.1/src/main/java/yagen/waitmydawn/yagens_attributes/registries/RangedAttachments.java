package yagen.waitmydawn.yagens_attributes.registries;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.data.ShotData;
import yagen.waitmydawn.yagens_attributes.data.ShieldState;
import yagen.waitmydawn.yagens_attributes.data.StatusState;
import yagen.waitmydawn.yagens_attributes.data.ComboState;
import yagen.waitmydawn.yagens_attributes.data.KillBuffState;
import java.util.function.Supplier;

public final class RangedAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, YagensAttributes.MODID);
    public static final Supplier<AttachmentType<ShotData>> SHOT = REGISTER.register("ranged_shot", () -> AttachmentType.builder(() -> ShotData.EMPTY).serialize(ShotData.CODEC).build());
    public static final Supplier<AttachmentType<ShieldState>> SHIELD = REGISTER.register("shield", () -> AttachmentType.builder(ShieldState::new).serialize(ShieldState.CODEC).build());
    public static final Supplier<AttachmentType<StatusState>> STATUS = REGISTER.register("status", () -> AttachmentType.builder(StatusState::new).serialize(StatusState.CODEC).build());
    public static final Supplier<AttachmentType<ComboState>> COMBO = REGISTER.register("combo", () -> AttachmentType.builder(ComboState::new).build());
    public static final Supplier<AttachmentType<KillBuffState>> KILL_BUFFS = REGISTER.register("kill_buffs", () -> AttachmentType.builder(KillBuffState::new).build());
    private RangedAttachments() {}
}
