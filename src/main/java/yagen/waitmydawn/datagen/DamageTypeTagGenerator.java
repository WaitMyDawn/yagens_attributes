package yagen.waitmydawn.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.DamageTypeRegistry;

import java.util.concurrent.CompletableFuture;

public class DamageTypeTagGenerator extends TagsProvider<DamageType> {
    public DamageTypeTagGenerator(PackOutput output,
                                  CompletableFuture<HolderLookup.Provider> lookup,
                                  ExistingFileHelper helper) {
        super(output, Registries.DAMAGE_TYPE, lookup, YagensAttributes.MODID, helper);
    }

    private static TagKey<DamageType> create(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, name));
    }

    public static final TagKey<DamageType> SLASH_STATUS_DAMAGE_TYPE = create("slash_status");

    @Override
    protected void addTags(@NotNull Provider pProvider) {
        tag(SLASH_STATUS_DAMAGE_TYPE)
                .add(DamageTypeRegistry.SLASH_STATUS_DAMAGE_TYPE);

        tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(DamageTypeRegistry.SLASH_STATUS_DAMAGE_TYPE);

        tag(DamageTypeTags.BYPASSES_EFFECTS)
                .add(DamageTypeRegistry.SLASH_STATUS_DAMAGE_TYPE);

        tag(DamageTypeTags.BYPASSES_ENCHANTMENTS)
                .add(DamageTypeRegistry.SLASH_STATUS_DAMAGE_TYPE);
    }
}