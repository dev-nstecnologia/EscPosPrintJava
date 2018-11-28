package commons;

/**
 * Created by alissonlima on 8/30/2016.
 */
public enum NFCeTBand {

    VISA("01", "Visa"),
    MATERCARD("02", "Mastercard"),
    AMERICAN_EXPRESS("03", "Am. Express"),
    SOROCRED("04", "Sorocred"),
    DINERS_CLUBE("05", "Diners Club"),
    ELO("06","Elo"),
    HIPERCARD("07", "Hipercard"),
    AURA("08", "Aura"),
    CABAL("09", "Cabal"),
    OUTROS("99", "Outros");

    private String code;
    private String description;

    NFCeTBand(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static NFCeTBand getByCodigo(String code){
        for (NFCeTBand NFCeTBand : NFCeTBand.values()) {
            if (NFCeTBand.getCodigo().equals(code))
                return NFCeTBand;
        }
        return null;
    }

    public String getCodigo() {
        return code;
    }

    public String getDescricao() {
        return description;
    }
}
