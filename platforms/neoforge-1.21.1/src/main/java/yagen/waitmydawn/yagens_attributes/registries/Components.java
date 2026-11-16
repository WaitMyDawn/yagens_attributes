package yagen.waitmydawn.yagens_attributes.registries;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;

import java.util.UUID;
import java.util.function.Supplier;

import yagen.waitmydawn.yagens_attributes.data.ModItemData;
import yagen.waitmydawn.yagens_attributes.data.WeaponPolarityData;
import yagen.waitmydawn.yagens_attributes.data.RivenItemData;
import yagen.waitmydawn.yagens_attributes.core.RivenRoll;

public final class Components {
    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, YagensAttributes.MODID);
    public static final Supplier<DataComponentType<UUID>> WEAPON_ID = REGISTER.registerComponentType("weapon_id",
            builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));
    public static final Supplier<DataComponentType<Boolean>> ABSORB_EXPERIENCE = REGISTER.registerComponentType("absorb_experience",
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL));
    public static final Supplier<DataComponentType<Boolean>> CATALYZED = REGISTER.registerComponentType("catalyzed",
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL));
    public static final Supplier<DataComponentType<ModItemData>> MOD_DATA = REGISTER.registerComponentType("mod_data",
            builder -> builder.persistent(ModItemData.CODEC).networkSynchronized(ModItemData.STREAM_CODEC));
    public static final Supplier<DataComponentType<RivenRoll>> RIVEN_ROLL = REGISTER.registerComponentType("riven_roll",
            builder -> builder.persistent(RivenItemData.CODEC).networkSynchronized(RivenItemData.STREAM_CODEC));
    public static final Supplier<DataComponentType<WeaponPolarityData>> WEAPON_POLARITIES = REGISTER.registerComponentType("weapon_polarities",
            builder -> builder.persistent(WeaponPolarityData.CODEC).networkSynchronized(WeaponPolarityData.STREAM_CODEC));

    private Components() {
    }
    public static boolean absorbsExperience(ItemStack stack) { return stack.getOrDefault(ABSORB_EXPERIENCE.get(), false); }
    public static int capacity(ItemStack stack, yagen.waitmydawn.yagens_attributes.core.WeaponProgress progress) { return progress.level() * (stack.getOrDefault(CATALYZED.get(), false) ? 2 : 1); }

    public static UUID identify(ItemStack stack) {
        if (stack.getCount() != 1 || stack.getMaxStackSize() != 1)
            throw new IllegalArgumentException("Weapon must be non-stackable");
        UUID id = stack.get(WEAPON_ID.get());
        if (id == null) {
            id = UUID.randomUUID();
            stack.set(WEAPON_ID.get(), id);
        }
        return id;
    }
}
