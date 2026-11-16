package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.core.ui.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.text.ModText;
import java.util.*;
import java.util.stream.Stream;

public final class ModPanelScreen extends Screen {
    private static int nextRequestId;
    private final int view;
    private PanelPayload snapshot;
    private WeaponProgress progress = WeaponProgress.ZERO;
    private UUID selected;
    private int selectedSlot, slotPage, pendingId = -1, pendingAction, waitingTicks, debounce, detailsScroll, detailLineCount;
    private boolean queryDirty;
    private final ModGesture gesture = new ModGesture();
    private final ConflictFlash conflictFlash = new ConflictFlash();
    private List<PanelPayload.ModView> conflictingMods = List.of();
    private PanelPayload.ModView pendingInstall;
    private PanelPayload.ModView draggedMod;
    private double pointerX, pointerY;
    private int dragPageTicks;
    private String localError = "", searchValue = "";
    private ModInfo.Category category = ModInfo.Category.ALL;
    private ModInventory.Order order = ModInventory.Order.QUALITY;
    private ModFilter filter = ModFilter.NONE;
    private PanelButton filterButton;
    private EditBox search;
    private EditBox withdrawInput;
    private boolean withdrawalOpen, recycleMode, recycleDialog;
    private PanelButton recycleButton;
    private String withdrawalError = "";
    private PanelButton withdrawConfirm, withdrawCancel;
    private EndoBalanceButton endoButton, kuvaButton;
    private boolean withdrawKuva, cycleOpen, cycleUseNew;
    private PanelPayload.ModView hoveredMod;
    private PanelButton previous, next, refresh, sort, experienceToggle;
    private final Map<ModInfo.Category, PanelButton> categories = new LinkedHashMap<>();
    private List<Component> hoveredTooltip;
    private PanelPayload.ModView detailSource;
    private boolean detailExpanded;
    private List<FormattedCharSequence> detailLines = List.of();
    private List<Integer> hotbarSlots = List.of();
    private List<PanelGeometry.Rect> hotbarRectangles = List.of();
    private int requestedHotbarSlot = -1, inspectedHotbarSlot = -1, hotbarTitleWidth = -1;
    private boolean polarizing;
    private int polarizationSlot = -1;
    private ModInfo.Polarity polarizationChoice = ModInfo.Polarity.NONE;

    public ModPanelScreen(int view) { super(text(view == PanelRequest.STORAGE ? "mod_space" : "weapon_panel")); this.view = view; }
    private static Component text(String key, Object... args) { return Component.translatable("gui.yagens_attributes." + key, args); }
    private double scale() { return Math.min(width / (double) PanelGeometry.WIDTH, height / (double) PanelGeometry.HEIGHT); }
    private double originX() { return (width - PanelGeometry.WIDTH * scale()) / 2; }
    private double originY() { return (height - PanelGeometry.HEIGHT * scale()) / 2; }
    private double logicalX(double x) { return (x - originX()) / scale(); }
    private double logicalY(double y) { return (y - originY()) / scale(); }

    private PanelButton button(int x, int y, int width, int height, Component label, Runnable action) {
        return addRenderableWidget(new PanelButton(x, y, width, height, label, action));
    }

