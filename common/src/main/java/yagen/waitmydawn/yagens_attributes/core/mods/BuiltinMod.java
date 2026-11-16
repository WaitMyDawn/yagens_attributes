package yagen.waitmydawn.yagens_attributes.core.mods;

import java.util.List;
import java.util.Set;
import yagen.waitmydawn.yagens_attributes.core.*;

public abstract class BuiltinMod {
    private final String path;
    protected BuiltinMod(String path) { this.path = path; ModDefinition.id(id()); }
    public final String id() { return "yagens_attributes:" + path; }
    public final String name() { return "mod.yagens_attributes." + path; }
    public abstract ModDefinition definition();
    protected final ModDefinition define(ModInfo.Rarity rarity, ModInfo.Polarity polarity, int capacity, int maxLevel,
                                         Set<ModInfo.Category> scope, String texture, ModDefinition.Effect... entries) {
        return new ModDefinition(name(), rarity, polarity, capacity, 1, maxLevel, scope, texture, List.of(entries));
    }
}
