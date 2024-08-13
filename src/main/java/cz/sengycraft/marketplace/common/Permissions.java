package cz.sengycraft.marketplace.common;

public enum Permissions {

    SELL("sell"),
    VIEW("view"),
    BLACKMARKET("blackmarket"),
    HISTORY("history"),
    RELOAD("reload");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String permission() {
        return "marketplace." + permission;
    }

}