    @Override protected void init() {
        categories.clear();
        cancelGesture();
        detailSource = null; detailLines = List.of();
        updateWeaponStrip();
        if (view == PanelRequest.WEAPON) experienceToggle = button(146, 110, 70, 17, text("experience_off"), () -> request(page(), PanelRequest.TOGGLE_EXPERIENCE));
        button(PanelGeometry.WIDTH - 270, 39, 120, 20, text("mod_space"), () -> minecraft.setScreen(new ModPanelScreen(PanelRequest.STORAGE))).active = view != PanelRequest.STORAGE;
        button(PanelGeometry.WIDTH - 140, 39, 120, 20, text("weapon_panel"), () -> minecraft.setScreen(new ModPanelScreen(PanelRequest.WEAPON))).active = view != PanelRequest.WEAPON;
        int sx = view == PanelRequest.STORAGE ? 26 : 256, sy = view == PanelRequest.STORAGE ? 88 : 414;
        search = addRenderableWidget(new EditBox(font, sx, sy, 222, 14, text("search")));
        search.setBordered(false); search.setMaxLength(80); search.setTextColor(0xDCE6EA); search.setHint(text("search")); search.setValue(searchValue);
        search.setResponder(value -> { searchValue = value; queryDirty = true; debounce = 7; });
        sort = button(PanelGeometry.WIDTH - 260, sy - 6, 240, 24, sortLabel(), () -> { order = ModInventory.Order.values()[(order.ordinal() + 1) % ModInventory.Order.values().length]; sort.setMessage(sortLabel()); changedQuery(); });
        if (view == PanelRequest.STORAGE) filterButton = button(PanelGeometry.FILTER.x(), PanelGeometry.FILTER.y(), PanelGeometry.FILTER.width(), PanelGeometry.FILTER.height(), text("filter_open", filter.groups().size()), this::openFilters);
        if (view == PanelRequest.STORAGE) recycleButton = button(158, 566, 36, 28, Component.literal("\uE109").withStyle(net.minecraft.network.chat.Style.EMPTY.withFont(net.minecraft.resources.ResourceLocation.parse("yagens_attributes:mod_symbols"))), () -> { recycleMode = !recycleMode; recycleButton.selected(recycleMode); cancelGesture(); });
        if (view == PanelRequest.STORAGE) endoButton = addRenderableWidget(new EndoBalanceButton(this::openWithdrawal));
        if (view == PanelRequest.STORAGE) kuvaButton = addRenderableWidget(new EndoBalanceButton(PanelGeometry.KUVA, true, () -> openWithdrawal(true)));
        if (view == PanelRequest.STORAGE) {
            addCategory(ModInfo.Category.ALL, 20, 70); addCategory(ModInfo.Category.WARFRAME, 96, 88); addCategory(ModInfo.Category.WEAPON, 190, 76);
            if (ModInfo.Category.WEAPON.contains(category)) {
                addCategory(ModInfo.Category.MELEE, 282, 70); addCategory(ModInfo.Category.RANGED, 358, 70);
                if (ModInfo.Category.MELEE.contains(category)) {
                    addCategory(ModInfo.Category.SWORD, 446, 64); addCategory(ModInfo.Category.AXE, 516, 64);
                    addCategory(ModInfo.Category.TRIDENT, 586, 78); addCategory(ModInfo.Category.MACE, 670, 64);
                } else if (ModInfo.Category.RANGED.contains(category)) {
                    addCategory(ModInfo.Category.BOW, 446, 64); addCategory(ModInfo.Category.CROSSBOW, 516, 88);
                }
            }
        }
        int footer = PanelGeometry.HEIGHT - 30;
        previous = button(PanelGeometry.WIDTH - 350, footer, 28, 22, Component.literal("<"), () -> request(page() - 1, PanelRequest.REFRESH));
        next = button(PanelGeometry.WIDTH - 316, footer, 28, 22, Component.literal(">"), () -> request(page() + 1, PanelRequest.REFRESH));
        refresh = button(PanelGeometry.WIDTH - 172, footer, 72, 22, text("refresh"), () -> request(page(), PanelRequest.REFRESH));
        button(PanelGeometry.WIDTH - 92, footer, 72, 22, Component.translatable("gui.done"), this::onClose);
        var dialog = PanelGeometry.WITHDRAWAL;
        String previousInput = withdrawInput == null ? "" : withdrawInput.getValue();
        withdrawInput = addRenderableWidget(new EditBox(font, dialog.x() + 18, dialog.y() + 67, dialog.width() - 36, 20, text("endo_amount")));
        withdrawInput.setMaxLength(19); withdrawInput.setFilter(value -> value.chars().allMatch(c -> c >= '0' && c <= '9'));
        withdrawInput.setHint(text("endo_amount")); withdrawInput.setValue(previousInput);
        withdrawInput.setResponder(value -> { withdrawalError = ""; updateWithdrawal(); });
        withdrawConfirm = button(dialog.x() + 18, dialog.y() + 129, 132, 24, text("endo_withdraw"), this::submitWithdrawal);
        withdrawCancel = button(dialog.x() + 170, dialog.y() + 129, 132, 24, Component.translatable("gui.cancel"), this::closeWithdrawal);
        showWithdrawal(withdrawalOpen);
        updateButtons();
        if (snapshot == null && pendingId < 0) request(0, PanelRequest.REFRESH);
    }
    private Component sortLabel() { return text("sort", Component.translatable("order.yagens_attributes." + order.name().toLowerCase(Locale.ROOT))); }
    private void addCategory(ModInfo.Category value, int x, int width) {
        var button = button(x, 112, width, 24, Component.translatable("category.yagens_attributes." + value.key()), () -> {
            category = value; changedQuery(); rebuildWidgets();
        });
        button.selected(value == category || value != ModInfo.Category.ALL && value.contains(category)); categories.put(value, button);
    }
    private void changedQuery() { queryDirty = true; debounce = 0; }
    private void openFilters() { if (ready() && view == PanelRequest.STORAGE) { cancelGesture(); minecraft.setScreen(new ModFilterScreen(this, filter)); } }
    void closeFilters(ModFilter value) {
        filter = value; snapshot = null; selected = null; detailSource = null; queryDirty = false;
        minecraft.setScreen(this);
    }
    private int page() { return snapshot == null ? 0 : snapshot.page(); }
    private int maximumCapacity() { return snapshot == null ? 0 : yagen.waitmydawn.yagens_attributes.registries.Components.capacity(snapshot.weaponStack(), progress); }
    private UUID weaponId() { return snapshot == null ? null : snapshot.weaponId(); }
    private PanelPayload.ModView selectedMod() {
        if (cycleOpen && snapshot != null && !snapshot.cycleViews().isEmpty() && snapshot.cycleViews().get(0).id().equals(selected)) return snapshot.cycleViews().get(0);
        return snapshot == null ? null : Stream.concat(snapshot.mods().stream(), snapshot.installed().stream()).filter(mod -> mod.id().equals(selected)).findFirst().orElse(null);
    }
    private void request(int page, int action) {
        request(page, action, view == PanelRequest.WEAPON ? selectedSlot : -1);
    }
    private void request(int page, int action, int slot) {
        if (pendingId >= 0 || minecraft.getConnection() == null) return;
        pendingInstall = action == PanelRequest.INSTALL ? selectedMod() : null;
        cancelGesture();
        pendingId = nextRequestId = nextRequestId == Integer.MAX_VALUE ? 0 : nextRequestId + 1;
        waitingTicks = 0; localError = ""; queryDirty = false; pendingAction = action;
        requestedHotbarSlot = minecraft.player == null ? -1 : minecraft.player.getInventory().selected;
        var query = new ModInventory.Query(searchValue, category, order, filter);
        PacketDistributor.sendToServer(new PanelRequest(pendingId, view, Math.max(0, page), action, action == PanelRequest.RELEASE ? null : weaponId(), action == PanelRequest.POLARIZE || action == PanelRequest.TOGGLE_EXPERIENCE ? null : selected,
                slot, query, minecraft.options.languageCode,
                action == PanelRequest.WITHDRAW_ENDO || action == PanelRequest.WITHDRAW_KUVA || action == PanelRequest.RECYCLE ? EndoCurrency.parse(withdrawInput.getValue(), maximumWithdrawal()).orElse(0) : 0,
                action == PanelRequest.POLARIZE ? polarizationChoice : ModInfo.Polarity.NONE));
        updateButtons();
    }
    public void accept(PanelPayload payload) {
        if (payload.requestId() != pendingId || payload.view() != view) return;
        cancelGesture();
        snapshot = payload; progress = payload.progress(); pendingId = -1;
        if (pendingAction == PanelRequest.INSTALL && "gui.yagens_attributes.mod_conflict".equals(payload.error()) && pendingInstall != null) {
            var incoming = payload.mods().stream().filter(mod -> mod.id().equals(pendingInstall.id()) || mod.info().equals(pendingInstall.info())).findFirst().orElse(pendingInstall);
            rejectConflict(incoming);
        }
        pendingInstall = null;
        if (pendingAction == PanelRequest.POLARIZE && payload.error().isEmpty()) { polarizing = false; polarizationSlot = -1; selected = null; localError = snapshot.creative() ? "polarize_success_creative" : "polarize_success"; }
        inspectedHotbarSlot = requestedHotbarSlot;
        if ((pendingAction == PanelRequest.WITHDRAW_ENDO || pendingAction == PanelRequest.WITHDRAW_KUVA || pendingAction == PanelRequest.RECYCLE)) {
            withdrawalError = payload.error();
            if (payload.error().isEmpty()) closeWithdrawal();
        }
        slotPage = Math.min(slotPage, Math.max(0, (payload.slotCount() - 1) / 8));
        selectedSlot = Math.min(selectedSlot, Math.max(0, payload.slotCount() - 1));
        if (!cycleOpen && (view == PanelRequest.STORAGE ? payload.mods().stream().noneMatch(mod -> mod.id().equals(selected)) : selectedMod() == null)) selected = null;
        if (pendingAction == PanelRequest.CYCLE) cycleUseNew = false;
        updateButtons();
    }
    public void acceptProgress(ProgressPayload payload) {
        if (Objects.equals(weaponId(), payload.weapon())) { progress = payload.progress(); updateButtons(); }
    }
    private void updateButtons() {
        if (previous == null) return;
        if (filterButton != null) { filterButton.active = ready(); filterButton.selected(!filter.groups().isEmpty()); }
        if (recycleButton != null) { recycleButton.active = pendingId < 0 && snapshot != null && !withdrawalOpen && !cycleOpen; recycleButton.selected(recycleMode); }
        boolean ready = snapshot != null && pendingId < 0;
        previous.active = ready && page() > 0; next.active = ready && page() + 1 < snapshot.pages(); refresh.active = pendingId < 0;
        if (experienceToggle != null) {
            experienceToggle.visible = hasHeldWeapon() && !cycleOpen && !withdrawalOpen && polarizationSlot < 0;
            experienceToggle.active = ready && !polarizing;
            boolean enabled = snapshot != null && yagen.waitmydawn.yagens_attributes.registries.Components.absorbsExperience(snapshot.weaponStack());
            experienceToggle.setMessage(text(enabled ? "experience_on" : "experience_off")); experienceToggle.selected(enabled);
        }
        if (endoButton != null) { endoButton.balance(snapshot == null ? 0 : snapshot.endo()); endoButton.active = ready; }
        if (kuvaButton != null) { kuvaButton.balance(snapshot == null ? 0 : snapshot.kuva()); kuvaButton.active = ready && !cycleOpen; kuvaButton.visible = snapshot != null && (snapshot.kuva() > 0 || snapshot.hasRiven()); }
        updateWithdrawal();
    }
    private boolean owner(PanelPayload.ModView mod) { return mod != null && minecraft.player != null && mod.owner().equals(minecraft.player.getUUID()); }
    private boolean here(PanelPayload.ModView mod) { return mod != null && weaponId() != null && weaponId().equals(mod.weapon()); }
    private boolean ready() { return snapshot != null && pendingId < 0 && !withdrawalOpen && !cycleOpen && polarizationSlot < 0; }
    private boolean hasHeldWeapon() { return weaponId() != null && snapshot.profile() != null && !snapshot.weaponStack().isEmpty(); }
    private ModInfo.Polarity slotPolarity(int slot) { return snapshot == null || snapshot.profile() == null ? ModInfo.Polarity.NONE : PolarityRules.slot(snapshot.profile().slots(), slot); }
    private String slotSymbol(ModInfo.Polarity polarity) { return polarity == ModInfo.Polarity.NONE ? "" : ClientPackets.modCatalog().polaritySymbol(polarity); }
    private int cost(ModInfo info, int slot) { return PolarityRules.cost(info, slotPolarity(slot)); }
    private boolean removable(PanelPayload.ModView mod) { return ready() && here(mod) && (owner(mod) || mod.shared()); }
    private boolean releasable(PanelPayload.ModView mod) { return ready() && view == PanelRequest.STORAGE && owner(mod) && mod.weapon() != null; }
    private void release(PanelPayload.ModView mod) {
        if (!releasable(mod)) { localError = "release_denied"; return; }
        select(mod); request(page(), PanelRequest.RELEASE);
    }
    private PanelPayload.ModView inSlot(int slot) { return snapshot == null ? null : snapshot.installed().stream().filter(mod -> mod.slot() == slot).findFirst().orElse(null); }
    private boolean installable(PanelPayload.ModView mod, int slot) {
        return ready() && owner(mod) && mod.weapon() == null && snapshot.profile() != null && weaponId() != null && slot >= 0 && slot < snapshot.slotCount() && inSlot(slot) == null
                && mod.info().compatible(snapshot.profile().category()) && (snapshot.creative() || (long) snapshot.usedCapacity() + cost(mod.info(), slot) <= maximumCapacity())
                && snapshot.installed().stream().noneMatch(other -> mod.info().conflicts(other.info(), snapshot.profile().category()));
    }
    private int firstSlot() {
        if (snapshot == null) return -1;
        if (view == PanelRequest.WEAPON && selectedSlot < snapshot.slotCount() && inSlot(selectedSlot) == null) return selectedSlot;
        for (int i = 0; i < snapshot.slotCount(); i++) if (inSlot(i) == null) return i;
        return -1;
    }
    private boolean withdrawable(PanelPayload.ModView mod) { return ready() && owner(mod) && mod.weapon() == null && mod.info().defined(); }
    private boolean upgradeable(PanelPayload.ModView mod) {
        if (!(ready() && mod != null && mod.info().defined() && (owner(mod) || removable(mod)) && (mod.weapon() == null || here(mod)) && mod.info().level() < mod.info().details().maxLevel() && (snapshot.creative() || snapshot.endo() >= mod.info().details().upgradeCost()))) return false;
        if (snapshot.creative() || mod.weapon() == null) return true;
        var next = ClientPackets.modCatalog().card(mod.info(), mod.info().level() + 1);
        return snapshot.usedCapacity() - cost(mod.info(), mod.slot()) + cost(next.info(), mod.slot()) <= maximumCapacity();
    }
    private void cancelGesture() { gesture.cancel(); draggedMod = null; dragPageTicks = 0; }
    private void select(PanelPayload.ModView mod) {
        if (!Objects.equals(selected, mod.id())) detailsScroll = 0;
        selected = mod.id(); search.setFocused(false); setFocused(null); updateButtons();
    }
    private void remove(PanelPayload.ModView mod) {
        if (!removable(mod)) { localError = "gesture_denied"; return; }
        select(mod); request(page(), owner(mod) ? PanelRequest.UNINSTALL : PanelRequest.TAKE);
    }
    private void quick(PanelPayload.ModView mod) {
        if (releasable(mod)) release(mod);
        else if (here(mod)) remove(mod);
        else if (rejectConflict(mod)) return;
        else if (installable(mod, firstSlot())) { select(mod); request(page(), PanelRequest.INSTALL, firstSlot()); }
        else localError = "gesture_denied";
    }
    private boolean rejectConflict(PanelPayload.ModView incoming) {
        if (incoming == null || snapshot == null || snapshot.profile() == null || weaponId() == null || !owner(incoming) || incoming.weapon() != null
                || !incoming.info().compatible(snapshot.profile().category())) return false;
        var blocked = snapshot.installed().stream().filter(mod -> incoming.info().conflicts(mod.info(), snapshot.profile().category())).toList();
        if (blocked.isEmpty()) return false;
        var participants = new ArrayList<PanelPayload.ModView>(); participants.add(incoming); participants.addAll(blocked);
        conflictingMods = List.copyOf(participants);
        conflictFlash.start(weaponId(), participants.stream().map(PanelPayload.ModView::id).toList(), net.minecraft.Util.getMillis());
        if (view == PanelRequest.WEAPON) slotPage = blocked.get(0).slot() / 8;
        localError = "mod_conflict";
        return true;
    }
    private void renderConflict(PanelCanvas canvas, PanelGeometry.Rect rectangle, UUID mod) {
        if (conflictFlash.red(weaponId(), mod, net.minecraft.Util.getMillis()))
            PanelPainter.frame(canvas, rectangle, 0x65D92636, 0xFFFF5669, 8);
    }
    private void renderConflictSummary(PanelCanvas canvas, int mx, int my) {
        if (!conflictFlash.active(weaponId(), net.minecraft.Util.getMillis()) || conflictingMods.size() < 2) return;
        var bounds = new PanelGeometry.Rect(346, 480, 408, 58);
        PanelPainter.frame(canvas, bounds, 0xF00C151E, 0xFF8E4652, 6);
        for (int i = 0; i < 2; i++) {
            var mod = conflictingMods.get(i); int x = bounds.x() + 8 + 200 * i;
            var badge = new PanelGeometry.Rect(x, bounds.y() + 6, 192, 46);
            renderTexture(canvas, new PanelGeometry.Rect(x + 6, bounds.y() + 12, 32, 32), mod);
            canvas.text(ModText.name(mod.info(), mod.name()).getString(), x + 44, bounds.y() + 23, 10, mod.info().color() | 0xFF000000, 142);
            renderConflict(canvas, badge, mod.id());
        }
        if (conflictingMods.size() > 2) canvas.text("+" + (conflictingMods.size() - 2), bounds.x() + bounds.width() - 23, bounds.y() + 43, 8, 0xFFFF5669, 20);
        if (bounds.contains(mx, my)) hoveredTooltip = conflictingMods.stream().map(mod -> ModText.name(mod.info(), mod.name())).toList();
    }
    private void updateWeaponStrip() {
        if (view != PanelRequest.WEAPON || minecraft.player == null) return;
        var slots = HotbarWeapons.slots(minecraft.player.getInventory().items);
        int titleWidth = Math.round(font.width(title) * 20F / 9F);
        if (!slots.equals(hotbarSlots) || hotbarTitleWidth != titleWidth) {
            hotbarSlots = slots; hotbarTitleWidth = titleWidth; hotbarRectangles = PanelGeometry.weaponStrip(titleWidth, slots.size());
        }
    }
    private void renderWeaponStrip(PanelCanvas canvas, int mx, int my) {
        if (minecraft.player == null) return;
        for (int i = 0; i < hotbarSlots.size(); i++) {
            int slot = hotbarSlots.get(i); var r = hotbarRectangles.get(i);
            boolean hovered = r.contains(mx, my), current = minecraft.player.getInventory().selected == slot;
            PanelPainter.frame(canvas, r, current || hovered ? 0x90152630 : 0x40101A22, current ? PanelPainter.TEAL : hovered ? PanelPainter.GOLD : 0x50526975, 4);
            var graphics = canvas.graphics(); graphics.pose().pushPose(); graphics.pose().translate(r.x() + 2, r.y() + 2, 0); graphics.pose().scale(2, 2, 1);
            graphics.renderItem(minecraft.player.getInventory().getItem(slot), 0, 0); graphics.pose().popPose();
            if (hovered) hoveredTooltip = List.of(minecraft.player.getInventory().getItem(slot).getHoverName(), text("hotbar_weapon", slot + 1));
        }
    }
    private void switchWeapon(int slot) {
        if (!ready() || !HotbarWeapons.select(minecraft, slot)) return;
        cancelGesture(); polarizing = false; polarizationSlot = -1; selected = null; selectedSlot = 0; slotPage = 0; detailsScroll = 0;
        detailSource = null; detailLines = List.of(); snapshot = null; progress = WeaponProgress.ZERO;
        request(0, PanelRequest.REFRESH);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xEF04090E); hoveredTooltip = null; hoveredMod = null;
        int mx = (int) logicalX(mouseX), my = (int) logicalY(mouseY);
        graphics.pose().pushPose(); graphics.pose().translate(originX(), originY(), 0); graphics.pose().scale((float) scale(), (float) scale(), 1);
        var canvas = new PanelCanvas(graphics, font);
        String subtitle = snapshot == null ? text("loading").getString() : snapshot.weaponName().getString();
        PanelPainter.background(canvas, title.getString(), subtitle);
        if (view == PanelRequest.WEAPON) renderWeaponStrip(canvas, mx, my);
        int searchX = view == PanelRequest.STORAGE ? 20 : 250, searchY = view == PanelRequest.STORAGE ? 82 : 408;
        PanelPainter.frame(canvas, new PanelGeometry.Rect(searchX, searchY, 236, 24), 0xE0172833, 0xFF4C7583, 4);
        if (view == PanelRequest.STORAGE) renderCards(canvas, PanelGeometry.storage(), mx, my);
        else { renderStats(canvas, mx, my); renderSlots(canvas, mx, my); renderCards(canvas, PanelGeometry.compatible(), mx, my); renderDetails(canvas, mx, my); }
        renderActions(canvas, mx, my);
        for (var renderable : renderables) if (renderable != withdrawInput && renderable != withdrawConfirm && renderable != withdrawCancel) renderable.render(graphics, mx, my, partialTick);
        if (view == PanelRequest.STORAGE && PanelGeometry.ENDO.contains(mx, my)) hoveredTooltip = List.of(text("endo_balance", snapshot == null ? 0 : snapshot.endo()), text("endo_click_withdraw"));
        if (view == PanelRequest.STORAGE && kuvaButton != null && kuvaButton.visible && PanelGeometry.KUVA.contains(mx, my)) hoveredTooltip = List.of(text("kuva_balance", snapshot.kuva()), text("kuva_click_withdraw"), text("cycle_drop_hint"));
        String status = !localError.isEmpty() ? text(localError).getString() : snapshot == null ? text("loading").getString() : snapshot.error();
        if (!status.isEmpty()) canvas.text(Component.translatable(status).getString(), 24, PanelGeometry.HEIGHT - 56, 9, 0xFFFF9A8D, 740);
        else if (pendingId >= 0 || queryDirty) canvas.text(text("loading").getString(), 24, PanelGeometry.HEIGHT - 56, 9, PanelPainter.MUTED, 500);
        else if (snapshot != null) canvas.text(text("inventory_count", snapshot.total(), snapshot.copies()).getString(), 24, PanelGeometry.HEIGHT - 56, 9, PanelPainter.MUTED, 650);
        if (snapshot != null) canvas.text(text("page", snapshot.page() + 1, snapshot.pages()).getString(), PanelGeometry.WIDTH - 274, PanelGeometry.HEIGHT - 24, 10, PanelPainter.MUTED, 95);
        if (view == PanelRequest.STORAGE && PanelGeometry.RECYCLE.contains(mx, my)) hoveredTooltip = List.of(text("recycle_hint"));
        if (view == PanelRequest.STORAGE && PanelGeometry.RELEASE.contains(mx, my)) hoveredTooltip = List.of(text("release_label"), text("release_hint"));
        if (view == PanelRequest.STORAGE && PanelGeometry.FILTER.contains(mx, my)) hoveredTooltip = List.of(text("filter_explanation"));
        if (gesture.dragging()) renderDragged(canvas, mx, my);
        renderConflictSummary(canvas, mx, my);
        // Modal surfaces must cover the elevated text plane used by EditBox.
        graphics.pose().pushPose(); graphics.pose().translate(0, 0, 400);
        if (withdrawalOpen) renderWithdrawal(canvas, mx, my, partialTick);
        if (polarizationSlot >= 0) renderPolarization(canvas, mx, my);
        if (cycleOpen) renderCycle(canvas, mx, my);
        graphics.pose().popPose();
        graphics.pose().popPose();
        if (hoveredTooltip != null) graphics.renderComponentTooltip(font, hoveredTooltip, mouseX, mouseY);
    }

    private void renderCards(PanelCanvas canvas, List<PanelGeometry.Rect> rectangles, int mx, int my) {
        if (snapshot == null) return;
        if (snapshot.mods().isEmpty()) canvas.text(text("empty_filtered").getString(), rectangles.get(0).x() + 8, rectangles.get(0).y() + 18, 13, PanelPainter.MUTED, 650);
        for (int i = 0; i < Math.min(snapshot.mods().size(), rectangles.size()); i++) {
            var mod = snapshot.mods().get(i); var rectangle = rectangles.get(i);
            if (rectangle.contains(mx, my)) hoveredMod = mod;
            PanelPainter.card(canvas, rectangle, card(mod));
            renderArt(canvas, rectangle, mod);
            renderConflict(canvas, rectangle, mod.id());
            if (rectangle.contains(mx, my)) hoveredTooltip = tooltip(mod);
        }
    }
    private PanelPainter.Card card(PanelPayload.ModView mod) {
        String category = Component.translatable("category.yagens_attributes." + mod.info().category().key()).getString();
        int capacity = here(mod) ? cost(mod.info(), mod.slot()) : mod.info().capacityCost();
        int capacityColor = capacity < mod.info().capacityCost() ? 0xFF79DBA7 : capacity > mod.info().capacityCost() ? 0xFFE37F87 : 0;
        return new PanelPainter.Card(ModText.name(mod.info(), mod.name()).getString(), category, mod.info().polaritySymbol(), mod.quantity(), mod.info().color(), mod.id().equals(selected), mod.weapon() != null, capacity,
                ModText.rank(mod.info()).getString(), ModText.maxLevel(mod.info()), capacityColor);
    }
    private void renderArt(PanelCanvas canvas, PanelGeometry.Rect rectangle, PanelPayload.ModView mod) {
        renderTexture(canvas, PanelGeometry.artwork(rectangle), mod);
    }
    private void renderTexture(PanelCanvas canvas, PanelGeometry.Rect art, PanelPayload.ModView mod) {
        String value = mod.info().details() == null ? "yagens_attributes:item/mod" : mod.info().details().texture();
        if (mod.info().defined()) value = mod.info().style().texture(mod.info().details().type(), value);
        var id = net.minecraft.resources.ResourceLocation.parse(value);
        var sprite = minecraft.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(id);
        canvas.graphics().blit(art.x(), art.y(), 0, art.width(), art.height(), sprite);
    }
    private List<Component> tooltip(PanelPayload.ModView mod) {
        var lines = new ArrayList<>(ModText.tooltip(mod.info(), mod.name(), mod.bonuses(), mod.shared(), mod.weapon(), hasShiftDown()));
        if (here(mod) && cost(mod.info(), mod.slot()) != mod.info().capacityCost()) lines.add(text("slot_cost", cost(mod.info(), mod.slot())));
        if (view == PanelRequest.STORAGE && owner(mod) && mod.weapon() != null) lines.add(text("release_card_hint"));
        return List.copyOf(lines);
    }
    private void renderSlots(PanelCanvas canvas, int mx, int my) {
        int count = snapshot == null ? 8 : snapshot.slotCount();
        int used = snapshot == null ? 0 : snapshot.usedCapacity();
        PanelPainter.capacity(canvas, used, maximumCapacity(), text("capacity", used, maximumCapacity()).getString());
        if (count > 8) canvas.text("<  " + (slotPage + 1) + " / " + ((count + 7) / 8) + "  >", 674, 76, 10, PanelPainter.TEAL, 112);
        var rectangles = PanelGeometry.slots();
        for (int i = 0; i < 8; i++) {
            int slot = slotPage * 8 + i; if (slot >= count) break;
            var rectangle = rectangles.get(i);
            var installed = snapshot == null ? null : snapshot.installed().stream().filter(mod -> mod.slot() == slot).findFirst().orElse(null);
            ModInfo.Polarity polarity = snapshot != null && snapshot.profile() != null && slot < snapshot.profile().slots().size() ? snapshot.profile().slots().get(slot) : ModInfo.Polarity.NONE;
            if (installed != null) {
                PanelPainter.card(canvas, rectangle, card(installed));
                renderArt(canvas, rectangle, installed);
                int color = polarity == ModInfo.Polarity.NONE || installed.info().polarity() == ModInfo.Polarity.NONE ? PanelPainter.MUTED : polarity.matches(installed.info().polarity()) ? 0xFF79DBA7 : 0xFFE37F87;
                canvas.text((slot + 1) + (polarity == ModInfo.Polarity.NONE ? "" : " : " + slotSymbol(polarity)), rectangle.x() + 9, rectangle.y() + 9, 10, color, 76);
                renderConflict(canvas, rectangle, installed.id());
            }
            else PanelPainter.emptySlot(canvas, rectangle, text("slot_number", slot + 1).getString(), slotSymbol(polarity), selectedSlot == slot);
            if (gesture.dragging() && rectangle.contains(mx, my)) {
                boolean valid = canDrop(gesture.drop(slot, false, false, false), slot);
                PanelPainter.frame(canvas, rectangle, valid ? 0x204CBF95 : 0x20C65D69, valid ? 0xFF79DBA7 : 0xFFE37F87, 9);
            }
            if (rectangle.contains(mx, my)) hoveredTooltip = installed != null ? tooltip(installed) : polarity == ModInfo.Polarity.NONE
                    ? List.of(text("slot_number", slot + 1)) : List.of(text("slot_number", slot + 1), Component.literal(slotSymbol(polarity)));
            if (polarizing) {
                boolean target = rectangle.contains(mx, my) || selectedSlot == slot;
                PanelPainter.frame(canvas, rectangle, target ? 0x305ABEC6 : 0x105ABEC6, target ? PanelPainter.GOLD : PanelPainter.TEAL, 9);
                if (rectangle.contains(mx, my)) hoveredTooltip = List.of(text("polarize_select_slot", slot + 1));
            }
        }
        canvas.text(text("compatible_mods").getString(), 250, 392, 11, PanelPainter.GOLD, 400);
    }
    private void renderStats(PanelCanvas canvas, int mx, int my) {
        PanelPainter.frame(canvas, new PanelGeometry.Rect(20, 80, 208, PanelGeometry.HEIGHT - 139), 0xD00C151E, 0xFF445F6A, 7);
        canvas.text(text("weapon_statistics").getString(), 32, 93, 14, PanelPainter.GOLD, 184);
        if (snapshot == null || snapshot.profile() == null) { canvas.text(text("no_profile").getString(), 32, 128, 11, PanelPainter.MUTED, 180); return; }
        canvas.text(text("weapon_level", progress.level()).getString(), 32, 115, 10, PanelPainter.TEXT, 184);
        canvas.fill(32, 130, 184, 5, 0xFF263943); canvas.fill(32, 130, Math.round(184 * progress.fraction()), 5, PanelPainter.TEAL);
        canvas.text(text(progress.level() == WeaponProgress.MAX_LEVEL ? "max_level" : "weapon_experience", progress.experience(), progress.nextLevelExperience()).getString(), 32, 140, 8, PanelPainter.MUTED, 184);
        if (experienceToggle != null && experienceToggle.visible && mx >= 146 && mx < 216 && my >= 110 && my < 127) hoveredTooltip = List.of(text("experience_absorption_help"));
        var keys = StatText.keys(snapshot.profile());
        if (snapshot.hasWeaponRiven()) {
            canvas.text(text("riven_disposition_label").getString(), 32, 152, 9, 0xFFB169F5, 118);
            PanelPainter.disposition(canvas, RivenRules.stars(snapshot.profile().rivenDisposition()));
            if (PanelGeometry.DISPOSITION.contains(mx, my)) hoveredTooltip = List.of(text("riven_disposition", StatText.number(snapshot.profile().rivenDisposition())));
        }
        int y = 164;
        for (String key : keys) { statLine(canvas, StatText.label(key).getString(), StatText.value(key, snapshot.resolved().getOrDefault(key, 0D)), y, PanelPainter.TEXT); y += 14; }
        canvas.fill(32, y + 1, 184, 1, 0x506A8791); y += 14;
        canvas.text(text("composition").getString(), 32, y, 12, PanelPainter.GOLD, 180); y += 22;
        double total = 0;
        for (DamageType type : DamageType.DISPLAY_ORDER) {
            double amount = snapshot.resolved().getOrDefault("damage." + type.key(), 0D); total += amount;
            if (amount <= 0) continue;
            double base = snapshot.profile().baseDamage() * snapshot.profile().distribution().getOrDefault(type, 0D);
            int color = amount > base + 1e-6 ? 0xFF8BCCA7 : amount < base - 1e-6 ? 0xFFE89A89 : PanelPainter.TEXT;
            canvas.component(StatText.damageLabel(type), 32, y, 10, PanelPainter.MUTED, 123);
            canvas.text(StatText.number(amount), 162, y, 10, color, 54); y += 12;
        }
        y = Math.min(444, y + 13); statLine(canvas, text("total_damage").getString(), StatText.number(total), y, PanelPainter.GOLD);
    }
    private void statLine(PanelCanvas canvas, String label, String value, int y, int color) { canvas.text(label, 32, y, 10, PanelPainter.MUTED, 123); canvas.text(value, 162, y, 10, color, 54); }

    private void renderDetails(PanelCanvas canvas, int mx, int my) {
        var r = PanelGeometry.DETAILS;
        PanelPainter.frame(canvas, r, 0xD00C151E, 0xFF445F6A, 7);
        canvas.text(text("mod_details").getString(), r.x() + 12, r.y() + 12, 12, PanelPainter.GOLD, r.width() - 24);
        var mod = selectedMod();
        if (mod == null) {
            detailLineCount = 0; detailSource = null; detailLines = List.of();
            canvas.text(text("select_mod_details").getString(), r.x() + 12, r.y() + 40, 10, PanelPainter.MUTED, r.width() - 24);
        } else {
            float size = 10F / 9F;
            if (!mod.equals(detailSource) || detailExpanded != hasShiftDown()) {
                var information = new ArrayList<>(tooltip(mod));
                if (hasShiftDown()) information.add(text(mod.owner().equals(minecraft.player.getUUID()) ? "owner_self" : "owner_other"));
                int singleLines = 1 + ModText.properties(mod.info(), mod.bonuses()).size();
                detailLines = java.util.stream.IntStream.range(0, information.size()).boxed().flatMap(index -> index < singleLines
                        ? Stream.of(information.get(index).getVisualOrderText())
                        : font.split(information.get(index), (int) ((r.width() - 24) / size)).stream()).toList();
                detailSource = mod; detailExpanded = hasShiftDown();
            }
            var lines = detailLines;
            detailLineCount = lines.size(); detailsScroll = Math.min(detailsScroll, Math.max(0, detailLineCount - 14));
            canvas.graphics().pose().pushPose(); canvas.graphics().pose().translate(r.x() + 12, r.y() + 32, 0); canvas.graphics().pose().scale(size, size, 1);
            for (int i = 0; i < Math.min(14, lines.size() - detailsScroll); i++) {
                var line = lines.get(i + detailsScroll);
                float fit = Math.min(1, (r.width() - 24) / size / Math.max(1, font.width(line)));
                canvas.graphics().pose().pushPose(); canvas.graphics().pose().translate(0, Math.round(i * 12 / size), 0); canvas.graphics().pose().scale(fit, fit, 1);
                float x = i + detailsScroll == 0 ? ((r.width() - 24) / size / fit - font.width(line)) / 2 : 0;
                canvas.graphics().drawString(font, line, Math.round(x), 0, PanelPainter.TEXT, false); canvas.graphics().pose().popPose();
            }
            canvas.graphics().pose().popPose();
            if (lines.size() > 14) canvas.text(text("details_scroll").getString(), r.x() + 12, 318, 8, PanelPainter.MUTED, r.width() - 24);
        }
    }

    private PanelGeometry.Rect upgradeRect() { return view == PanelRequest.STORAGE ? PanelGeometry.STORAGE_UPGRADE : PanelGeometry.DETAIL_UPGRADE; }
    private long upgradeCost(PanelPayload.ModView mod) { return snapshot != null && snapshot.creative() ? 0 : mod.info().details().upgradeCost(); }
    private void renderActions(PanelCanvas canvas, int mx, int my) {
        var mod = selectedMod();
        if (view == PanelRequest.STORAGE) PanelPainter.icon(canvas, PanelGeometry.RELEASE, PanelPainter.Icon.RELEASE, releasable(gesture.dragging() ? draggedMod : mod), PanelGeometry.RELEASE.contains(mx, my));
        PanelPainter.icon(canvas, PanelGeometry.PHYSICAL_MOD, PanelPainter.Icon.INVENTORY, withdrawable(mod), PanelGeometry.PHYSICAL_MOD.contains(mx, my));
        PanelPainter.icon(canvas, PanelGeometry.PERMISSION, mod != null && mod.shared() ? PanelPainter.Icon.SHARED : PanelPainter.Icon.PRIVATE, ready() && owner(mod) && here(mod), PanelGeometry.PERMISSION.contains(mx, my));
        PanelPainter.icon(canvas, upgradeRect(), PanelPainter.Icon.UPGRADE, upgradeable(mod), upgradeRect().contains(mx, my));
        if (mod != null && mod.info().defined()) canvas.text(text("upgrade_cost", upgradeCost(mod)).getString(), view == PanelRequest.STORAGE ? 250 : upgradeRect().x() + 44, upgradeRect().y() + 6, 9, PanelPainter.GOLD, view == PanelRequest.STORAGE ? 136 : 210);
        canvas.text(text("gesture_hint").getString(), 400, PanelGeometry.HEIGHT - 24, 9, PanelPainter.MUTED, 330);
        if (view == PanelRequest.STORAGE && hasHeldWeapon()) {
            var r = PanelGeometry.HELD_WEAPON;
            PanelPainter.frame(canvas, r, 0xB0111D26, PanelPainter.MUTED, 4);
            canvas.graphics().renderItem(snapshot.weaponStack(), r.x() + 16, r.y() + 8);
            if (r.contains(mx, my)) hoveredTooltip = List.of(text("gesture_held_weapon"));
        }
        if (PanelGeometry.PHYSICAL_MOD.contains(mx, my)) hoveredTooltip = List.of(text("gesture_inventory"));
        if (PanelGeometry.PERMISSION.contains(mx, my)) hoveredTooltip = List.of(text(mod != null && mod.shared() ? "make_private" : "share"));
        if (upgradeRect().contains(mx, my)) hoveredTooltip = List.of(mod != null && mod.info().defined() ? text("upgrade_endo", upgradeCost(mod)) : text("upgrade"));
        if (view == PanelRequest.WEAPON) {
            var kuva = PanelGeometry.WEAPON_KUVA;
            PanelPainter.frame(canvas, kuva, 0xD0111D26, canCycle(mod) ? 0xFFBB88E8 : PanelPainter.MUTED, 4);
            canvas.graphics().renderItem(new net.minecraft.world.item.ItemStack(yagen.waitmydawn.yagens_attributes.registries.ModItems.KUVA.get()), kuva.x() + 10, kuva.y() + 6);
            if (snapshot != null && (snapshot.kuva() > 0 || snapshot.hasRiven())) canvas.text(String.valueOf(snapshot.kuva()), kuva.x() + kuva.width() + 8, kuva.y() + 9, 10, PanelPainter.GOLD, 80);
            if (kuva.contains(mx, my)) hoveredTooltip = List.of(text("cycle_drop_hint"));
            var r = PanelGeometry.POLARIZE;
            PanelPainter.frame(canvas, r, 0xD0111D26, polarizing ? PanelPainter.GOLD : canPolarize() ? PanelPainter.TEAL : PanelPainter.MUTED, 4);
            canvas.graphics().renderItem(new net.minecraft.world.item.ItemStack(yagen.waitmydawn.yagens_attributes.registries.ModItems.FORMA.get()), r.x() + 10, r.y() + 6);
            canvas.text(snapshot != null && snapshot.creative() ? text("creative_free").getString() : "×" + formaCount(), r.x() + r.width() + 8, r.y() + 9, 10, PanelPainter.GOLD, 90);
            if (r.contains(mx, my)) hoveredTooltip = List.of(text("polarize_action"), text(canPolarize() ? snapshot.creative() ? "polarize_hint_creative" : "polarize_hint" : polarizationFailure()));
        }
    }
    private int slotAt(double x, double y) {
        if (view != PanelRequest.WEAPON || snapshot == null) return -1;
        for (int i = 0; i < PanelGeometry.slots().size(); i++) if (PanelGeometry.slots().get(i).contains(x, y) && slotPage * 8 + i < snapshot.slotCount()) return slotPage * 8 + i;
        return -1;
    }
    private ModGesture.Action dropAt(double x, double y) {
        boolean pool = view == PanelRequest.WEAPON ? PanelGeometry.MOD_POOL.contains(x, y) : x >= 20 && x < 1080 && y >= 148 && y < 540;
        return gesture.drop(slotAt(x, y), pool, PanelGeometry.PHYSICAL_MOD.contains(x, y), view == PanelRequest.STORAGE && hasHeldWeapon() && PanelGeometry.HELD_WEAPON.contains(x, y));
    }
    private boolean canDrop(ModGesture.Action action, int slot) {
        var mod = draggedMod;
        return switch (action) {
            case INSTALL -> installable(mod, slot < 0 ? firstSlot() : slot);
            case MOVE -> movable(mod, slot);
            case REMOVE -> removable(mod);
            case WITHDRAW_ITEM -> withdrawable(mod);
            default -> false;
        };
    }
    private boolean movable(PanelPayload.ModView mod, int slot) {
        if (!removable(mod) || slot < 0 || slot >= snapshot.slotCount()) return false;
        var target = inSlot(slot);
        if (target != null && !removable(target)) return false;
        int next = PolarityRules.movedCapacity(snapshot.usedCapacity(), mod.info(), mod.slot(), target == null ? null : target.info(), slot, snapshot.profile().slots());
        return snapshot.creative() || next <= maximumCapacity() || next <= snapshot.usedCapacity();
    }
    private PanelGeometry.Rect kuvaTarget() { return view == PanelRequest.STORAGE ? PanelGeometry.KUVA : PanelGeometry.WEAPON_KUVA; }
    private boolean canCycle(PanelPayload.ModView mod) {
        return mod != null && mod.info().riven() != null && (owner(mod) || here(mod) && mod.shared()) && (mod.weapon() == null || here(mod));
    }
    private void openCycle(PanelPayload.ModView mod) {
        if (!ready() || !canCycle(mod)) { localError = "cycle_denied"; return; }
        select(mod); cancelGesture(); cycleOpen = true; cycleUseNew = false; search.setFocused(false); setFocused(null);
        request(page(), PanelRequest.OPEN_CYCLE);
    }
    private void submitCycle() {
        if (pendingId >= 0 || snapshot == null || snapshot.cycleViews().isEmpty()) return;
        if (snapshot.pendingCycle()) request(page(), cycleUseNew && snapshot.cycleViews().size() == 2 ? PanelRequest.KEEP_NEW : PanelRequest.KEEP_OLD);
        else if (snapshot.kuva() >= snapshot.cycleViews().get(0).info().riven().cycleCost()) request(page(), PanelRequest.CYCLE);
    }
    private void openCycleRules() {
        if (pendingId < 0 && snapshot != null && !snapshot.cycleViews().isEmpty())
            minecraft.setScreen(new RivenRulesScreen(this, snapshot.cycleViews().get(0).info()));
    }
    void resumeCycle() { if (cycleOpen) request(page(), PanelRequest.OPEN_CYCLE); }
    private void cycleCard(PanelCanvas canvas, PanelGeometry.Rect bounds, PanelPayload.ModView mod, boolean selected, boolean candidate) {
        PanelPainter.frame(canvas, bounds, 0xFF17212D, selected ? PanelPainter.GOLD : 0xFF8051A6, 8);
        canvas.centeredText(text(candidate ? "cycle_new" : "cycle_old").getString(), bounds.x() + bounds.width() / 2, bounds.y() + 12, 12, PanelPainter.GOLD, bounds.width() - 20);
        if (mod == null) { canvas.centeredText(text("cycle_preview").getString(), bounds.x() + bounds.width() / 2, bounds.y() + 100, 10, PanelPainter.MUTED, bounds.width() - 36); return; }
        renderTexture(canvas, new PanelGeometry.Rect(bounds.x() + bounds.width() / 2 - 32, bounds.y() + 35, 64, 64), mod);
        canvas.centeredText(ModText.name(mod.info(), mod.name()).getString(), bounds.x() + bounds.width() / 2, bounds.y() + 110, 12, 0xFFBB88E8, bounds.width() - 28);
        var lines = ModText.tooltip(mod.info(), mod.name(), mod.bonuses(), mod.shared(), mod.weapon(), false);
        int y = bounds.y() + 138;
        for (int i = 1; i < lines.size(); i++) { canvas.centeredComponent(lines.get(i), bounds.x() + bounds.width() / 2, y, 10, PanelPainter.TEXT, bounds.width() - 36); y += 19; }
    }
    private void renderCycle(PanelCanvas canvas, int mx, int my) {
        hoveredTooltip = null;
        canvas.fill(0, 0, PanelGeometry.WIDTH, PanelGeometry.HEIGHT, 0xDD04090E);
        PanelPainter.frame(canvas, PanelGeometry.CYCLE_DIALOG, 0xFF0D1722, 0xFF8051A6, 9);
        canvas.centeredText(text("cycle_title").getString(), 550, 113, 16, 0xFFBB88E8, 600);
        var views = snapshot == null ? List.<PanelPayload.ModView>of() : snapshot.cycleViews();
        var help = PanelGeometry.CYCLE_HELP;
        boolean helpEnabled = pendingId < 0 && !views.isEmpty();
        PanelPainter.frame(canvas, help, 0xFF17232E, helpEnabled ? help.contains(mx, my) ? PanelPainter.GOLD : PanelPainter.TEAL : PanelPainter.MUTED, 4);
        canvas.centeredText("?", help.x() + help.width() / 2, help.y() + 5, 14, helpEnabled ? PanelPainter.TEXT : PanelPainter.MUTED, 18);
        if (help.contains(mx, my)) hoveredTooltip = List.of(text("rules_open"));
        cycleCard(canvas, PanelGeometry.CYCLE_OLD, views.isEmpty() ? null : views.get(0), !cycleUseNew, false);
        cycleCard(canvas, PanelGeometry.CYCLE_NEW, views.size() < 2 ? null : views.get(1), cycleUseNew && views.size() == 2, true);
        String message = snapshot == null ? text("loading").getString() : !snapshot.error().isEmpty() ? Component.translatable(snapshot.error()).getString() : snapshot.pendingCycle() ? text("cycle_choose").getString() : text("cycle_cost", views.isEmpty() ? 0 : views.get(0).info().riven().cycleCost(), snapshot.kuva()).getString();
        canvas.centeredText(message, 550, 440, 10, PanelPainter.TEXT, 600);
        boolean enabled = pendingId < 0 && !views.isEmpty() && (snapshot.pendingCycle() || snapshot.kuva() >= views.get(0).info().riven().cycleCost());
        PanelPainter.frame(canvas, PanelGeometry.CYCLE_ROLL, 0xFF253044, enabled ? 0xFFBB88E8 : PanelPainter.MUTED, 4);
        PanelPainter.frame(canvas, PanelGeometry.CYCLE_CLOSE, 0xFF17232E, PanelPainter.TEAL, 4);
        canvas.centeredText(text(pendingId >= 0 ? "loading" : snapshot != null && snapshot.pendingCycle() ? "cycle_confirm" : "cycle_action").getString(), 440, 470, 11, enabled ? PanelPainter.GOLD : PanelPainter.MUTED, 180);
        canvas.centeredText(Component.translatable("gui.back").getString(), 660, 470, 11, PanelPainter.TEXT, 180);
    }
    private int formaCount() {
        if (minecraft.player == null) return 0;
        int count = 0;
        for (int i = 0; i < minecraft.player.getInventory().getContainerSize(); i++) {
            var item = minecraft.player.getInventory().getItem(i);
            if (item.is(yagen.waitmydawn.yagens_attributes.registries.ModItems.FORMA.get())) count += item.getCount();
        }
        return count;
    }
    private String polarizationFailure() {
        if (snapshot == null || snapshot.profile() == null || weaponId() == null) return "polarize_invalid";
        if (!snapshot.creative() && progress.level() != WeaponProgress.MAX_LEVEL) return "polarize_requires_max";
        if (!snapshot.creative() && formaCount() == 0) return "polarize_no_forma";
        if (snapshot.installed().stream().anyMatch(mod -> !owner(mod) && !mod.shared())) return "polarize_locked_mod";
        return "";
    }
    private boolean canPolarize() { return view == PanelRequest.WEAPON && ready() && polarizationFailure().isEmpty(); }
    private void togglePolarization() {
        if (!polarizing && !canPolarize()) return;
        cancelGesture(); search.setFocused(false); setFocused(null);
        polarizing = !polarizing; polarizationSlot = -1; localError = "";
    }
    private void openPolarization(int slot) {
        if (!canPolarize()) { localError = polarizationFailure(); return; }
        polarizationSlot = slot;
        polarizationChoice = PolarityRules.FORMA_CHOICES.stream().filter(value -> value != slotPolarity(slot)).findFirst().orElseThrow();
        localError = "";
    }
    private void submitPolarization() {
        if (polarizationSlot >= 0 && pendingId < 0 && polarizationFailure().isEmpty() && polarizationChoice != slotPolarity(polarizationSlot)) request(page(), PanelRequest.POLARIZE, polarizationSlot);
    }
    private void renderPolarization(PanelCanvas canvas, int mx, int my) {
        hoveredTooltip = null;
        canvas.fill(0, 0, PanelGeometry.WIDTH, PanelGeometry.HEIGHT, 0xD004090E);
        var r = PanelGeometry.POLARIZATION;
        PanelPainter.frame(canvas, r, 0xFF101D27, PanelPainter.GOLD, 7);
        canvas.text(text("polarize_title", polarizationSlot + 1).getString(), r.x() + 18, r.y() + 15, 14, PanelPainter.GOLD, r.width() - 36);
        canvas.text(text(snapshot.creative() ? "polarize_reset_creative" : "polarize_reset").getString(), r.x() + 18, r.y() + 43, 10, PanelPainter.TEXT, r.width() - 36);
        for (int i = 0; i < PolarityRules.FORMA_CHOICES.size(); i++) {
            var choice = PolarityRules.FORMA_CHOICES.get(i); var tile = PanelGeometry.POLARITY_CHOICES.get(i);
            int color = choice == slotPolarity(polarizationSlot) ? PanelPainter.MUTED : choice == polarizationChoice ? PanelPainter.GOLD : PanelPainter.TEAL;
            PanelPainter.frame(canvas, tile, tile.contains(mx, my) ? 0xFF253C46 : 0xFF162832, color, 6);
            canvas.centeredText(ClientPackets.modCatalog().polaritySymbol(choice), tile.x() + tile.width() / 2, tile.y() + 14, 25, color, tile.width() - 20);
            if (tile.contains(mx, my)) hoveredTooltip = List.of(Component.translatable(ClientPackets.modCatalog().polarityName(choice)));
        }
        canvas.text(text(snapshot.creative() ? "polarize_keep_mods" : "polarize_return_mods").getString(), r.x() + 18, r.y() + 135, 10, PanelPainter.MUTED, r.width() - 36);
        String message = pendingId >= 0 ? text("loading").getString() : !polarizationFailure().isEmpty() ? text(polarizationFailure()).getString() : snapshot.error().isEmpty() ? text("polarize_keyboard").getString() : Component.translatable(snapshot.error()).getString();
        canvas.text(message, r.x() + 18, r.y() + 159, 9, PanelPainter.MUTED, r.width() - 36);
        boolean enabled = pendingId < 0 && polarizationFailure().isEmpty() && polarizationChoice != slotPolarity(polarizationSlot);
        PanelPainter.frame(canvas, PanelGeometry.POLARIZE_CONFIRM, 0xFF162832, enabled ? PanelPainter.GOLD : PanelPainter.MUTED, 4);
        PanelPainter.frame(canvas, PanelGeometry.POLARIZE_CANCEL, 0xFF162832, PanelPainter.TEAL, 4);
        canvas.centeredText(text("polarize_confirm").getString(), 459, 379, 10, enabled ? PanelPainter.GOLD : PanelPainter.MUTED, 150);
        canvas.centeredText(Component.translatable("gui.cancel").getString(), 641, 379, 10, PanelPainter.TEXT, 150);
    }
    private void renderDragged(PanelCanvas canvas, int x, int y) {
        if (draggedMod == null) return;
        hoveredTooltip = null;
        var action = dropAt(x, y); boolean cycling = kuvaTarget().contains(x, y) && canCycle(draggedMod); boolean recycling = view == PanelRequest.STORAGE && PanelGeometry.RECYCLE.contains(x, y) && recyclable(draggedMod); boolean releasing = view == PanelRequest.STORAGE && PanelGeometry.RELEASE.contains(x, y) && releasable(draggedMod); boolean valid = releasing || recycling || cycling || canDrop(action, slotAt(x, y));
        var graphics = canvas.graphics(); graphics.pose().pushPose(); graphics.pose().translate(0, 0, 500);
        PanelPainter.frame(canvas, new PanelGeometry.Rect(x - 20, y - 20, 40, 40), 0xE0111D26, valid ? PanelPainter.TEAL : PanelPainter.MUTED, 4);
        renderTexture(canvas, new PanelGeometry.Rect(x - 16, y - 16, 32, 32), draggedMod);
        canvas.text(text(releasing ? "release_label" : recycling ? "recycle_all_hint" : cycling ? "cycle_drop_hint" : valid ? "drop_" + action.name().toLowerCase(Locale.ROOT) : "drop_cancel").getString(), Math.min(x + 24, 920), Math.min(y + 14, 540), 9, valid ? PanelPainter.TEAL : PanelPainter.MUTED, 170);
        graphics.pose().popPose();
    }

    private long maximumWithdrawal() { if (recycleDialog) { var mod = selectedMod(); return mod == null ? 0 : mod.available(); } return snapshot == null ? 0 : withdrawKuva ? Math.min(snapshot.kuva(), snapshot.kuvaSpace()) : Math.min(snapshot.endo(), snapshot.endoSpace()); }
    private void updateWithdrawal() {
        if (withdrawConfirm == null) return;
        withdrawConfirm.active = withdrawalOpen && pendingId < 0 && EndoCurrency.parse(withdrawInput.getValue(), maximumWithdrawal()).isPresent() && (!recycleDialog || recycleReward() >= 0 && recycleReward() <= Long.MAX_VALUE - snapshot.endo());
        withdrawInput.setEditable(pendingId < 0);
    }
    private void showWithdrawal(boolean open) {
        if (open) cancelGesture();
        withdrawalOpen = open;
        withdrawInput.visible = open; withdrawConfirm.visible = open; withdrawCancel.visible = open;
        if (open) { setFocused(withdrawInput); withdrawInput.setFocused(true); }
        else { withdrawInput.setFocused(false); if (getFocused() == withdrawInput) setFocused(null); }
        updateWithdrawal();
    }
    private void openWithdrawal() { openWithdrawal(false); }
    private void openWithdrawal(boolean kuva) {
        recycleDialog = false; withdrawKuva = kuva; withdrawConfirm.setMessage(text(kuva ? "kuva_withdraw" : "endo_withdraw"));
        if (pendingId >= 0 || snapshot == null) return;
        withdrawInput.setValue(""); localError = ""; withdrawalError = ""; showWithdrawal(true);
    }
    private void closeWithdrawal() { showWithdrawal(false); recycleDialog = false; }
    private boolean recyclable(PanelPayload.ModView mod) { return view == PanelRequest.STORAGE && owner(mod) && mod.weapon() == null && mod.available() > 0; }
    private void openRecycling(PanelPayload.ModView mod) {
        if (!ready() || !recyclable(mod)) { localError = "recycle_denied"; return; }
        select(mod); recycleDialog = true; withdrawKuva = false; withdrawConfirm.setMessage(text("recycle_confirm"));
        withdrawInput.setValue("1"); withdrawalError = ""; showWithdrawal(true);
    }
    private long recycleReward() {
        var mod = selectedMod(); if (mod == null) return 0;
        try { long count = EndoCurrency.parse(withdrawInput.getValue(), maximumWithdrawal()).orElse(0); return Math.multiplyExact(count, ModRecycling.value(ClientPackets.modCatalog().qualities().get(mod.info().rarity()).baseEndo(), mod.info().level())); }
        catch (IllegalArgumentException | ArithmeticException exception) { return -1; }
    }
    private void submitWithdrawal() {
        if (withdrawConfirm.active) request(page(), recycleDialog ? PanelRequest.RECYCLE : withdrawKuva ? PanelRequest.WITHDRAW_KUVA : PanelRequest.WITHDRAW_ENDO);
    }
    private void renderWithdrawal(PanelCanvas canvas, int mx, int my, float partialTick) {
        hoveredTooltip = null;
        canvas.fill(0, 0, PanelGeometry.WIDTH, PanelGeometry.HEIGHT, 0xC004090E);
        var dialog = PanelGeometry.WITHDRAWAL;
        PanelPainter.frame(canvas, dialog, 0xFF101D27, PanelPainter.GOLD, 7);
        canvas.text(text(recycleDialog ? "recycle_title" : withdrawKuva ? "kuva_withdraw_title" : "endo_withdraw_title").getString(), dialog.x() + 18, dialog.y() + 15, 14, PanelPainter.GOLD, dialog.width() - 36);
        canvas.text((recycleDialog ? text("recycle_preview", maximumWithdrawal(), Math.max(0, recycleReward())) : text(withdrawKuva ? "kuva_available" : "endo_available", withdrawKuva ? snapshot.kuva() : snapshot.endo(), maximumWithdrawal())).getString(), dialog.x() + 18, dialog.y() + 42, 9, PanelPainter.MUTED, dialog.width() - 36);
        String message = pendingId >= 0 ? text("loading").getString() : withdrawalError.isEmpty() ? "" : Component.translatable(withdrawalError).getString();
        if (message.isEmpty() && recycleDialog && EndoCurrency.parse(withdrawInput.getValue(), maximumWithdrawal()).isPresent()
                && (recycleReward() < 0 || recycleReward() > Long.MAX_VALUE - snapshot.endo())) message = text("recycle_overflow").getString();
        if (message.isEmpty() && (maximumWithdrawal() == 0 || !withdrawInput.getValue().isEmpty() && !withdrawConfirm.active))
            message = recycleDialog ? text("recycle_amount_invalid").getString() : maximumWithdrawal() == 0 ? text(withdrawKuva ? snapshot.kuva() == 0 ? "kuva_insufficient" : "kuva_inventory_full" : snapshot.endo() == 0 ? "endo_insufficient" : "endo_inventory_full").getString() : text(withdrawKuva ? "kuva_input_range" : "endo_input_range", maximumWithdrawal()).getString();
        canvas.text(message, dialog.x() + 18, dialog.y() + 101, 9, 0xFFFF9A8D, dialog.width() - 36);
        withdrawInput.render(canvas.graphics(), mx, my, partialTick); withdrawConfirm.render(canvas.graphics(), mx, my, partialTick); withdrawCancel.render(canvas.graphics(), mx, my, partialTick);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double mx = logicalX(mouseX), my = logicalY(mouseY);
        if (cycleOpen) {
            if (button == 0 && pendingId < 0) {
                if (PanelGeometry.CYCLE_HELP.contains(mx, my)) { openCycleRules(); return true; }
                if (PanelGeometry.CYCLE_OLD.contains(mx, my)) cycleUseNew = false;
                if (PanelGeometry.CYCLE_NEW.contains(mx, my) && snapshot != null && snapshot.cycleViews().size() == 2) cycleUseNew = true;
                if (PanelGeometry.CYCLE_ROLL.contains(mx, my)) submitCycle();
                if (PanelGeometry.CYCLE_CLOSE.contains(mx, my)) { cycleOpen = false; updateButtons(); }
            }
            return true;
        }
        if (polarizationSlot >= 0) {
            if (button == 0 && pendingId < 0) {
                for (int i = 0; i < PanelGeometry.POLARITY_CHOICES.size(); i++) if (PanelGeometry.POLARITY_CHOICES.get(i).contains(mx, my)) polarizationChoice = PolarityRules.FORMA_CHOICES.get(i);
                if (PanelGeometry.POLARIZE_CONFIRM.contains(mx, my)) submitPolarization();
                if (PanelGeometry.POLARIZE_CANCEL.contains(mx, my)) polarizationSlot = -1;
            }
            return true;
        }
        if (withdrawalOpen) {
            if (!withdrawConfirm.mouseClicked(mx, my, button) && !withdrawCancel.mouseClicked(mx, my, button) && withdrawInput.mouseClicked(mx, my, button)) setFocused(withdrawInput);
            return true;
        }
        if (super.mouseClicked(mx, my, button)) return true;
        if (!ready() || button != 0 && button != 1) return false;
        if (view == PanelRequest.WEAPON && button == 0 && PanelGeometry.POLARIZE.contains(mx, my)) { togglePolarization(); return true; }
        if (polarizing) {
            if (snapshot.slotCount() > 8 && PanelGeometry.SLOT_PAGER.contains(mx, my)) { changeSlotPage(mx < 728 ? -1 : 1); return true; }
            int slot = slotAt(mx, my);
            if (button == 1) polarizing = false;
            else if (slot >= 0) openPolarization(slot);
            return true;
        }
        if (view == PanelRequest.WEAPON && button == 0) {
            for (int i = 0; i < hotbarRectangles.size(); i++) if (hotbarRectangles.get(i).contains(mx, my)) {
                switchWeapon(hotbarSlots.get(i)); return true;
            }
        }
        var mod = selectedMod();
        if (button == 0 && view == PanelRequest.STORAGE && PanelGeometry.RELEASE.contains(mx, my)) { release(mod); return true; }
        if (button == 0 && view == PanelRequest.WEAPON && PanelGeometry.WEAPON_KUVA.contains(mx, my)) { openCycle(mod); return true; }
        if (button == 0 && upgradeRect().contains(mx, my)) {
            if (upgradeable(mod)) request(page(), PanelRequest.UPGRADE); return true;
        }
        if (button == 0 && PanelGeometry.PERMISSION.contains(mx, my)) {
            if (owner(mod) && here(mod)) request(page(), PanelRequest.SHARE); return true;
        }
        if (view == PanelRequest.WEAPON) {
            if (button == 0 && snapshot.slotCount() > 8 && PanelGeometry.SLOT_PAGER.contains(mx, my)) { changeSlotPage(mx < 728 ? -1 : 1); return true; }
            var slots = PanelGeometry.slots();
            for (int i = 0; i < slots.size(); i++) if (slots.get(i).contains(mx, my) && slotPage * 8 + i < snapshot.slotCount()) {
                selectedSlot = slotPage * 8 + i;
                var installed = inSlot(selectedSlot);
                if (installed == null) { selected = null; cancelGesture(); updateButtons(); }
                else if (button == 1) { cancelGesture(); remove(installed); }
                else beginGesture(installed, selectedSlot, mx, my);
                return true;
            }
        }
        var rectangles = view == PanelRequest.STORAGE ? PanelGeometry.storage() : PanelGeometry.compatible();
        for (int i = 0; i < Math.min(rectangles.size(), snapshot.mods().size()); i++) if (rectangles.get(i).contains(mx, my)) {
            var card = snapshot.mods().get(i);
            if (button == 1) {
                cancelGesture(); select(card);
                if (releasable(card)) release(card); else if (withdrawable(card)) request(page(), PanelRequest.WITHDRAW_MOD); else localError = "gesture_denied";
                return true;
            }
            beginGesture(card, here(card) ? card.slot() : -1, mx, my);
            return true;
        }
        return false;
    }
    private void beginGesture(PanelPayload.ModView mod, int slot, double x, double y) {
        select(mod); draggedMod = mod; pointerX = x; pointerY = y;
        if (recycleMode && view == PanelRequest.STORAGE && mod.weapon() == null) { gesture.press(mod.id(), slot, x, y, net.minecraft.Util.getMillis()); return; }
        if (gesture.press(mod.id(), slot, x, y, net.minecraft.Util.getMillis())) { quick(mod); gesture.release(); draggedMod = null; }
    }
    private void changeSlotPage(int direction) { slotPage = Math.max(0, Math.min((snapshot.slotCount() - 1) / 8, slotPage + direction)); }
    @Override public boolean mouseReleased(double x, double y, int button) {
        if (cycleOpen) return true;
        if (withdrawalOpen || polarizationSlot >= 0 || polarizing) return true;
        if (button == 0 && gesture.dragging() && view == PanelRequest.STORAGE && PanelGeometry.RELEASE.contains(logicalX(x), logicalY(y))) {
            var mod = draggedMod; cancelGesture(); release(mod); return true;
        }
        if (button == 0 && gesture.dragging() && view == PanelRequest.STORAGE && PanelGeometry.RECYCLE.contains(logicalX(x), logicalY(y))) {
            var mod = draggedMod; cancelGesture(); if (recyclable(mod)) { select(mod); request(page(), PanelRequest.RECYCLE_ALL); } else localError = "recycle_denied"; return true;
        }
        if (button == 0 && gesture.dragging() && kuvaTarget().contains(logicalX(x), logicalY(y)) && canCycle(draggedMod)) {
            var mod = draggedMod; cancelGesture(); openCycle(mod); return true;
        }
        if (button == 0 && gesture.active() && !gesture.dragging() && recycleMode && view == PanelRequest.STORAGE && draggedMod.weapon() == null) { var mod = draggedMod; cancelGesture(); openRecycling(mod); return true; }
        if (button == 0 && gesture.active()) {
            double mx = logicalX(x), my = logicalY(y); var action = dropAt(mx, my); int slot = slotAt(mx, my);
            var mod = draggedMod;
            if (gesture.dragging() && action != ModGesture.Action.NONE) {
                if (!canDrop(action, slot)) {
                    if (action != ModGesture.Action.INSTALL || !rejectConflict(mod)) localError = "gesture_denied";
                }
                else {
                    select(mod);
                    switch (action) {
                        case INSTALL -> request(page(), PanelRequest.INSTALL, slot < 0 ? firstSlot() : slot);
                        case MOVE -> request(page(), PanelRequest.MOVE, slot);
                        case REMOVE -> remove(mod);
                        case WITHDRAW_ITEM -> request(page(), PanelRequest.WITHDRAW_MOD);
                        default -> { }
                    }
                }
            }
            gesture.release(); draggedMod = null; dragPageTicks = 0; return true;
        }
        return super.mouseReleased(logicalX(x), logicalY(y), button);
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (cycleOpen) return true;
        if (withdrawalOpen || polarizationSlot >= 0 || polarizing) return true;
        if (button == 0 && gesture.active()) { pointerX = logicalX(x); pointerY = logicalY(y); gesture.drag(pointerX, pointerY); return true; }
        return super.mouseDragged(logicalX(x), logicalY(y), button, dx / scale(), dy / scale());
    }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (cycleOpen || withdrawalOpen || polarizationSlot >= 0) return true;
        if (gesture.active()) {
            if (view == PanelRequest.WEAPON && snapshot.slotCount() > 8 && dy != 0) changeSlotPage(dy > 0 ? -1 : 1);
            return true;
        }
        if (view == PanelRequest.WEAPON && PanelGeometry.DETAILS.contains(logicalX(x), logicalY(y))) {
            if (dy != 0) detailsScroll = Math.max(0, Math.min(Math.max(0, detailLineCount - 14), detailsScroll + (dy > 0 ? -3 : 3)));
            return true;
        }
        if (pendingId < 0 && snapshot != null && dy != 0) { int page = Math.max(0, Math.min(snapshot.pages() - 1, page() + (dy > 0 ? -1 : 1))); if (page != page()) request(page, PanelRequest.REFRESH); }
        return true;
    }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (ready() && !search.isFocused() && key == GLFW.GLFW_KEY_DELETE && releasable(selectedMod())) { cancelGesture(); release(selectedMod()); return true; }
        if (cycleOpen) {
            if (key == GLFW.GLFW_KEY_F1) openCycleRules();
            else if (key == GLFW.GLFW_KEY_ESCAPE) { cycleOpen = false; updateButtons(); }
            else if (key == GLFW.GLFW_KEY_LEFT) cycleUseNew = false;
            else if (key == GLFW.GLFW_KEY_RIGHT && snapshot != null && snapshot.cycleViews().size() == 2) cycleUseNew = true;
            else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) submitCycle();
            return true;
        }
        if (ready() && !search.isFocused() && minecraft.options.keyDrop.matches(key, scanCode) && hoveredMod != null) {
            var target = hoveredMod;
            if (withdrawable(target)) { select(target); request(page(), hasControlDown() ? PanelRequest.DROP_STACK : PanelRequest.DROP_MOD); }
            else localError = "gesture_denied";
            return true;
        }
        if (polarizationSlot >= 0) {
            if (pendingId < 0) {
                if (key == GLFW.GLFW_KEY_ESCAPE) polarizationSlot = -1;
                else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) submitPolarization();
                else if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_TAB) polarizationChoice = PolarityRules.FORMA_CHOICES.get(Math.floorMod(PolarityRules.FORMA_CHOICES.indexOf(polarizationChoice) + (key == GLFW.GLFW_KEY_LEFT ? -1 : 1), 3));
            }
            return true;
        }
        if (!withdrawalOpen && !search.isFocused() && view == PanelRequest.WEAPON) {
            if (key == GLFW.GLFW_KEY_P) { togglePolarization(); return true; }
            if (polarizing) {
                if (key == GLFW.GLFW_KEY_ESCAPE) polarizing = false;
                else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) openPolarization(selectedSlot);
                else if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
                    int direction = key == GLFW.GLFW_KEY_LEFT ? -1 : key == GLFW.GLFW_KEY_RIGHT ? 1 : key == GLFW.GLFW_KEY_UP ? -4 : 4;
                    selectedSlot = Math.max(0, Math.min(snapshot.slotCount() - 1, selectedSlot + direction)); slotPage = selectedSlot / 8;
                }
                return true;
            }
        }
        if (key == GLFW.GLFW_KEY_ESCAPE && gesture.active()) { cancelGesture(); return true; }
        if (withdrawalOpen) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { closeWithdrawal(); return true; }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) { submitWithdrawal(); return true; }
            if (key == GLFW.GLFW_KEY_TAB) { setFocused(withdrawInput); withdrawInput.setFocused(true); return true; }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public void tick() {
        if (minecraft.player == null) { onClose(); return; }
        updateWeaponStrip();
        if (pendingId >= 0 && ++waitingTicks > 100) {
            pendingId = -1; localError = "timeout";
            if ((pendingAction == PanelRequest.WITHDRAW_ENDO || pendingAction == PanelRequest.WITHDRAW_KUVA || pendingAction == PanelRequest.RECYCLE)) withdrawalError = "gui.yagens_attributes.timeout";
            updateButtons();
        }
        if (gesture.dragging() && view == PanelRequest.WEAPON && snapshot.slotCount() > 8 && PanelGeometry.SLOT_PAGER.contains(pointerX, pointerY)) {
            if (++dragPageTicks >= 12) { changeSlotPage(pointerX < 728 ? -1 : 1); dragPageTicks = 0; }
        } else dragPageTicks = 0;
        if (view == PanelRequest.WEAPON && snapshot != null && pendingId < 0 && !withdrawalOpen && !cycleOpen && inspectedHotbarSlot != minecraft.player.getInventory().selected) {
            cancelGesture(); polarizing = false; polarizationSlot = -1; selected = null; snapshot = null; detailSource = null; slotPage = 0; selectedSlot = 0;
            request(0, PanelRequest.REFRESH); return;
        }
        if (!withdrawalOpen && !cycleOpen && polarizationSlot < 0 && !polarizing && !gesture.active() && queryDirty && pendingId < 0 && --debounce <= 0) request(0, PanelRequest.REFRESH);
    }
    @Override public void onClose() { cancelGesture(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
