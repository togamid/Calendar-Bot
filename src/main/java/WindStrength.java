public enum WindStrength {
    NONE,
    LOW,
    MODERATE,
    HIGH,
    SEVERE,
    RANDOM;

public static WindStrength fromValue(String value) {
        try {
            return WindStrength.valueOf(value.toUpperCase().trim());
        }
        catch (Exception e) {
            System.out.println("couldn't map"+value+" to a wind strength");
            return null;
        }
}
}
