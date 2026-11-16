package yagen.waitmydawn.yagens_attributes.data;

import yagen.waitmydawn.yagens_attributes.core.ui.*;
import yagen.waitmydawn.yagens_attributes.core.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import javax.imageio.ImageIO;

/**
 * Offscreen design fixtures share the production card painter and geometry without launching a game.
 */
public final class ArsenalPreview implements PanelPainter.Canvas {
    private final BufferedImage image = new BufferedImage(PanelGeometry.WIDTH * 2, PanelGeometry.HEIGHT * 2, BufferedImage.TYPE_INT_ARGB);
    private final Graphics2D graphics = image.createGraphics();
    private static final ModCatalog CATALOG = BuiltinMods.catalog();
    private static final java.util.List<ModCatalog.Card> CARDS = CATALOG.creative().stream().filter(card -> card.level() == 0).toList();
    private boolean ranged;

    private ArsenalPreview() {
        graphics.scale(2, 2);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static void render() {
        Path output = Path.of("build/ui-preview");
        try {
            Files.createDirectories(output);
            var storage = new ArsenalPreview();
            storage.storage();
            storage.write(output.resolve("mod-storage.png"));
            var weapon = new ArsenalPreview();
            weapon.weapon(false);
            weapon.write(output.resolve("weapon-panel.png"));
            var ranged = new ArsenalPreview();
            ranged.weapon(true);
            ranged.write(output.resolve("weapon-ranged.png"));
            var drag = new ArsenalPreview();
            drag.weapon(false);
            drag.drag();
            drag.write(output.resolve("weapon-drag.png"));
            var withdrawal = new ArsenalPreview();
            withdrawal.storage();
            withdrawal.withdrawal();
            withdrawal.write(output.resolve("endo-withdrawal.png"));
            var filters = new ArsenalPreview(); filters.filters(false); filters.write(output.resolve("mod-filters.png"));
            var split = new ArsenalPreview(); split.filters(true); split.write(output.resolve("mod-filters-split.png"));
            System.out.println("Rendered shared-painter UI previews to " + output.toAbsolutePath());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot render UI preview", exception);
        }
    }

    private void filters(boolean splitting) throws IOException {
        var filter = new ModFilter(java.util.List.of(java.util.List.of(ModFilter.Tag.RARE, ModFilter.Tag.LEGENDARY), java.util.List.of(ModFilter.Tag.AVAILABLE), java.util.List.of(ModFilter.Tag.LEVELED)));
        var layout = new ModFilterLayout(filter, 0);
        com.google.gson.JsonObject lang;
        try (var input = java.util.Objects.requireNonNull(ArsenalPreview.class.getResourceAsStream("/assets/yagens_attributes/lang/zh_cn.json"))) {
            lang = com.google.gson.JsonParser.parseString(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        }
        java.util.function.Function<String, String> words = key -> lang.get("gui.yagens_attributes." + key).getAsString();
        java.util.function.Function<ModFilter.Tag, String> tags = tag -> lang.get(tag.rarity() == null ? "filter.yagens_attributes." + tag.key() : CATALOG.qualityName(tag.rarity())).getAsString();
        java.util.function.ToIntFunction<ModFilter.Tag> colors = tag -> tag.rarity() == null ? PanelPainter.TEAL : 0xFF000000 | CATALOG.qualities().get(tag.rarity()).color();
        var state = new ModFilterPainter.State(0, 0, false, 0, splitting ? layout.links.get(0) : null, null, null, -1, -1);
        ModFilterPainter.backdrop(this, words); ModFilterPainter.palette(this, tags, colors, state);
        ModFilterPainter.groups(this, filter, layout, tags, words, colors, state);
        control(ModFilterLayout.CLEAR.x(), ModFilterLayout.CLEAR.y(), ModFilterLayout.CLEAR.width(), words.apply("filter_clear"), false);
        control(ModFilterLayout.CANCEL.x(), ModFilterLayout.CANCEL.y(), ModFilterLayout.CANCEL.width(), "取消", false);
        control(ModFilterLayout.APPLY.x(), ModFilterLayout.APPLY.y(), ModFilterLayout.APPLY.width(), words.apply("filter_apply"), true);
        if (splitting) text("拆分后：（稀有）与（传说）", 460, 203, 10, 0xFFFF8A79, 520);
    }

    private void storage() {
        PanelPainter.background(this, "玩家 MOD 仓库", "布局预览 · 示例库存，不代表游戏内已有 Mod");
        control(PanelGeometry.WIDTH - 270, 39, 120, "Mod 空间", true);
        control(PanelGeometry.WIDTH - 140, 39, 120, "武器面板", false);
        control(20, 82, 236, "搜索 Mod…", false);
        control(PanelGeometry.WIDTH - 260, 82, 240, "排序：品质", false);
        PanelPainter.frame(this, PanelGeometry.ENDO, 0xD0111D26, 0xFF607883, 4);
        itemIcon("/assets/yagens_attributes/textures/item/endo.png", PanelGeometry.ENDO.x() + 5, PanelGeometry.ENDO.y() + 4, 16);
        text("12,500", PanelGeometry.ENDO.x() + 27, PanelGeometry.ENDO.y() + 7, 10, PanelPainter.GOLD, 130);
        int x = 20;
        for (String category : new String[]{"全部", "战甲", "武器", "近战", "远程", "剑", "斧", "三叉戟"}) {
            control(x, 112, 76, category, category.equals("武器"));
            x += 84;
        }
        var cells = PanelGeometry.storage();
        for (int i = 0; i < cells.size(); i++) drawCard(cells.get(i), i, i == 2);
        footer("首批定义 · 示例等级及数量", "第 1 / 1 页");
        PanelPainter.icon(this, PanelGeometry.STORAGE_UPGRADE, PanelPainter.Icon.UPGRADE, true, false);
        text("升级 · 4", 156, 572, 9, PanelPainter.GOLD, 136);
        PanelPainter.frame(this, PanelGeometry.HELD_WEAPON, 0xB0111D26, PanelPainter.MUTED, 4);
        itemIcon("/assets/minecraft/textures/item/diamond_sword.png", PanelGeometry.HELD_WEAPON.x() + 16, PanelGeometry.HELD_WEAPON.y() + 8, 16);
    }

    private void weapon(boolean ranged) {
        this.ranged = ranged;
        PanelPainter.background(this, "武器面板", "布局预览 · 示例配置 · 基础伤害 7");
        control(PanelGeometry.WIDTH - 270, 39, 120, "Mod 空间", false);
        control(PanelGeometry.WIDTH - 140, 39, 120, "武器面板", true);
        PanelPainter.frame(this, new PanelGeometry.Rect(20, 80, 208, PanelGeometry.HEIGHT - 139), 0xD00C151E, 0xFF445F6A, 7);
        String[] weapons = {"diamond_sword", "diamond_axe", "bow", "crossbow_standby", "mace"};
        var strip = PanelGeometry.weaponStrip(80, weapons.length);
        for (int i = 0; i < weapons.length; i++) {
            var r = strip.get(i);
            boolean current = i == (ranged ? 2 : 0);
            PanelPainter.frame(this, r, current ? 0x90152630 : 0x40101A22, current ? PanelPainter.TEAL : 0x50526975, 4);
            itemIcon("/assets/minecraft/textures/item/" + weapons[i] + ".png", r.x() + 2, r.y() + 2, 32);
        }
        text("武器属性", 32, 93, 14, PanelPainter.GOLD, 180);
        text("等级 20 / 30", 32, 115, 10, PanelPainter.TEXT, 184);
        fill(32, 130, 184, 5, 0xFF263943);
        fill(32, 130, 39, 5, PanelPainter.TEAL);
        text("经验 10 / 47", 32, 140, 8, PanelPainter.MUTED, 184);
        String[] labels = ranged ? new String[]{"暴击几率", "暴击伤害", "触发几率", "触发时间", "射速", "多重射击"} : new String[]{"暴击几率", "暴击伤害", "触发几率", "触发时间"};
        String[] values = ranged ? new String[]{"40%", "2x", "20%", "6 s", "2x", "1.5"} : new String[]{"43.2%", "2x", "18%", "6 s"};
        for (int i = 0; i < labels.length; i++) {
            text(labels[i], 32, 164 + i * 14, 10, PanelPainter.MUTED, 120);
            text(values[i], 162, 164 + i * 14, 10, PanelPainter.TEXT, 54);
        }
        int offset = ranged ? 28 : 0;
        fill(32, 221 + offset, 184, 1, 0x506A8791);
        text("伤害组成", 32, 234 + offset, 12, PanelPainter.GOLD, 180);
        String[] components = {"冰冻", "冲击", "穿刺", "切割"};
        String[] amounts = ranged ? new String[]{"3", "0.6", "4.2", "1.8"} : new String[]{"3", "0.7", "3.15", "4.725"};
        String[] types = {"cold", "impact", "puncture", "slash"};
        for (int i = 0; i < components.length; i++) {
            itemIcon("/assets/yagens_attributes/textures/status/" + types[i] + "_status.png", 32, 256 + offset + i * 12, 9);
            text(components[i], 46, 256 + offset + i * 12, 10, PanelPainter.MUTED, 106);
            text(amounts[i], 162, 256 + offset + i * 12, 10, i == 0 || i == 3 ? 0xFF8BCCA7 : PanelPainter.TEXT, 54);
        }
        text("总伤害", 32, 317 + offset, 10, PanelPainter.MUTED, 120);
        text(ranged ? "9.6" : "11.575", 162, 317 + offset, 10, PanelPainter.GOLD, 54);
        PanelPainter.capacity(this, 3, 20, "容量 3/20");
        var slots = PanelGeometry.slots();
        for (int i = 0; i < slots.size(); i++) {
            if (i == 0 || i == 2 || i == 5) {
                var mod = sample(i + 2, i == 2);
                PanelPainter.card(this, slots.get(i), new PanelPainter.Card(mod.title(), mod.subtitle(), mod.polarity(), 1, mod.rarity(), mod.selected(), true, mod.capacityCost(), mod.rank(), mod.maxRank()));
                artwork(slots.get(i), i + 2);
            } else
                PanelPainter.emptySlot(this, slots.get(i), "槽位 " + (i + 1), i == 6 ? ModInfo.Polarity.MADURAI.symbol() : "", i == 6);
        }
        PanelPainter.frame(this, PanelGeometry.DETAILS, 0xD00C151E, 0xFF445F6A, 7);
        text("Mod 详情", 812, 120, 12, PanelPainter.GOLD, 256);
        centeredText("万用纳米立方", 940, 140, 10, 0xFFB87333, 256);
        text("武器", 812, 154, 10, PanelPainter.TEXT, 256);
        text("等级 5", 812, 168, 10, PanelPainter.MAX_LEVEL, 50);
        text(ModInfo.Polarity.MADURAI.symbol() + "  容量消耗 7", 858, 168, 10, PanelPainter.TEXT, 210);
        text("+90% 伤害", 812, 182, 10, 0xFFB87333, 256);
        PanelPainter.icon(this, PanelGeometry.DETAIL_UPGRADE, PanelPainter.Icon.UPGRADE, true, false);
        text("升级 · 2", 856, 344, 9, PanelPainter.GOLD, 200);
        text("可用于此武器的 MOD", 250, 392, 11, PanelPainter.GOLD, 400);
        control(250, 408, 236, "搜索 Mod…", false);
        control(PanelGeometry.WIDTH - 260, 408, 240, "排序：品质", false);
        var cards = PanelGeometry.compatible();
        for (int i = 0; i < cards.size(); i++) drawCard(cards.get(i), i, i == 2);
        footer("12 种兼容 Mod · 共 36 张", "第 1 / 2 页");
    }

    private void drag() {
        var target = PanelGeometry.slots().get(7);
        PanelPainter.frame(this, target, 0x204CBF95, 0xFF79DBA7, 9);
        int x = target.x() + target.width() / 2, y = target.y() + target.height() / 2;
        PanelPainter.frame(this, new PanelGeometry.Rect(x - 20, y - 20, 40, 40), 0xE0111D26, PanelPainter.TEAL, 4);
        itemIcon("/assets/yagens_attributes/textures/item/mod.png", x - 16, y - 16, 32);
        text("松开安装", x + 24, y + 14, 9, PanelPainter.TEAL, 170);
    }

    private void withdrawal() {
        fill(0, 0, PanelGeometry.WIDTH, PanelGeometry.HEIGHT, 0xC004090E);
        var r = PanelGeometry.WITHDRAWAL;
        PanelPainter.frame(this, r, 0xFF101D27, PanelPainter.GOLD, 7);
        text("取出内融核心", r.x() + 18, r.y() + 15, 14, PanelPainter.GOLD, r.width() - 36);
        text("余额 12500 · 本次最多可取 2304", r.x() + 18, r.y() + 42, 9, PanelPainter.MUTED, r.width() - 36);
        control(r.x() + 18, r.y() + 67, r.width() - 36, "64", false);
        control(r.x() + 18, r.y() + 129, 132, "取出", false);
        control(r.x() + 170, r.y() + 129, 132, "取消", false);
    }

    private ModCatalog.Card variant(int i) {
        var card = CARDS.get(i % CARDS.size());
        return CATALOG.card(card.id(), Math.min(i == 2 ? 5 : i % 6, card.info().details().maxLevel()));
    }

    private PanelPainter.Card sample(int i, boolean selected) {
        var card = variant(i);
        var info = card.info();
        return new PanelPainter.Card(ModNames.translate("zh_cn", info.name()), "武器", info.polaritySymbol(), i % 5 + 1, info.color(), selected, i % 5 == 0, info.capacityCost(), "等级 " + info.level(), info.level() == info.details().maxLevel());
    }

    private void artwork(PanelGeometry.Rect rectangle, int i) {
        var art = PanelGeometry.artwork(rectangle);
        var texture = variant(i).info().details().texture().split(":", 2);
        itemIcon("/assets/" + texture[0] + "/textures/" + texture[1] + ".png", art.x(), art.y(), art.width());
    }

    private void drawCard(PanelGeometry.Rect rectangle, int i, boolean selected) {
        PanelPainter.card(this, rectangle, sample(i, selected));
        artwork(rectangle, i);
    }

    private void footer(String status, String page) {
        text(status, 24, PanelGeometry.HEIGHT - 56, 9, PanelPainter.MUTED, 650);
        PanelPainter.icon(this, PanelGeometry.PHYSICAL_MOD, PanelPainter.Icon.INVENTORY, true, false);
        PanelPainter.icon(this, PanelGeometry.PERMISSION, PanelPainter.Icon.PRIVATE, true, false);
        text("双击装卸 · 拖拽移动 · 右键取出", 310, 576, 9, PanelPainter.MUTED, 420);
        control(PanelGeometry.WIDTH - 350, PanelGeometry.HEIGHT - 30, 28, "<", false);
        control(PanelGeometry.WIDTH - 316, PanelGeometry.HEIGHT - 30, 28, ">", false);
        text(page, PanelGeometry.WIDTH - 274, PanelGeometry.HEIGHT - 24, 10, PanelPainter.MUTED, 95);
        control(PanelGeometry.WIDTH - 172, PanelGeometry.HEIGHT - 30, 72, "刷新", false);
        control(PanelGeometry.WIDTH - 92, PanelGeometry.HEIGHT - 30, 72, "完成", false);
    }

    private void control(int x, int y, int width, String value, boolean selected) {
        PanelPainter.frame(this, new PanelGeometry.Rect(x, y, width, 24), selected ? 0xD02E3430 : 0xD0111D26, selected ? PanelPainter.GOLD : 0xFF607883, 4);
        text(value, x + 7, y + 7, 10, selected ? PanelPainter.GOLD : PanelPainter.TEXT, width - 14);
    }

    private void write(Path path) throws IOException {
        graphics.dispose();
        ImageIO.write(image, "png", path.toFile());
    }

    private void itemIcon(String path, int x, int y, int size) {
        try (var stream = ArsenalPreview.class.getResourceAsStream(path)) {
            if (stream == null) return;
            var texture = ImageIO.read(stream);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.drawImage(texture, x, y, size, size, null);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load preview icon", exception);
        }
    }

    @Override
    public void fill(int x, int y, int width, int height, int color) {
        graphics.setColor(new Color(color, true));
        graphics.fillRect(x, y, width, height);
    }

    @Override
    public void gradient(int x, int y, int width, int height, int top, int bottom) {
        graphics.setPaint(new GradientPaint(x, y, new Color(top, true), x, y + height, new Color(bottom, true)));
        graphics.fillRect(x, y, width, height);
    }

    @Override
    public void text(String value, int x, int y, float size, int color, int maximumWidth) {
        graphics.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12).deriveFont(size));
        graphics.setColor(new Color(color, true));
        var metrics = graphics.getFontMetrics();
        String shown = value;
        if (textWidth(shown, size) > maximumWidth) {
            while (!shown.isEmpty() && textWidth(shown + "…", size) > maximumWidth)
                shown = shown.substring(0, shown.length() - 1);
            shown += "…";
        }
        float cursor = x;
        for (char ch : shown.toCharArray()) {
            var polarity = PolarityGlyphs.type(ch);
            if (polarity == null) {
                graphics.drawString(String.valueOf(ch), cursor, y + metrics.getAscent());
                cursor += metrics.charWidth(ch);
            } else {
                float pixelSize = size / 32F;
                var glyph = glyph(polarity);
                for (int gy = 0; gy < 32; gy++)
                    for (int gx = 0; gx < 32; gx++) {
                        int alpha = glyph.getRGB(gx, gy) >>> 24;
                        if (alpha == 0) continue;
                        graphics.setColor(new Color((color & 0xFFFFFF) | (alpha * (color >>> 24) / 255 << 24), true));
                        graphics.fill(new java.awt.geom.Rectangle2D.Float(cursor + gx * pixelSize, y + gy * pixelSize, pixelSize, pixelSize));
                    }
                graphics.setColor(new Color(color, true));
                cursor += size;
            }
        }
    }

    @Override
    public void centeredText(String value, int centerX, int y, float size, int color, int maximumWidth) {
        graphics.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12).deriveFont(size));
        text(value, centerX - Math.round(Math.min(maximumWidth, textWidth(value, size)) / 2), y, size, color, maximumWidth);
    }

    private float textWidth(String value, float size) {
        float width = 0;
        for (char ch : value.toCharArray())
            width += PolarityGlyphs.type(ch) == null ? graphics.getFontMetrics().charWidth(ch) : size;
        return width;
    }

    private static final java.util.Map<ModInfo.Polarity, BufferedImage> GLYPHS = new java.util.EnumMap<>(ModInfo.Polarity.class);

    private static BufferedImage glyph(ModInfo.Polarity polarity) {
        return GLYPHS.computeIfAbsent(polarity, key -> {
            try (var input = ArsenalPreview.class.getResourceAsStream("/assets/yagens_attributes/textures/polarity/" + key.key() + ".png")) {
                return ImageIO.read(java.util.Objects.requireNonNull(input));
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot load polarity preview", exception);
            }
        });
    }
}
