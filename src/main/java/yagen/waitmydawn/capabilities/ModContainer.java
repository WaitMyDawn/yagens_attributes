package yagen.waitmydawn.capabilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.mods.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.registries.ComponentRegistry;

import java.util.*;
import java.util.stream.Collectors;

public class ModContainer implements IModContainer {
    //Container Root
    public static final String MOD_SLOT_CONTAINER = "YA_Mods";
    public static final String MOD_DATA = "data";
    public static final String MAX_SLOTS = "maxMods";
    public static final String MUST_EQUIP = "mustEquip";
    public static final String IMPROVED = "improved";

    //Slot Data
    public static final String SLOT_INDEX = "index";
    public static final String MOD_ID = "id";
    public static final String MOD_LEVEL = "level";

    ModSlot[] slots;
    int maxMods = 0;
    int activeSlots = 0;
    boolean modWheel = false;
    boolean mustEquip = true;
    boolean improved = false;

//    public ModContainer(
//            int maxMods,
//            boolean modWheel,
//            boolean mustEquip,
//            boolean improved,
//            ModSlot[] slots,
//            ComponentRegistry.DefaultAttributeProfile defaultAttrProfile) {
//        this.maxMods = maxMods;
//        this.slots = slots;
//        this.modWheel = modWheel;
//        this.mustEquip = mustEquip;
//        this.improved = improved;
//        this.activeSlots = Arrays.stream(slots).filter(Objects::nonNull).toList().size();
//        this.defaultAttributeProfile = defaultAttrProfile;
//    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof ModContainer o &&
                Arrays.equals(o.slots, this.slots) &&
                this.maxMods == o.maxMods &&
                this.activeSlots == o.activeSlots &&
                this.modWheel == o.modWheel &&
                this.mustEquip == o.mustEquip &&
                this.improved == o.improved
        );
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(this.slots);
        hash = (hash * 31 + maxMods) * 31 + activeSlots;
        hash *= 1000;
        hash += modWheel ? 100 : 0;
        hash += mustEquip ? 10 : 0;
        hash += improved ? 1 : 0;
        return hash;
    }

    public Map<DamageType, Float> damageProfile = new EnumMap<>(DamageType.class);

    @Override
    public Map<DamageType, Float> getDamageProfile() {
        return damageProfile;
    }

    @Override
    public void setDamageProfile(Map<DamageType, Float> map) {
        damageProfile.clear();
        damageProfile.putAll(map);
    }

    public ComponentRegistry.AttributeProfile attributeProfile =
            new ComponentRegistry.AttributeProfile(Map.of());

    @Override
    public ComponentRegistry.AttributeProfile getAttributeProfile() {
        return attributeProfile;
    }

    @Override
    public void setAttributeProfile(ComponentRegistry.AttributeProfile profile) {
        this.attributeProfile = profile;
    }

//    public ComponentRegistry.DefaultAttributeProfile defaultAttributeProfile =
//            new ComponentRegistry.DefaultAttributeProfile(Map.of());
//
//    @Override
//    public ComponentRegistry.DefaultAttributeProfile getDefaultAttributeProfile() {
//        return defaultAttributeProfile;
//    }
//
//    @Override
//    public void setDefaultAttributeProfile(ComponentRegistry.DefaultAttributeProfile profile) {
//        this.defaultAttributeProfile = profile;
//    }

    //Codec<List<ModData>> MOD_LIST_CODEC = Codec.list(ModData.CODEC);
    public static final Codec<ModSlot> MOD_SLOT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf(MOD_ID).forGetter(data -> data.getMod().getModResource()),
            Codec.INT.fieldOf(SLOT_INDEX).forGetter(ModSlot::index),
            Codec.INT.fieldOf(MOD_LEVEL).forGetter(ModSlot::getLevel)
            //Codec.BOOL.optionalFieldOf(MOD_LOCKED, false).forGetter(ModSlot::isLocked)
    ).apply(builder, (id, index, lvl) -> ModSlot.of(new ModData(id, lvl), index)));

    public static final Codec<IModContainer> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf(MAX_SLOTS).forGetter(IModContainer::getMaxModCount),
            //Codec.BOOL.fieldOf(MOD_WHEEL).forGetter(IModContainer::isModWheel), // change 3
            Codec.BOOL.fieldOf(MUST_EQUIP).forGetter(IModContainer::mustEquip),
            Codec.BOOL.optionalFieldOf(IMPROVED, false).forGetter(IModContainer::isImproved),
            Codec.list(MOD_SLOT_CODEC).fieldOf(MOD_DATA).forGetter(IModContainer::getActiveMods),

            Codec.unboundedMap(DamageType.CODEC, Codec.FLOAT)
                    .optionalFieldOf("damage_profile", Map.of())
                    .forGetter(IModContainer::getDamageProfile),
            ComponentRegistry.AttributeProfile.CODEC
                    .optionalFieldOf("attributes_profile",
                            new ComponentRegistry.AttributeProfile(Map.of()))
                    .forGetter(IModContainer::getAttributeProfile)
