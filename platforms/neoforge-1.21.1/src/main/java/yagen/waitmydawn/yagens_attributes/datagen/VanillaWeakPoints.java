package yagen.waitmydawn.yagens_attributes.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import yagen.waitmydawn.yagens_attributes.core.WeakPointRegion;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static yagen.waitmydawn.yagens_attributes.core.WeakPointRegion.Anchor.EYES;
import static yagen.waitmydawn.yagens_attributes.core.WeakPointRegion.Anchor.FEET;
import static yagen.waitmydawn.yagens_attributes.core.WeakPointRegion.Rotation.BODY;
import static yagen.waitmydawn.yagens_attributes.core.WeakPointRegion.Rotation.HEAD;
import static yagen.waitmydawn.yagens_attributes.core.WeakPointRegion.Rotation.NONE;
import static yagen.waitmydawn.yagens_attributes.core.WeakPointRegion.State.*;

/** Authored against the adult vanilla models and their colliders. Membership is explicit. */
final class VanillaWeakPoints {
    private VanillaWeakPoints() {}

    static Map<String, JsonObject> files() {
        var files = new TreeMap<String, JsonObject>();
        var humanoid = region("head", EYES, NONE, -.50, -.13, -.50, .50, .14, .50);
        var mobTag = new JsonObject();
        mobTag.addProperty("replace", false);
        mobTag.add("values", vanillaIds("bogged", "drowned", "enderman", "giant", "husk", "piglin", "piglin_brute", "player",
                "skeleton", "stray", "villager", "wither_skeleton", "zombie", "zombie_villager", "zombified_piglin"));
        files.put("data/yagens_attributes/tags/entity_type/weak_points/humanoid.json", mobTag);
        var humanoidTemplate = profile(humanoid);
        humanoidTemplate.add("entity_tags", list("yagens_attributes:weak_points/humanoid"));
        files.put(templatePath("humanoid"), humanoidTemplate);

        var tallFace = region("head", EYES, NONE, -.50, -.13, -.50, .50, .20, .50);
        group(files, "tall_humanoid_head", tallFace, "evoker", "illusioner", "pillager", "vindicator", "wandering_trader", "witch");
        exact(files, "villager", region("head", EYES, NONE, -.50, -.10, -.50, .50, .19, .50));
        group(files, "spider", head(.20, .34, .93, .13, .74), "spider", "cave_spider");
        exact(files, "cave_spider", head(.26, .43, 1.14, .17, .74));
        group(files, "cattle", head(.36, .69, 1.13, .39, 1.00), "cow", "mooshroom");
        group(files, "hoglin", head(.33, .25, 1.08, .38, 1.20), "hoglin", "zoglin");
        group(files, "llama", head(.30, .86, 1.12, .28, 1.17), "llama", "trader_llama");
        group(files, "pack_equine", head(.19, .85, 1.17, .35, .84), "donkey", "mule");
        group(files, "horse_neck", head(.21, .86, 1.00, .32, .81), "horse", "skeleton_horse", "zombie_horse");
        group(files, "guardian_eye", region("eye", FEET, BODY, -.24, .40, .43, .24, .63, .74), "guardian", "elder_guardian");

        exact(files, "ghast",
                region("left_eye", EYES, BODY, -.36, -.055, .44, -.065, .055, .68),
                region("right_eye", EYES, BODY, .065, -.055, .44, .36, .055, .68));
        exact(files, "ender_dragon", new WeakPointRegion("head", FEET, NONE,
                new WeakPointRegion.Point(-.5, 0, -.5), new WeakPointRegion.Point(.5, 1, .5),
                ALWAYS, WeakPointRegion.Part.DRAGON_HEAD));
        exact(files, "wither", centeredHead(.71, 1.01));
        exact(files, "strider", centeredHead(.51, 1.03));
        exact(files, "squid", squidEye("left_eye", -.33, -.14, .36, .55), squidEye("right_eye", .14, .33, .36, .55));
        exact(files, "glow_squid", squidEye("left_eye", -.41, -.15, .30, .55), squidEye("right_eye", .15, .41, .30, .55));
        exact(files, "goat", state(region("head_up", FEET, HEAD, -.34, .47, .20, .34, 1.07, .98), GOAT_HEAD_UP),
                state(region("head_down", FEET, HEAD, -.34, .22, .30, .34, .75, 1.08), GOAT_HEAD_DOWN));
        exact(files, "pufferfish", stateHead("face_small", PUFFERFISH_SMALL, .29, .19, .77, .18, .72),
                stateHead("face_medium", PUFFERFISH_MEDIUM, .34, .40, .90, .22, .72),
                stateHead("face_full", PUFFERFISH_FULL, .38, .43, .89, .23, .72));
        exact(files, "shulker", state(region("exposed_head", FEET, NONE, -.20, .50, -.20, .20, .76, .20), SHULKER_EXPOSED));
        exact(files, "armadillo", stateHead("exposed_head", ARMADILLO_EXPOSED, .18, .08, .65, .36, .90));
        exact(files, "bat", stateHead("flying_head", BAT_FLYING, .27, .47, .74, -.13, .72),
                stateHead("hanging_head", BAT_HANGING, .27, .30, .55, -.13, .72));

        exact(files, "allay", centeredHead(.43, .97));
        exact(files, "axolotl", head(.36, .30, 1.05, .30, .76));
        exact(files, "bee", head(.34, .24, 1.04, .23, .62));
        exact(files, "blaze", centeredHead(.69, .98));
        exact(files, "breeze", centeredHead(.69, .99));
        exact(files, "cat", head(.24, .49, .88, .37, 1.10));
        exact(files, "camel", head(.20, .91, 1.12, .37, .85));
        exact(files, "chicken", head(.32, .79, 1.33, .37, .94));
        exact(files, "cod", head(.18, .02, .83, .16, .62));
        exact(files, "creeper", centeredHead(.66, .96));
        exact(files, "dolphin", head(.28, .21, .95, .32, 1.06));
        exact(files, "endermite", head(.34, .02, .64, .30, .73));
        exact(files, "fox", head(.43, .30, .86, .21, .85));
        exact(files, "frog", head(.45, .39, 1.00, .12, .62));
        exact(files, "iron_golem", head(.23, .74, 1.00, -.10, .62));
        exact(files, "ocelot", head(.28, .63, 1.07, .38, 1.36));
        exact(files, "panda", head(.42, .37, 1.04, .39, 1.12));
        exact(files, "parrot", head(.17, .46, .82, .16, .64));
        exact(files, "phantom", head(.29, .13, .60, .26, .66));
        exact(files, "pig", head(.30, .55, 1.11, .37, 1.05));
        exact(files, "polar_bear", head(.25, .53, .98, .39, 1.19));
        exact(files, "rabbit", head(.26, .59, .94, .12, .65));
        exact(files, "ravager", head(.27, .36, 1.00, .29, .97));
        exact(files, "salmon", head(.14, .29, .94, .08, .61));
        exact(files, "sheep", head(.22, .76, 1.07, .37, .98));
        exact(files, "silverfish", head(.36, .02, .45, .26, .77));
        exact(files, "sniffer", head(.23, .12, .72, .38, 1.32));
        exact(files, "snow_golem", centeredHead(.64, 1.00));
        exact(files, "tadpole", head(.25, .19, .66, .18, .60));
        exact(files, "tropical_fish", head(.23, .08, .84, .21, .63));
        exact(files, "turtle", head(.17, .15, .96, .34, .71));
        exact(files, "vex", centeredHead(.50, .91));
        exact(files, "warden", centeredHead(.72, 1.08));
        exact(files, "wolf", head(.34, .55, 1.15, .37, 1.26));
        return files;
    }

