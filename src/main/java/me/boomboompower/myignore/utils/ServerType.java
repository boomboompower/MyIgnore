package me.boomboompower.myignore.utils;

public enum ServerType {

    UNKNOWN("", ""),
    HYPIXEL("/ignore add %s", "/ignore remove %s"),
    BADLION("/ignorelist add %s", "/ignorelist rm %s");

    private String add;
    private String remove;

    ServerType(String add, String remove) {
        this.add = add;
        this.remove = remove;
    }

    public String getAddToIgnoreCommand() {
        return this.add;
    }

    public String getRemoveFromIgnoreCommand() {
        return this.remove;
    }
}
