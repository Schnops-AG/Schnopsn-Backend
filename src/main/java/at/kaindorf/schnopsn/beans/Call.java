package at.kaindorf.schnopsn.beans;

public enum Call {
    NORMAL("Normal",0), BETTLER("Bettler",5),
    SCHNAPSER("Schnapser",6),ASSENBETTLER("AssenBettler",7),
    PLAUDERER("Plauderer",8),GANG("Gang",9),
    ZEHNERGANG("Zehnergang",10),BAUER("Bauer",12),
    KONTRASCHNAPSER("Kontraschnapser",12),FARBENRINGERL("Farbenringerl",16),
    KONTRABAUER("Kontrabauer",24),TRUMPFFARBENRINGERL("Trumpffarbenringerl",24),
    KONTRATRUMPFFARBENRINGERL("Kontratrumpffarbenringerl",48);

    private final String callName;
    private final Integer value;

    Call(String callName, Integer value) {
        this.callName = callName;
        this.value = value;
    }

    public String getCallName() {
        return callName;
    }
    public Integer getValue() {
        return value;
    }
}
