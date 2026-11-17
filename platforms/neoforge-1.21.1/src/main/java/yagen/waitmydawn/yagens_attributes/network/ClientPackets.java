package yagen.waitmydawn.yagens_attributes.network;

import java.util.function.Consumer;
import java.util.function.Supplier;
import yagen.waitmydawn.yagens_attributes.core.ModCatalog;
import yagen.waitmydawn.yagens_attributes.data.ModDefinitions;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraft.world.entity.LivingEntity;

/** Physical-client setup installs handlers without linking client classes during server loading. */
public final class ClientPackets {
    private static Consumer<StatusEffectPayload> effects = payload -> {};
    public static void effects(Consumer<StatusEffectPayload> handler) { effects = handler; }
    public static void receive(StatusEffectPayload payload) { effects.accept(payload); }
    private static Consumer<WeaponBuffsPayload> weaponBuffs = payload -> {};
    public static void weaponBuffs(Consumer<WeaponBuffsPayload> handler) { weaponBuffs = handler; }
    public static void receive(WeaponBuffsPayload payload) { weaponBuffs.accept(payload); }
    private static Consumer<ProfilePayload> profiles = payload -> {};
    private static Consumer<PanelPayload> panel = payload -> {};
    private static Consumer<TooltipPayload> tooltip = payload -> {};
    private static Consumer<ProgressPayload> progress = payload -> {};
    private static Consumer<ChargePayload> charge = payload -> {};
    private static Consumer<ComboPayload> combo = payload -> {};
    private static Consumer<ShieldPayload> shield = payload -> {};
    private static Consumer<StatusVisualPayload> visual = payload -> {};
    private static Consumer<StatusMarkersPayload> markers = payload -> {};
    private static Consumer<ModCatalogPayload> modCatalog = payload -> {};
    private static Supplier<ModCatalog> displayCatalog = () -> ModDefinitions.INSTANCE.catalog();
    private static Consumer<LivingEntityUseItemEvent.Start> useStart = event -> {};
    private static Consumer<LivingEntityUseItemEvent.Tick> useTick = event -> {};
    private static Consumer<LivingEntity> useStop = entity -> {};
    private ClientPackets() {}
    public static void install(Consumer<ProfilePayload> profileHandler, Consumer<PanelPayload> panelHandler) { profiles = profileHandler; panel = panelHandler; }
    public static void inspection(Consumer<TooltipPayload> tooltipHandler, Consumer<ProgressPayload> progressHandler) { tooltip = tooltipHandler; progress = progressHandler; }
    public static void receive(ProfilePayload payload) { profiles.accept(payload); }
    public static void receive(PanelPayload payload) { panel.accept(payload); }
    public static void receive(TooltipPayload payload) { tooltip.accept(payload); }
    public static void receive(ProgressPayload payload) { progress.accept(payload); }
    public static void ranged(Consumer<ChargePayload> handler, Consumer<LivingEntityUseItemEvent.Start> start, Consumer<LivingEntityUseItemEvent.Tick> tick, Consumer<LivingEntity> stop) {
        charge = handler; useStart = start; useTick = tick; useStop = stop;
    }
    public static void receive(ChargePayload payload) { charge.accept(payload); }
    public static void combo(Consumer<ComboPayload> handler) { combo = handler; }
    public static void receive(ComboPayload payload) { combo.accept(payload); }
    public static void shield(Consumer<ShieldPayload> handler) { shield = handler; }
    public static void receive(ShieldPayload payload) { shield.accept(payload); }
    public static void visual(Consumer<StatusVisualPayload> handler) { visual = handler; }
    public static void receive(StatusVisualPayload payload) { visual.accept(payload); }
    public static void markers(Consumer<StatusMarkersPayload> handler) { markers = handler; }
    public static void receive(StatusMarkersPayload payload) { markers.accept(payload); }
    public static void mods(Consumer<ModCatalogPayload> handler, Supplier<ModCatalog> catalog) { modCatalog = handler; displayCatalog = catalog; }
    public static void receive(ModCatalogPayload payload) { modCatalog.accept(payload); }
    public static ModCatalog modCatalog() { return displayCatalog.get(); }
    public static void useStart(LivingEntityUseItemEvent.Start event) { useStart.accept(event); }
    public static void useTick(LivingEntityUseItemEvent.Tick event) { useTick.accept(event); }
    public static void useStop(LivingEntity entity) { useStop.accept(entity); }
}