    private static void exact(Map<String, JsonObject> files, String entity, WeakPointRegion... regions) {
        files.put("data/minecraft/weak_points/entities/" + entity + ".json", profile(regions));
    }

    private static void group(Map<String, JsonObject> files, String template, WeakPointRegion region, String... entities) {
        var profile = profile(region);
        profile.add("entity_types", vanillaIds(entities));
        files.put(templatePath(template), profile);
    }

    private static String templatePath(String template) {
        return "data/yagens_attributes/weak_points/templates/" + template + ".json";
    }

    private static JsonObject profile(WeakPointRegion... regions) {
        var map = new JsonObject();
        for (var region : regions) {
            var object = new JsonObject();
            object.addProperty("anchor", region.anchor().name().toLowerCase(Locale.ROOT));
            object.addProperty("rotation", region.rotation().name().toLowerCase(Locale.ROOT));
            if (region.state() != ALWAYS) object.addProperty("state", region.state().name().toLowerCase(Locale.ROOT));
            if (region.part() != WeakPointRegion.Part.BODY) object.addProperty("part", region.part().name().toLowerCase(Locale.ROOT));
            object.add("min", point(region.min()));
            object.add("max", point(region.max()));
            map.add(region.id(), object);
        }
        var profile = new JsonObject();
        profile.add("regions", map);
        return profile;
    }

    private static JsonArray point(WeakPointRegion.Point point) {
        var array = new JsonArray();
        array.add(point.x()); array.add(point.y()); array.add(point.z());
        return array;
    }

    private static JsonArray vanillaIds(String... names) {
        var array = new JsonArray();
        for (String name : names) array.add("minecraft:" + name);
        return array;
    }

    private static JsonArray list(String... values) {
        var array = new JsonArray();
        for (String value : values) array.add(value);
        return array;
    }

    private static WeakPointRegion centeredHead(double minY, double maxY) {
        return region("head", FEET, NONE, -.50, minY, -.50, .50, maxY, .50);
    }

    private static WeakPointRegion head(double halfWidth, double minY, double maxY, double minZ, double maxZ) {
        // The first surface of a square collider can reach sqrt(0.5) along the local forward axis at a diagonal yaw.
        return region("head", FEET, BODY, -halfWidth, minY, minZ, halfWidth, maxY, Math.max(.72, maxZ));
    }

    private static WeakPointRegion stateHead(String id, WeakPointRegion.State state,
                                             double halfWidth, double minY, double maxY, double minZ, double maxZ) {
        return state(region(id, FEET, BODY, -halfWidth, minY, minZ, halfWidth, maxY, maxZ), state);
    }

    private static WeakPointRegion squidEye(String id, double minX, double maxX, double minY, double maxY) {
        return state(region(id, FEET, NONE, minX, minY, .43, maxX, maxY, .70), SQUID_MODEL);
    }

    private static WeakPointRegion state(WeakPointRegion region, WeakPointRegion.State state) {
        return new WeakPointRegion(region.id(), region.anchor(), region.rotation(), region.min(), region.max(), state, region.part());
    }

    private static WeakPointRegion region(String id, WeakPointRegion.Anchor anchor, WeakPointRegion.Rotation rotation,
                                          double x0, double y0, double z0, double x1, double y1, double z1) {
        return new WeakPointRegion(id, anchor, rotation, new WeakPointRegion.Point(x0, y0, z0), new WeakPointRegion.Point(x1, y1, z1));
    }
}
