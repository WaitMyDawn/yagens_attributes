package yagen.waitmydawn.capabilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.List;

public class PlayerBossData {
    private final List<EntityType<?>> bosses;

    public PlayerBossData() {
        this(new ArrayList<>());
    }

    public PlayerBossData(List<EntityType<?>> bosses) {
        this.bosses = new ArrayList<>(bosses);
    }

    public boolean addBoss(EntityType<? extends Monster> boss) {
        if (!bosses.contains(boss)) {
            bosses.add(boss);
            return true;
        }
        return false;
    }

    public boolean removeBoss(EntityType<? extends Monster> boss) {
        if (bosses.contains(boss)) {
            bosses.remove(boss);
            return true;
        }
        return false;
    }

    public void removeLatestBoss() {
        bosses.removeLast();
    }

    public int getSize() {
        return bosses.size();
    }

    public List<EntityType<?>> getList() {
        return bosses;
    }

    // client
    public void setBosses(List<EntityType<?>> newBosses) {
        this.bosses.clear();
        this.bosses.addAll(newBosses);
    }

    public static final Codec<PlayerBossData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec()
                    .listOf()
                    .fieldOf("bosses")
                    .forGetter(PlayerBossData::getList)
    ).apply(instance, PlayerBossData::new));

    private static final StreamCodec<ByteBuf, EntityType<?>> ENTITY_TYPE_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(BuiltInRegistries.ENTITY_TYPE::get, BuiltInRegistries.ENTITY_TYPE::getKey);

    public static final StreamCodec<ByteBuf, PlayerBossData> STREAM_CODEC =
            ENTITY_TYPE_STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(PlayerBossData::new, PlayerBossData::getList);
}