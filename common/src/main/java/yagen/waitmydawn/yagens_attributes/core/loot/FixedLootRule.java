package yagen.waitmydawn.yagens_attributes.core.loot;

import yagen.waitmydawn.yagens_attributes.core.ModDefinition;

/** A fixed-size item reward appended independently to one explicit entity loot table. */
public record FixedLootRule(String id, String entity, String sourceTable, String item, double chance, int count) {
    public FixedLootRule {
        for (String value : new String[]{id, entity, sourceTable, item}) {
            ModDefinition.id(value);
            for (String segment : value.replace(':', '/').split("/", -1))
                if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) throw new IllegalArgumentException("Invalid loot resource path segment");
        }
        if (!Double.isFinite(chance) || chance < 0 || chance > 1 || count < 1 || count > 64)
            throw new IllegalArgumentException("Invalid fixed loot probability or count");
    }
    public String rewardTable() {
        int colon = id.indexOf(':'); return id.substring(0, colon + 1) + "additions/" + id.substring(colon + 1);
    }
    public FixedLootRule reward(String item, double chance, int count) { return new FixedLootRule(id, entity, sourceTable, item, chance, count); }
    public double expectedCount() { return chance * count; }
}