//            ComponentRegistry.DefaultAttributeProfile.CODEC
//                    .optionalFieldOf("default_attributes_profile",
//                            new ComponentRegistry.DefaultAttributeProfile(Map.of()))
//                    .forGetter(IModContainer::getDefaultAttributeProfile)
    ).apply(builder, (count, equip, improved, mods, damageProfile, attributeProfile) -> {
        var container = new ModContainer(count, equip, improved);
        mods.forEach(slot -> container.slots[slot.index()] = slot);
        container.activeSlots = mods.size();
        container.setDamageProfile(damageProfile);
        container.setAttributeProfile(attributeProfile);
//        container.setDefaultAttributeProfile(defaultAttributeProfile);
        return container;
    }));

    public static final StreamCodec<FriendlyByteBuf, IModContainer> STREAM_CODEC = StreamCodec.of((buf, container) -> {
        buf.writeInt(container.getMaxModCount());
        buf.writeBoolean(container.isModWheel());
        buf.writeBoolean(container.mustEquip());
        buf.writeBoolean(container.isImproved());
        var mods = container.getActiveMods();
        int i = mods.size();
        buf.writeInt(i);
        for (int j = 0; j < i; j++) {
            var mod = mods.get(j);
            ModData.writeToBuffer(buf, mod.modData());
            buf.writeInt(mod.index());
        }

        var dmg = container.getDamageProfile();
        buf.writeInt(dmg.size());
        dmg.forEach((type, val) -> {
            buf.writeUtf(type.name());
            buf.writeFloat(val);
        });
        // new
        var attrs = container.getAttributeProfile().map();
        buf.writeInt(attrs.size());
        attrs.forEach((attr, list) -> {
            buf.writeUtf(BuiltInRegistries.ATTRIBUTE.getKey(attr).toString());
            buf.writeInt(list.size());
            list.forEach(mod -> AttributeModifier.STREAM_CODEC.encode(buf, mod));
        });

//        var defAttr = container.getDefaultAttributeProfile();
//        System.out.println("encode getDefaultAttributeProfile .map(): " + defAttr.map());
//        System.out.println("encode getDefaultAttributeProfile : " + defAttr);
//        ComponentRegistry.DefaultAttributeProfile.STREAM_CODEC.encode(buf, defAttr);
    }, (buf) -> {
        var count = buf.readInt();
        var wheel = buf.readBoolean();
        var equip = buf.readBoolean();
        var improved = buf.readBoolean();
        int i = buf.readInt();

        var container = new ModContainer(count, wheel, equip, improved);
        for (int j = 0; j < i; j++) {
            var mod = new ModSlot(ModData.readFromBuffer(buf), buf.readInt());
            container.slots[mod.index()] = mod;
        }
        container.activeSlots = i;

        int dmgCnt = buf.readInt();
        Map<DamageType, Float> dmg = new EnumMap<>(DamageType.class);
        for (int k = 0; k < dmgCnt; k++) {
            dmg.put(DamageType.valueOf(buf.readUtf()), buf.readFloat());
        }
        container.damageProfile = dmg;
        // new
        int attrCnt = buf.readInt();
        Map<Attribute, List<AttributeModifier>> attrMap = new HashMap<>();
        for (int k = 0; k < attrCnt; k++) {
            ResourceLocation key = ResourceLocation.tryParse(buf.readUtf());
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(key);
            int cnt = buf.readInt();
            List<AttributeModifier> list = new ArrayList<>(cnt);
            for (int j = 0; j < cnt; j++) {
                list.add(AttributeModifier.STREAM_CODEC.decode(buf));
            }
            if (attr != null) attrMap.put(attr, list);
        }
        //container.attributeProfile = new ComponentRegistry.AttributeProfile(attrMap);
        container.setAttributeProfile(new ComponentRegistry.AttributeProfile(attrMap));

//        ComponentRegistry.DefaultAttributeProfile defAttr = ComponentRegistry.DefaultAttributeProfile.STREAM_CODEC.decode(buf);
//        container.setDefaultAttributeProfile(defAttr);
//        System.out.println("Read default attributes .map(): " + defAttr.map());
//        System.out.println("Read default attributes: " + defAttr);
        return container;
    });

    public ModContainer() {
    }

    public ModContainer(int maxMods, boolean modWheel, boolean mustEquip) {
        this(maxMods, modWheel, mustEquip, false);
    }

    public ModContainer(int maxMods, boolean modWheel, boolean mustEquip, boolean improved) {
        this.maxMods = maxMods;
        this.slots = new ModSlot[this.maxMods];
        this.modWheel = modWheel;
        this.mustEquip = mustEquip;
        this.improved = improved;
    }

    public ModContainer(int maxMods, boolean modWheel, boolean mustEquip, boolean improved, ModSlot[] slots) {
        this.maxMods = maxMods;
        this.slots = slots;
        this.modWheel = modWheel;
        this.mustEquip = mustEquip;
        this.improved = improved;
        this.activeSlots = Arrays.stream(slots).filter(Objects::nonNull).toList().size();
    }

    @Override
    public int getMaxModCount() {
        return maxMods;
    }


    @Override
    public int getActiveModCount() {
        return activeSlots;
    }

    @Override
    public boolean isEmpty() {
        return activeSlots == 0;
    }

    @Override
    public ModSlot[] getAllMods() {
        var result = new ModSlot[maxMods];
        if (maxMods > 0) {
            System.arraycopy(slots, 0, result, 0, slots.length);
        }
        return result;
    }

    @Override
    public @NotNull List<ModSlot> getActiveMods() {
        return Arrays.stream(this.slots).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public int getNextAvailableIndex() {
        return ArrayUtils.indexOf(this.slots, null);
    }

    @Override
    public boolean mustEquip() {
        return mustEquip;
    }

    @Override
    public boolean isImproved() {
        return improved;
    }

    @Override
    public boolean isModWheel() {
        return modWheel;
    }

    @Override
    public @NotNull ModData getModAtIndex(int index) {
        if (index >= 0 && index < maxMods) {
            var result = slots[index];
            if (result != null) {
                return slots[index].modData();
            }
        }
        return ModData.EMPTY;
    }

    @Override
    public int getIndexForMod(AbstractMod mod) {
        for (int i = 0; i < maxMods; i++) {
            var s = slots[i];

            if (s != null && s.getMod().equals(mod)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public IModContainerMutable mutableCopy() {
        return new Mutable(this);
    }

    public class Mutable extends ModContainer implements IModContainerMutable {
//        private ModSlot[] slots;
//        private int maxMods = 0;
//        private int activeSlots = 0;
//        private boolean modWheel = false;
//        private boolean mustEquip = true;
//        private boolean improved = false;

        public Mutable(ModContainer modContainer) {
            this.maxMods = modContainer.maxMods;
            this.activeSlots = modContainer.activeSlots;
            this.modWheel = modContainer.modWheel;
            this.mustEquip = modContainer.mustEquip;
            this.improved = modContainer.improved;
            this.slots = Arrays.copyOf(modContainer.slots, modContainer.slots.length);
        }

        @Override
        public void setMaxModCount(int maxMods) {
            this.maxMods = maxMods;
            slots = Arrays.copyOf(slots, maxMods);
        }

        @Override
        public void setImproved(boolean improved) {
            this.improved = improved;
        }

        @Override
        public boolean addModAtIndex(AbstractMod mod, int level, int index) {
            if (index > -1 && index < maxMods &&
                    slots[index] == null &&
                    Arrays.stream(slots).noneMatch(s -> s != null && s.getMod().equals(mod))) {
                slots[index] = ModSlot.of(new ModData(mod, level), index);
                activeSlots++;
                return true;
            }
            return false;
        }

        @Override
        public boolean addMod(AbstractMod mod, int level) {
            return addModAtIndex(mod, level, getNextAvailableIndex());
        }

        @Override
        public boolean removeModAtIndex(int index) {
            if (index > -1 && index < maxMods && slots[index] != null) {
                slots[index] = null;
                activeSlots--;
                return true;
            }
            return false;
        }

        @Override
        public boolean removeMod(AbstractMod mod) {
            if (mod == null) {
                return false;
            }

            for (int i = 0; i < maxMods; i++) {
                var modData = slots[i];
                if (modData != null && mod.equals(modData.getMod())) {
                    return removeModAtIndex(i);
                }
                break;
            }
            return false;
        }

        @Override
        public IModContainer toImmutable() {
            return new ModContainer(this.maxMods, this.modWheel, this.mustEquip, this.improved, this.slots);
        }
    }
}
