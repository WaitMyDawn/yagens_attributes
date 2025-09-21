package yagen.waitmydawn.api.mods;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import yagen.waitmydawn.api.registry.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import yagen.waitmydawn.registries.ItemRegistry;
import yagen.waitmydawn.util.MinecraftInstanceHelper;

import java.util.Objects;

public class ModData implements Comparable<ModData> {
    public static final String MOD_ID = "id";
    public static final String MOD_LEVEL = "level";

    public static final Codec<ModData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf(MOD_ID).forGetter(data -> data.mod.getModResource()),
            Codec.INT.fieldOf(MOD_LEVEL).forGetter(ModData::getLevel)
    ).apply(builder, ModData::new));

    public static final ModData EMPTY = new ModData(ModRegistry.none(), 0);

    protected final AbstractMod mod;
    protected final int modLevel;

//    protected final ResourceLocation modId;
//    protected final int level;

    private ModData() throws Exception {
        throw new Exception("Cannot create empty mod slots.");
    }

    //    public ModData(ResourceLocation modId, int level) {
//        this.modId = modId;
//        this.level = level;
//    }
    public ModData(AbstractMod mod, int level) {
        this.mod = Objects.requireNonNull(mod);
        this.modLevel = level;
    }

//    public ModData(AbstractMod mod, int level) {
//        this(mod, level);
//    }

    public ModData(ResourceLocation modId, int level) {
        this(ModRegistry.getMod(modId), level);
    }


    public static void writeToBuffer(FriendlyByteBuf buf, ModData data) {
        buf.writeResourceLocation(data.mod.getModResource());
        buf.writeInt(data.modLevel);
    }

    public static ModData readFromBuffer(FriendlyByteBuf buf) {
        return new ModData(buf.readResourceLocation(), buf.readInt());
    }

    public AbstractMod getMod() {
        return mod == null ? ModRegistry.none() : mod;
    }

    public int getLevel() {
        return modLevel;
    }


    public boolean canRemove() {
        return true;
    }

    public ModRarity getRarity() {
        return getMod().getRarity();
    }

    public Component getDisplayName() {
        return getMod().getDisplayName(MinecraftInstanceHelper.instance.player()).append(" ").append(Component.translatable(ItemRegistry.MOD.get().getDescriptionId()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ModData other) {
            return this.mod.equals(other.mod) && this.modLevel == other.modLevel;
        }

        return false;
    }

    public int hashCode() {
        return 31 * this.mod.hashCode() + this.modLevel;
    }

    public int compareTo(ModData other) {
        int i = this.mod.getModId().compareTo(other.mod.getModId());
        if (i == 0) {
            i = Integer.compare(this.modLevel, other.modLevel);
        }
        return i;
    }
}