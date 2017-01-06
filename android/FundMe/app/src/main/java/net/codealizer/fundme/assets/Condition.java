package net.codealizer.fundme.assets;

public enum Condition {
    USED(1), ACCEPTABLE(2), GOOD(3), VERY_GOOD(4), BRAND_NEW(5);
    private int value;

    Condition(int value) {
        this.value = value;
    }

    public static Condition getCondition(int value) {
        switch (value) {
            case 1:
                return USED;
            case 2:
                return ACCEPTABLE;
            case 3:
                return GOOD;
            case 4:
                return VERY_GOOD;
            case 5:
                return BRAND_NEW;
            default:
                return USED;
        }
    }

    @Override
    public String toString() {
        switch (value) {
            case 1:
                return "Used";
            case 2:
                return "Acceptable";
            case 3:
                return "Good";
            case 4:
                return "Very Good";
            case 5:
                return "Brand New";
            default:
                return "Used";
        }
    }
}