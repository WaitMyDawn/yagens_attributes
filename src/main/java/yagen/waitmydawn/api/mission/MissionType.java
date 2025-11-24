package yagen.waitmydawn.api.mission;

import java.util.Random;

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

    public static int getRandomLevel(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    public static String getRandomMissionType() {
        Random random = new Random();
        return values()[random.nextInt(values().length)].getValue();
    }
}
