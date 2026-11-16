package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.util.*;

/** Shield vulnerability is global and independent of the entity's selected life material. */
public final class ShieldMaterials extends SimpleJsonResourceReloadListener {
    public static final ShieldMaterials INSTANCE = new ShieldMaterials();
    private Map<DamageType, Double> multipliers = Map.of();
    private ShieldMaterials() { super(new Gson(), "shield_materials"); }
    @Override protected void apply(Map<ResourceLocation, JsonElement> documents, ResourceManager manager, ProfilerFiller profiler) {
        if (documents.size() != 1) throw new IllegalArgumentException("Expected one shield material profile");
        var object = documents.values().iterator().next().getAsJsonObject();
        JsonData.fields(object, "multipliers", "description");
        var prepared = JsonData.damage(object.getAsJsonObject("multipliers"));
        multipliers = Map.copyOf(prepared);
    }
    public Map<DamageType, Double> multipliers() { return multipliers; }
}
