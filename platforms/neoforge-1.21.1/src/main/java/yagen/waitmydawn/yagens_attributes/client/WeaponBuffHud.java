package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

@EventBusSubscriber(modid = "yagens_attributes", value = Dist.CLIENT)
public final class WeaponBuffHud {
    private record Key(UUID mod, int index) {}
    private static final class Cell {
        WeaponBuff buff; int count, flash, retiring = -1;
        Cell(WeaponBuff buff, long now) { this.buff = buff; count = buff.count(now); flash = 8; }
    }
    private static final Map<Key, Cell> CELLS = new LinkedHashMap<>();
    private static UUID weapon;
    private static long now, revision;
    private WeaponBuffHud() {}
    public static void clear() { CELLS.clear(); weapon = null; now = 0; revision++; }
    public static void accept(WeaponBuffsPayload payload) {
        if (!Objects.equals(weapon, payload.weapon())) CELLS.clear();
        weapon = payload.weapon(); now = payload.time();
        var present = new HashSet<Key>();
        for (var buff : payload.buffs()) {
            var key = new Key(buff.mod(), buff.index()); present.add(key);
            var cell = CELLS.get(key);
            if (cell == null) CELLS.put(key, new Cell(buff, now));
            else {
                if (!cell.buff.deadlines().equals(buff.deadlines())) cell.flash = 8;
                cell.buff = buff; cell.count = buff.count(now); cell.retiring = -1;
            }
        }
        CELLS.forEach((key, cell) -> { if (!present.contains(key) && cell.retiring < 0) cell.retiring = 6; });
        revision++;
    }
    public static void tick() {
        var client = Minecraft.getInstance();
        if (client.level == null || client.isPaused()) return;
        now++;
        if (now % 20 == 0) revision++;
        CELLS.values().removeIf(cell -> {
            int count = cell.buff.count(now);
            if (count != cell.count) { cell.flash = 6; cell.count = count; revision++; }
            if (count == 0 && cell.retiring < 0) cell.retiring = 6;
            if (cell.flash > 0) cell.flash--;
            return cell.retiring >= 0 && --cell.retiring <= 0;
        });
    }
    public static long revision() { return revision; }
    private static boolean editing() { return Minecraft.getInstance().screen instanceof HudEditorScreen screen && screen.edits(HudElement.BUFFS); }
    public static int logicalWidth() { return Math.min(ClientConfigs.BUFF_HUD_COLUMNS.get(), Math.max(1, editing() ? 4 : CELLS.size())) * 36; }
    public static int logicalHeight() { return logicalHeight(ClientConfigs.BUFF_HUD_SECONDS.get()); }
    public static int logicalHeight(boolean seconds) { return Math.max(1, ((editing() ? 4 : CELLS.size()) + ClientConfigs.BUFF_HUD_COLUMNS.get() - 1) / ClientConfigs.BUFF_HUD_COLUMNS.get()) * (seconds ? 44 : 36); }
    @SubscribeEvent public static void render(RenderGuiEvent.Post event) {
        var client = Minecraft.getInstance();
        if (editing() || client.player == null || !client.player.isAlive() || client.options.hideGui || !ClientConfigs.BUFF_HUD.get() || CELLS.isEmpty()
                || !Objects.equals(weapon, client.player.getMainHandItem().get(Components.WEAPON_ID.get()))) return;
        var box = HudElement.BUFFS.place(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), HudElement.BUFFS.settings());
        var graphics = event.getGuiGraphics(); graphics.pose().pushPose();
        graphics.pose().translate(box.x(), box.y(), 0); graphics.pose().scale(box.scale(), box.scale(), 1);
        int index = 0, columns = ClientConfigs.BUFF_HUD_COLUMNS.get();
        for (var cell : CELLS.values()) {
            draw(graphics, cell.buff, cell.retiring >= 0 ? 0 : cell.count, cell.buff.fraction(now), cell.flash, cell.retiring >= 0,
                    index % columns * 36, index / columns * (ClientConfigs.BUFF_HUD_SECONDS.get() ? 44 : 36), now, ClientConfigs.BUFF_HUD_SECONDS.get());
            index++;
        }
        graphics.pose().popPose();
    }
    private static void draw(GuiGraphics graphics, WeaponBuff buff, int count, float fraction, int flash, boolean retiring, int x, int y, long time, boolean secondsVisible) {
        var client = Minecraft.getInstance(); int color = 0xFFB9C6D1;
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("yagens_attributes", "item/mod");
        try {
            var info = ClientPackets.modCatalog().card(buff.type(), buff.rank()).info(); color = 0xFF000000 | info.color();
            texture = ResourceLocation.parse(info.style().texture(info.details().type(), info.details().texture()));
        } catch (IllegalArgumentException ignored) { }
        graphics.pose().pushPose(); graphics.pose().translate(x + 16, y + 16, 0);
        float scale = retiring ? 1 - flash * .015F : 1 + flash * .012F;
        graphics.pose().scale(scale, scale, 1); graphics.pose().translate(-16, -16, 0);
        graphics.blit(4, 4, 0, 24, 24, client.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture));
        int active = Math.round(fraction * 120);
        for (int i = 0; i < 120; i++) {
            int edge = i / 30, step = i % 30;
            int px = edge == 0 ? step + 1 : edge == 1 ? 31 : edge == 2 ? 30 - step : 0;
            int py = edge == 0 ? 0 : edge == 1 ? step + 1 : edge == 2 ? 31 : 30 - step;
            graphics.fill(px, py, px + 1, py + 1, i >= active ? 0x6046515D : retiring ? 0xFFE98A80 : flash > 0 ? 0xFFFFFFFF : color);
        }
        String stacks = Integer.toString(count);
        graphics.drawString(client.font, stacks, 32 - client.font.width(stacks), 24, 0xFFFFFFFF, true);
        graphics.pose().popPose();
        if (!secondsVisible) return;
        String seconds = StatText.number(Math.ceil(Math.max(0, buff.next(time) - time) / 2D) / 10D) + "s";
        graphics.drawString(client.font, seconds, x + (32 - client.font.width(seconds)) / 2, y + 33, retiring ? 0xFFE98A80 : 0xFFD5DDE4, true);
    }
    public static void preview(GuiGraphics graphics) {
        preview(graphics, ClientConfigs.BUFF_HUD_SECONDS.get());
    }
    public static void preview(GuiGraphics graphics, boolean seconds) {
        var choices = ClientPackets.modCatalog().creative().stream().filter(card -> card.level() == card.info().details().maxLevel()
                && card.info().details().conditional().stream().anyMatch(modifier -> modifier.condition().onKill())).limit(4).toList();
        int columns = ClientConfigs.BUFF_HUD_COLUMNS.get();
        for (int i = 0; i < 4; i++) {
            var card = choices.isEmpty() ? null : choices.get(i % choices.size());
            var modifier = card == null ? null : card.info().details().conditional().stream().filter(value -> value.condition().onKill()).findFirst().orElseThrow();
            int duration = modifier == null ? 200 : modifier.condition().durationTicks(), maximum = modifier == null ? 2 : modifier.condition().maxStacks();
            var buff = new WeaponBuff(new UUID(0, i), 0, card == null ? "yagens_attributes:mod" : card.id(), card == null ? 0 : card.level(),
                    modifier == null ? Stats.DAMAGE : modifier.stat(), modifier == null ? 0 : modifier.addition(), false, duration, maximum,
                    modifier == null ? ConditionalModifier.Decay.INDEPENDENT : modifier.condition().decay(), Collections.nCopies(Math.min(2, maximum), 7L * duration / 10));
            draw(graphics, buff, Math.min(2, maximum), .7F, 0, false, i % columns * 36, i / columns * (seconds ? 44 : 36), 0, seconds);
        }
    }
    public static List<Component> details(UUID currentWeapon, UUID mod) {
        if (currentWeapon == null || !currentWeapon.equals(weapon)) return List.of();
        var lines = new ArrayList<Component>();
        for (var cell : CELLS.values()) if (cell.retiring < 0 && cell.buff.mod().equals(mod) && cell.buff.count(now) > 0) {
            var buff = cell.buff; int count = buff.count(now);
            lines.add(Component.translatable("gui.yagens_attributes.buff_stacks", count, buff.maximum()));
            String amount = (buff.addition() >= 0 ? "+" : "") + StatText.number(buff.addition() * count * 100) + "%";
            lines.add(buff.perTargetStatus() ? Component.translatable("gui.yagens_attributes.per_status_type", amount, StatText.label(buff.stat()))
                    : Component.literal(amount + " ").append(StatText.label(buff.stat())));
            String remaining = buff.decay() == ConditionalModifier.Decay.INDEPENDENT ? String.join(" / ", buff.deadlines().stream().filter(value -> value > now)
                    .map(value -> StatText.number((value - now) / 20D) + " s").toList()) : StatText.number((buff.next(now) - now) / 20D) + " s";
            lines.add(Component.translatable("gui.yagens_attributes." + (buff.decay() == ConditionalModifier.Decay.INDEPENDENT ? "buff_layer_times" : "buff_next_expiry"), remaining));
        }
        return List.copyOf(lines);
    }
}
