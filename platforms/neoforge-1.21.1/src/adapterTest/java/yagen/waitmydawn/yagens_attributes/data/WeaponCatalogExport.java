package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;

/** Opt-in registry inspection and native inventory rendering, excluded from release artifacts. */
@EventBusSubscriber(modid = YagensAttributes.MODID, value = Dist.CLIENT)
public final class WeaponCatalogExport {
    private static final Map<String, String> REGISTRIES = Map.of(
            "irons_spellbooks", "io.redspace.ironsspellbooks.registries.ItemRegistry",
            "cataclysm", "com.github.L_Ender.cataclysm.init.ModItems",
            "twilightforest", "twilightforest.init.TFItems");
    private static final Map<String, String> SPECIAL_CLASSES = Map.ofEntries(
            Map.entry("io.redspace.ironsspellbooks.item.weapons.StaffItem", "casting"),
            Map.entry("io.redspace.ironsspellbooks.item.weapons.StaffOfTheNines", "casting"),
            Map.entry("com.github.L_Ender.cataclysm.items.Cataclysm_Weapon", "custom_melee"),
            Map.entry("com.github.L_Ender.cataclysm.items.Coral_Spear", "throwable"),
            Map.entry("com.github.L_Ender.cataclysm.items.Coral_Bardiche", "throwable"),
            Map.entry("com.github.L_Ender.cataclysm.items.Brontes", "custom_melee"),
            Map.entry("com.github.L_Ender.cataclysm.items.Infernal_forge", "custom_melee"),
            Map.entry("com.github.L_Ender.cataclysm.items.Void_forge", "custom_melee"),
            Map.entry("com.github.L_Ender.cataclysm.items.Laser_Gatling", "custom_ranged"),
            Map.entry("com.github.L_Ender.cataclysm.items.Wrath_of_the_desert", "custom_ranged"),
            Map.entry("com.github.L_Ender.cataclysm.items.Void_Assault_SHoulder_Weapon", "custom_ranged"),
            Map.entry("com.github.L_Ender.cataclysm.items.Wither_Assault_SHoulder_Weapon", "custom_ranged"),
            Map.entry("com.github.L_Ender.cataclysm.items.Bulwark_of_the_flame", "defensive"),
            Map.entry("com.github.L_Ender.cataclysm.items.Void_core", "casting"),
            Map.entry("twilightforest.item.TwilightWandItem", "casting"),
            Map.entry("twilightforest.item.LifedrainScepterItem", "casting"),
            Map.entry("twilightforest.item.ZombieWandItem", "summoning"),
            Map.entry("twilightforest.item.FortificationWandItem", "defensive"),
            Map.entry("twilightforest.item.ChainBlockItem", "throwable"),
            Map.entry("twilightforest.item.IceBombItem", "throwable"),
            Map.entry("twilightforest.item.CubeOfAnnihilationItem", "throwable"),
            Map.entry("twilightforest.item.MoonwormQueenItem", "custom_ranged"),
            Map.entry("twilightforest.item.LampOfCindersItem", "utility"),
            Map.entry("twilightforest.item.PeacockFanItem", "utility"));
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean started;
    private static int readyTicks;
    private record Subject(ItemStack stack, JsonObject metadata) {}
    private WeaponCatalogExport() {}

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        String output = System.getProperty("yagens_attributes.weaponCatalog");
        if (output == null || started) return;
        var client = Minecraft.getInstance();
        if (!(client.screen instanceof TitleScreen) || client.getOverlay() != null) return;
        if (++readyTicks < 20) return;
        started = true;
        try {
            var screen = new ExportScreen(Path.of(output), subjects());
            profiles(Path.of(output));
            client.setScreen(screen);
        }
        catch (Exception failure) { throw new IllegalStateException("Cannot inspect weapon registry fields", failure); }
    }

    private static void profiles(Path output) throws Exception {
        Class.forName("net.minecraft.server.level.ServerLevel");
        Class.forName("net.minecraft.world.item.ItemCooldowns");
        var definitions = yagen.waitmydawn.yagens_attributes.core.BuiltinCombat.weaponDefinitions();
        var exact = new LinkedHashMap<net.minecraft.resources.ResourceLocation, JsonElement>();
        for (String key : new TreeSet<>(definitions.keySet())) {
            var id = net.minecraft.resources.ResourceLocation.parse(key);
            try (var stream = WeaponCatalogExport.class.getResourceAsStream("/data/" + id.getNamespace() + "/weapon_stats/" + id.getPath() + ".json")) {
                if (stream == null) throw new IllegalStateException("Missing generated profile " + key);
                exact.put(id, JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)));
            }
        }
        var profiles = new WeaponProfiles();
        profiles.apply(new WeaponProfiles.Prepared(exact, Map.of()), null, net.minecraft.util.profiling.InactiveProfiler.INSTANCE);
        var values = new JsonObject();
        for (var id : exact.keySet()) {
            if (!BuiltInRegistries.ITEM.containsKey(id)) throw new IllegalStateException("Missing registered weapon " + id);
            var item = BuiltInRegistries.ITEM.get(id).getDefaultInstance();
            var profile = profiles.weapon(item);
            if (profile == null || !WeaponEligibility.operable(item, profile)) throw new IllegalStateException("Unresolved or inoperable weapon " + id);
            values.add(id.toString(), JsonData.weaponJson(profile));
        }
        Files.writeString(output.resolve("resolved-profiles.json"), JSON.toJson(values));
        YagensAttributes.LOGGER.info("Validated {} generated weapon profiles against loaded item registries and native baselines", values.size());
    }

    private static List<Subject> subjects() throws ReflectiveOperationException {
        var missing = REGISTRIES.keySet().stream().filter(id -> !ModList.get().isLoaded(id)).sorted().toList();
        if (!missing.isEmpty()) throw new IllegalStateException("Missing catalog mods: " + missing);
        var result = new ArrayList<Subject>();
        for (var registry : new TreeMap<>(REGISTRIES).entrySet()) {
            var seen = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
            var fields = Arrays.stream(Class.forName(registry.getValue()).getFields())
                    .sorted(Comparator.comparing(java.lang.reflect.Field::getName)).toList();
            for (var field : fields) {
                if (!Modifier.isStatic(field.getModifiers()) || !(field.get(null) instanceof Supplier<?> supplier)
                        || !(supplier.get() instanceof Item item) || !seen.add(item)) continue;
                ItemStack stack = item.getDefaultInstance();
                var attributes = new JsonArray();
                stack.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                    if (!attribute.equals(Attributes.ATTACK_DAMAGE) && !attribute.equals(Attributes.ATTACK_SPEED)) return;
                    var value = new JsonObject();
                    value.addProperty("attribute", attribute.getRegisteredName());
                    value.addProperty("amount", modifier.amount());
                    value.addProperty("operation", modifier.operation().getSerializedName());
                    attributes.add(value);
                });
                String role = null;
                for (Class<?> type = item.getClass(); type != null && role == null; type = type.getSuperclass())
                    role = SPECIAL_CLASSES.get(type.getName());
                var family = WeaponFamilies.match(item.getClass());
                boolean projectile = item instanceof ProjectileWeaponItem;
                if (role == null && item instanceof ShieldItem) role = "defensive";
                if (role == null && family.isEmpty() && item instanceof DiggerItem) role = "tool";
                if (family.isEmpty() && role == null && !projectile && attributes.isEmpty()) continue;
                var data = new JsonObject();
                String id = BuiltInRegistries.ITEM.getKey(item).toString();
                data.addProperty("id", id);
                data.addProperty("mod", registry.getKey());
                data.addProperty("version", ModList.get().getModContainerById(registry.getKey()).orElseThrow().getModInfo().getVersion().toString());
                data.addProperty("field", registry.getValue() + "." + field.getName());
                data.addProperty("name", stack.getHoverName().getString());
                data.addProperty("class", item.getClass().getName());
                data.addProperty("superclass", item.getClass().getSuperclass().getName());
                data.addProperty("default_family", family.map(Enum::name).orElse(""));
                data.addProperty("role", role == null ? projectile ? "ranged" : family.isPresent() ? "standard" : "unclassified_attack_item" : role);
                data.addProperty("projectile_weapon", projectile);
                data.addProperty("max_stack_size", stack.getMaxStackSize());
                data.add("attributes", attributes);
                data.addProperty("icon", id.replace(':', '/') + ".png");
                result.add(new Subject(stack, data));
            }
        }
        result.sort(Comparator.comparing(value -> value.metadata().get("id").getAsString()));
        return result;
    }

    private static final class ExportScreen extends Screen {
        private final Path output;
        private final List<Subject> subjects;
        private int index;
        private boolean finished;
        private boolean buffPreview;
        private ExportScreen(Path output, List<Subject> subjects) throws java.io.IOException {
            super(Component.literal("Weapon catalog export"));
            this.output = output; this.subjects = subjects;
            Files.createDirectories(output);
            Files.writeString(output.resolve("client.pid"), Long.toString(ProcessHandle.current().pid()));
        }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (finished) return;
            try {
                if (index >= subjects.size()) {
                    if (!buffPreview) {
                        yagen.waitmydawn.yagens_attributes.client.ClientMods.accept(new yagen.waitmydawn.yagens_attributes.network.ModCatalogPayload(
                                ModJson.GSON.toJson(ModJson.catalog(yagen.waitmydawn.yagens_attributes.core.BuiltinMods.catalog()))));
                        graphics.fill(0, 0, width, height, 0xFF15202B);
                        graphics.drawCenteredString(font, Component.translatable("gui.yagens_attributes.hud_buffs"), width / 2, 24, 0xFFE1CE90);
                        graphics.pose().pushPose(); graphics.pose().translate(40, 65, 0); graphics.pose().scale(3, 3, 1);
                        yagen.waitmydawn.yagens_attributes.client.WeaponBuffHud.preview(graphics);
                        graphics.pose().popPose(); graphics.flush();
                        try (var screenshot = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) { screenshot.writeToFile(output.resolve("weapon-buffs.png")); }
                        buffPreview = true; return;
                    }
                    var values = new JsonArray(); subjects.forEach(subject -> values.add(subject.metadata()));
                    Files.writeString(output.resolve("weapons.json"), JSON.toJson(values));
                    finished = true;
                    YagensAttributes.LOGGER.info("Exported {} native weapon icons to {}", subjects.size(), output);
                    minecraft.stop();
                    return;
                }
                var subject = subjects.get(index++);
                graphics.fill(0, 0, width, height, 0xFF15202B);
                int x = (width - 128) / 2, y = (height - 128) / 2;
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(8, 8, 1);
                graphics.renderFakeItem(subject.stack(), 0, 0);
                graphics.pose().popPose();
                graphics.flush();
                try (var screenshot = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                    double scale = (double) screenshot.getWidth() / width;
                    int size = (int) Math.round(128 * scale);
                    try (var icon = new NativeImage(size, size, false)) {
                        int left = (int) Math.round(x * scale), top = (int) Math.round(y * scale);
                        for (int row = 0; row < size; row++) for (int column = 0; column < size; column++)
                            icon.setPixelRGBA(column, row, screenshot.getPixelRGBA(left + column, top + row));
                        Path path = output.resolve(subject.metadata().get("icon").getAsString());
                        Files.createDirectories(path.getParent());
                        icon.writeToFile(path);
                    }
                }
            } catch (Exception failure) { throw new IllegalStateException("Cannot export weapon icon at index " + (index - 1), failure); }
        }
    }
}
