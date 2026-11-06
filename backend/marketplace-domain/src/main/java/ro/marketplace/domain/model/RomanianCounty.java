package ro.marketplace.domain.model;

import java.util.Arrays;

/** County-level coverage, including the municipality of București, using ISO 3166-2 codes. */
public enum RomanianCounty {
    AB("Alba"), AR("Arad"), AG("Argeș"), BC("Bacău"), BH("Bihor"),
    BN("Bistrița-Năsăud"), BT("Botoșani"), BV("Brașov"), BR("Brăila"), B("București"),
    BZ("Buzău"), CS("Caraș-Severin"), CL("Călărași"), CJ("Cluj"), CT("Constanța"),
    CV("Covasna"), DB("Dâmbovița"), DJ("Dolj"), GL("Galați"), GR("Giurgiu"), GJ("Gorj"),
    HR("Harghita"), HD("Hunedoara"), IL("Ialomița"), IS("Iași"), IF("Ilfov"),
    MM("Maramureș"), MH("Mehedinți"), MS("Mureș"), NT("Neamț"), OT("Olt"), PH("Prahova"),
    SM("Satu Mare"), SJ("Sălaj"), SB("Sibiu"), SV("Suceava"), TR("Teleorman"),
    TM("Timiș"), TL("Tulcea"), VS("Vaslui"), VL("Vâlcea"), VN("Vrancea");

    private final String displayName;

    RomanianCounty(String displayName) {
        this.displayName = displayName;
    }

    public String id() {
        return "RO-" + name();
    }

    public String displayName() {
        return displayName;
    }

    public static boolean isKnownId(String id) {
        return Arrays.stream(values()).anyMatch(county -> county.id().equals(id));
    }
}
