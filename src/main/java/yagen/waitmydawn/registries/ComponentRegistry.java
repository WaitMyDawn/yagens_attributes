package yagen.waitmydawn.registries;

import com.mojang.serialization.Codec;
//import yagen.waitmydawn.YagensAttributes;
//import yagen.waitmydawn.api.item.UpgradeData;
//import yagen.waitmydawn.api.item.WaywardCompassData;
//import yagen.waitmydawn.api.item.curios.AffinityData;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.mods.IModContainer;
//import yagen.waitmydawn.capabilities.magic.ModContainer;
//import yagen.waitmydawn.fluids.PotionFluid;
//import yagen.waitmydawn.item.FurledMapItem;
//import yagen.waitmydawn.item.armor.UpgradeOrbType;
//import yagen.waitmydawn.item.weapons.AutoloaderCrossbow;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.capabilities.ModContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            int exp,           // 当前经验
            int polarity,      // 极化次数
            int level          // 当前等级
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

        /* 工具方法：新建 / 增量 */
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

    public record StringListData(List<String> strings) {
        public static final Codec<StringListData> CODEC = RecordCodecBuilder.create(
                b -> b.group(
                        Codec.STRING.listOf().fieldOf("strings").forGetter(StringListData::strings)
                ).apply(b, StringListData::new)
        );

        public static final StreamCodec<ByteBuf, StringListData> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), // 不定长 String List
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

//    /* ====== 新增：运行时词条列表 ====== */
//    /* 新增：用字符串保存文本组件 */
//    public record UniqueInfoList(List<MutableComponent> components) {
//
//        /* 只存取纯文本内容 */
//        private static final Codec<MutableComponent> COMP_CODEC =
//                Codec.STRING.xmap(
//                        Component::literal,        // 读：String -> MutableComponent
//                        comp -> comp.getString()   // 写：MutableComponent -> String
//                );
//
//        public static final Codec<UniqueInfoList> CODEC =
//                COMP_CODEC.listOf().xmap(UniqueInfoList::new, UniqueInfoList::components);
//
//        public static final StreamCodec<FriendlyByteBuf, UniqueInfoList> STREAM_CODEC =
//                StreamCodec.of(
//                        (buf, list) -> {
//                            buf.writeInt(list.components.size());
//                            list.components.forEach(c -> buf.writeUtf(c.getString()));
//                        },
//                        buf -> {
//                            int n = buf.readInt();
//                            List<MutableComponent> list = new ArrayList<>(n);
//                            for (int i = 0; i < n; i++) {
//                                list.add(Component.literal(buf.readUtf()));
//                            }
//                            return new UniqueInfoList(list);
//                        }
//                );
//    }

    public record RivenRawInfoList(List<RivenRawInfo> raw) {
        /* 只存 key + baseValue 两字段 */
        public record RivenRawInfo(String key, double base) {}

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

    //public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeData>> UPGRADE_DATA = register("upgrade_data", (builder) -> builder.persistent(UpgradeData.CODEC).networkSynchronized(UpgradeData.STREAM_CODEC).cacheEncoding());
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
    //    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DefaultAttributeProfile>>
//            DEFAULT_ATTRIBUTES_PROFILE = register("default_attributes_profile", b -> b
//            .persistent(DefaultAttributeProfile.CODEC)
//            .networkSynchronized(DefaultAttributeProfile.STREAM_CODEC)
//            .cacheEncoding()
//    );
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
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Item>>
            RIVEN_TYPE = register("riven_type", b -> b
            .persistent(BuiltInRegistries.ITEM.byNameCodec())
            .networkSynchronized(StreamCodec.of(
                    (buf, item) -> buf.writeVarInt(BuiltInRegistries.ITEM.getId(item)),
                    buf -> BuiltInRegistries.ITEM.byId(buf.readVarInt())
            ))
            .cacheEncoding());
//    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UniqueInfoList>>
//            UNIQUE_INFO_LIST = register("unique_info_list", b -> b
//            .persistent(UniqueInfoList.CODEC)
//            .networkSynchronized(UniqueInfoList.STREAM_CODEC)
//            .cacheEncoding());
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
        return stack.getOrDefault(ComponentRegistry.STRING_LIST_DATA,
                new StringListData(new ArrayList<>())).strings();
    }

    public static void setPolarities(ItemStack stack, List<String> list) {
        stack.set(ComponentRegistry.STRING_LIST_DATA, new StringListData(list));
    }

    public static UpgradeData getUpgrade(ItemStack stack) {
        return stack.getOrDefault(ComponentRegistry.UPGRADE_DATA, new UpgradeData(0, 0, 1));
    }

    public static void setUpgrade(ItemStack stack, UpgradeData data) {
        stack.set(ComponentRegistry.UPGRADE_DATA, data);
    }
}
