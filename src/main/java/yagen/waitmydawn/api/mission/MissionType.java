package yagen.waitmydawn.api.mission;

public enum MissionType {
    EXTERMINATE("Exterminate"),
    DEFENSE("Defense"),
    SURVIVAL("Survival"),
    ASSASSINATION("Assassination");

    private final String value;

    MissionType(final String newValue) {
        value = newValue;
    }

    public String getValue() {
        return this.value;
    }

    public static MissionType fromString(String str) {
        for (MissionType t : values()) {
            if (t.getValue().equals(str)) return t;
        }
        throw new IllegalArgumentException("Unknown MissionType: " + str);
    }
}
