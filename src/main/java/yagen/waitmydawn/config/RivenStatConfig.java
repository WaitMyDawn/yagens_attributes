package yagen.waitmydawn.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 *
 * Example: slash.json:
 * [
 *   { "operation": "addition", "weight": 3, "base_value": 15.0 },
 *   { "operation": "multibase", "weight": 2, "base_value": 0.5 }
 * ]
 */
public record RivenStatConfig(List<Entry> entries) {

    public record Entry(String operation, int weight, double baseValue) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("operation").forGetter(Entry::operation),
                Codec.INT.fieldOf("weight").forGetter(Entry::weight),
                Codec.DOUBLE.fieldOf("base_value").forGetter(Entry::baseValue)
        ).apply(instance, Entry::new));
    }

    public static final Codec<List<Entry>> CODEC = Entry.CODEC.listOf();
}