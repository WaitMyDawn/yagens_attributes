package yagen.waitmydawn.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.mods.IModContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.capabilities.ModContainer;

import java.util.*;
import java.util.function.UnaryOperator;

public class ComponentRegistry {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, YagensAttributes.MODID);

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String pName, UnaryOperator<DataComponentType.Builder<T>> pBuilder) {
        return COMPONENTS.register(pName, () -> pBuilder.apply(DataComponentType.builder()).build());
    }

    public record DamageProfile(Map<DamageType, Float> map) {

        public static final Codec<DamageProfile> CODEC =
                Codec.unboundedMap(DamageType.CODEC, Codec.FLOAT)
                        .xmap(DamageProfile::new, DamageProfile::map);

        public static final StreamCodec<ByteBuf, DamageProfile> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public void encode(ByteBuf buf, DamageProfile profile) {
                        Map<DamageType, Float> map = profile.map();
                        buf.writeInt(map.size());
                        map.forEach((type, value) -> {
                            DamageType.STREAM_CODEC.encode(buf, type);
                            buf.writeFloat(value);
                        });
                    }

                    @Override
                    public DamageProfile decode(ByteBuf buf) {
                        int size = buf.readInt();
                        Map<DamageType, Float> map = new HashMap<>();
                        for (int i = 0; i < size; i++) {
                            DamageType type = DamageType.STREAM_CODEC.decode(buf);
                            float value = buf.readFloat();
                            map.put(type, value);
                        }
                        return new DamageProfile(map);
                    }
                };
    }

    public record AttributeProfile(Map<Attribute, List<AttributeModifier>> map) {

        public static final Codec<AttributeProfile> CODEC =
                Codec.unboundedMap(
                        BuiltInRegistries.ATTRIBUTE.byNameCodec(),
                        AttributeModifier.CODEC.listOf()
                ).xmap(AttributeProfile::new, AttributeProfile::map);

        public static final StreamCodec<FriendlyByteBuf, AttributeProfile> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public void encode(FriendlyByteBuf buf, AttributeProfile prof) {
                        Map<Attribute, List<AttributeModifier>> m = prof.map();
                        buf.writeInt(m.size());
                        m.forEach((attr, list) -> {
                            buf.writeUtf(BuiltInRegistries.ATTRIBUTE.getKey(attr).toString());
                            buf.writeInt(list.size());
                            list.forEach(mod -> AttributeModifier.STREAM_CODEC.encode(buf, mod));
                        });
                    }

                    @Override
                    public AttributeProfile decode(FriendlyByteBuf buf) {
                        int n = buf.readInt();
                        Map<Attribute, List<AttributeModifier>> map = new HashMap<>();
                        for (int i = 0; i < n; i++) {
                            Attribute attr = BuiltInRegistries.ATTRIBUTE
                                    .get(ResourceLocation.tryParse(buf.readUtf()));
                            int cnt = buf.readInt();
                            List<AttributeModifier> list = new ArrayList<>(cnt);
                            for (int j = 0; j < cnt; j++) {
                                list.add(AttributeModifier.STREAM_CODEC.decode(buf));
                            }
                            if (attr != null) map.put(attr, list);
                        }
                        return new AttributeProfile(map);
                    }
                };
    }

    public record DefaultItemAttributes(ItemAttributeModifiers modifiers) {
        public static final Codec<DefaultItemAttributes> CODEC =
                ItemAttributeModifiers.CODEC.xmap(DefaultItemAttributes::new, DefaultItemAttributes::modifiers);

        public static final StreamCodec<RegistryFriendlyByteBuf, DefaultItemAttributes> STREAM_CODEC =
                ItemAttributeModifiers.STREAM_CODEC.map(DefaultItemAttributes::new, DefaultItemAttributes::modifiers);
    }

    public record UpgradeData(
            int exp,
            int polarity,
            int level
    ) {
        public static final Codec<UpgradeData> CODEC = RecordCodecBuilder.create(
                b -> b.group(
                        Codec.INT.fieldOf("exp").forGetter(UpgradeData::exp),
                        Codec.INT.fieldOf("polarity").forGetter(UpgradeData::polarity),
                        Codec.INT.fieldOf("level").forGetter(UpgradeData::level)
                ).apply(b, UpgradeData::new)
        );

        public static final StreamCodec<ByteBuf, UpgradeData> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, UpgradeData::exp,
                        ByteBufCodecs.INT, UpgradeData::polarity,
                        ByteBufCodecs.INT, UpgradeData::level,
                        UpgradeData::new
                );

        public UpgradeData withExp(int delta) {
            return new UpgradeData(exp + delta, polarity, level);
        }

        public UpgradeData withLevel(int newLevel, int newExp) {
            return new UpgradeData(newExp, polarity, newLevel);
        }

        public UpgradeData withPolarity(int delta) {
            return new UpgradeData(0, polarity + delta, 1);
        }

        public int nextLevelExpNeed() {
            return (7 + 2 * level) * (1 + polarity * polarity);
        }
    }

    public record EndoInfo(int level, String missionType) {
        public static final Codec<EndoInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("level").forGetter(EndoInfo::level),
                Codec.STRING.fieldOf("missionType").forGetter(EndoInfo::missionType)
        ).apply(i, EndoInfo::new));

        public static final StreamCodec<ByteBuf, EndoInfo> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, EndoInfo::level,
                        ByteBufCodecs.STRING_UTF8, EndoInfo::missionType,
                        EndoInfo::new
                );

        public static final EndoInfo EMPTY = new EndoInfo(0, "NonType");

        public EndoInfo withLevel(int newLevel) {
            return newLevel == level ? this : new EndoInfo(level, missionType);
        }

        public EndoInfo withMissionType(String newMissionType) {
            return Objects.equals(newMissionType, missionType) ? this : new EndoInfo(level, missionType);
        }

        public EndoInfo addLevel() {
            return new EndoInfo(level + 1, missionType);
        }
    }

    public record StringListData(List<String> strings) {
        public static final Codec<StringListData> CODEC = RecordCodecBuilder.create(
                b -> b.group(
                        Codec.STRING.listOf().fieldOf("strings").forGetter(StringListData::strings)
                ).apply(b, StringListData::new)
        );

        public static final StreamCodec<ByteBuf, StringListData> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                        StringListData::strings,
                        StringListData::new
                );

        public StringListData withAdded(String s) {
            List<String> newList = new ArrayList<>(strings);
            newList.add(s);
            return new StringListData(newList);
        }

        public StringListData withCleared() {
            return new StringListData(List.of());
        }
    }

    public record RivenRawInfoList(List<RivenRawInfo> raw) {

        public record RivenRawInfo(String key, double base) {
        }

        private static final Codec<RivenRawInfo> RAW_CODEC =
                RecordCodecBuilder.create(b -> b.group(
                        Codec.STRING.fieldOf("key").forGetter(RivenRawInfo::key),
                        Codec.DOUBLE.fieldOf("base").forGetter(RivenRawInfo::base)
                ).apply(b, RivenRawInfo::new));

        public static final Codec<RivenRawInfoList> CODEC =
                RAW_CODEC.listOf().xmap(RivenRawInfoList::new, RivenRawInfoList::raw);

        public static final StreamCodec<FriendlyByteBuf, RivenRawInfoList> STREAM_CODEC =
                StreamCodec.of(
                        (buf, list) -> {
                            buf.writeInt(list.raw.size());
                            list.raw.forEach(r -> {
                                buf.writeUtf(r.key());
                                buf.writeDouble(r.base());
                            });
                        },
                        buf -> {
                            int n = buf.readInt();
                            List<RivenRawInfo> l = new ArrayList<>(n);
                            for (int i = 0; i < n; i++) l.add(new RivenRawInfo(buf.readUtf(), buf.readDouble()));
                            return new RivenRawInfoList(l);
                        }
                );
    }

    public record ReservoirsAttributes(
            double duration,
            double strength,
            double range
    ) {
        public static final Codec<ReservoirsAttributes> CODEC = RecordCodecBuilder.create(
                b -> b.group(
                        Codec.DOUBLE.fieldOf("duration").forGetter(ReservoirsAttributes::duration),
                        Codec.DOUBLE.fieldOf("strength").forGetter(ReservoirsAttributes::strength),
                        Codec.DOUBLE.fieldOf("range").forGetter(ReservoirsAttributes::range)
                ).apply(b, ReservoirsAttributes::new)
        );

        public static final StreamCodec<ByteBuf, ReservoirsAttributes> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.DOUBLE, ReservoirsAttributes::duration,
                        ByteBufCodecs.DOUBLE, ReservoirsAttributes::strength,
                        ByteBufCodecs.DOUBLE, ReservoirsAttributes::range,
                        ReservoirsAttributes::new
                );

        public ReservoirsAttributes withRandomDuration(Random random) {
            return new ReservoirsAttributes(random.nextDouble() * 5.0, strength, range);
        }

        public ReservoirsAttributes withRandomStrength(Random random) {
            return new ReservoirsAttributes(duration, random.nextDouble() * 5.0, range);
        }

        public ReservoirsAttributes withRandomRange(Random random) {
            return new ReservoirsAttributes(duration, strength, random.nextDouble() * 5.0);
        }
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<IModContainer>>
            MOD_CONTAINER = register("mod_container", (builder) -> builder
            .persistent(ModContainer.CODEC)
            .networkSynchronized(ModContainer.STREAM_CODEC)
            .cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DamageProfile>>
            DAMAGE_PROFILE = register("damage_profile", b -> b
            .persistent(DamageProfile.CODEC)
            .networkSynchronized(DamageProfile.STREAM_CODEC)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AttributeProfile>>
            ATTRIBUTES_PROFILE = register("attributes_profile", b -> b
            .persistent(AttributeProfile.CODEC)
            .networkSynchronized(AttributeProfile.STREAM_CODEC)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DefaultItemAttributes>>
            DEFAULT_ITEM_ATTRIBUTES = register("default_item_attributes", b -> b
            .persistent(DefaultItemAttributes.CODEC)
            .networkSynchronized(DefaultItemAttributes.STREAM_CODEC)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeData>>
            UPGRADE_DATA = register("upgrade_data", b -> b
            .persistent(UpgradeData.CODEC)
            .networkSynchronized(UpgradeData.STREAM_CODEC)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ReservoirsAttributes>>
            RESERVOIRS_ATTRIBUTES = register("reservoirs_attributes", b -> b
            .persistent(ReservoirsAttributes.CODEC)
            .networkSynchronized(ReservoirsAttributes.STREAM_CODEC)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EndoInfo>>
            ENDO_INFO = register("endo_info", b -> b
            .persistent(EndoInfo.CODEC)
            .networkSynchronized(EndoInfo.STREAM_CODEC)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StringListData>>
            STRING_LIST_DATA = register("string_list_data", b -> b
            .persistent(StringListData.CODEC)
            .networkSynchronized(StringListData.STREAM_CODEC)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>>
            FORMA_TYPE = register("forma_type", b -> b
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Attribute>>
            GRACE_ABILITY = register("grace_ability", b -> b
            .persistent(BuiltInRegistries.ATTRIBUTE.byNameCodec())
            .networkSynchronized(StreamCodec.of(
                    (buf, attribute) -> buf.writeVarInt(BuiltInRegistries.ATTRIBUTE.getId(attribute)),
                    buf -> Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.byId(buf.readVarInt()))
            ))
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Item>>
            RIVEN_TYPE = register("riven_type", b -> b
            .persistent(BuiltInRegistries.ITEM.byNameCodec())
            .networkSynchronized(StreamCodec.of(
                    (buf, item) -> buf.writeVarInt(BuiltInRegistries.ITEM.getId(item)),
                    buf -> BuiltInRegistries.ITEM.byId(buf.readVarInt())
            ))
            .cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>>
            RIVEN_POLARITY_TYPE = register("riven_polarity_type", b -> b
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .cacheEncoding()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RivenRawInfoList>>
            RIVEN_RAW_INFO = register("riven_raw_info", b -> b
            .persistent(RivenRawInfoList.CODEC)
            .networkSynchronized(RivenRawInfoList.STREAM_CODEC)
            .cacheEncoding());

    public static List<String> getPolarities(ItemStack stack) {
        return stack.getOrDefault(STRING_LIST_DATA,
                new StringListData(new ArrayList<>())).strings();
    }

    public static void setPolarities(ItemStack stack, List<String> list) {
        stack.set(STRING_LIST_DATA, new StringListData(list));
    }

    public static UpgradeData getUpgrade(ItemStack stack) {
        return stack.getOrDefault(UPGRADE_DATA, new UpgradeData(0, 0, 1));
    }

    public static void setUpgrade(ItemStack stack, UpgradeData data) {
        stack.set(UPGRADE_DATA, data);
    }

    public static EndoInfo getEndoInfo(ItemStack stack) {
        return stack.getOrDefault(ENDO_INFO, EndoInfo.EMPTY);
    }

    public static void setEndoInfo(ItemStack stack, EndoInfo info) {
        stack.set(ENDO_INFO, info);
    }


}
