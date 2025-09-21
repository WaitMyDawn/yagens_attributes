package yagen.waitmydawn.api.item;

public enum FormaType {
    CTH("Cth"),
    NYA("Nya"),
    NOD("Nod");

    private final String value;

    FormaType(final String newValue) {
        value = newValue;
    }

    public String getValue() {
        return this.value;
    }

    public static FormaType fromString(String str) {
        for (FormaType t : values()) {
            if (t.getValue().equals(str)) return t;
        }
        throw new IllegalArgumentException("Unknown FormaType: " + str);
    }

//    String[] basicType={"Cth,Nya,"Nod"}
}
