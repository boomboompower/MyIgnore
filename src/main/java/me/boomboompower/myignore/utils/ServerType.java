package me.boomboompower.myignore.utils;

import java.util.regex.Pattern;

public enum ServerType {

    UNKNOWN("", ""),
    HYPIXEL("/ignore add %s", "/ignore remove %s"),
    BADLION("/ignorelist add %s", "/ignorelist rm %s"),
    OPIA("/ignore %s", "ignore %s", Pattern.compile("(?<level>(.+) )?(?<rank>.+ )?(?<player>\\S{1,16}) » (?<message>.*)"));

    private String add;
    private String remove;
    private Pattern ignorePattern;

    ServerType(String add, String remove) {
        this(add, remove, Pattern.compile("(?<rank>\\[.+] )?(?<player>\\S{1,16}): (?<message>.*)"));
    }

    ServerType(String add, String remove, Pattern customPatterns) {
        this.add = add;
        this.remove = remove;
        this.ignorePattern = customPatterns;
    }

    public String getAddToIgnoreCommand() {
        return this.add;
    }

    public String getRemoveFromIgnoreCommand() {
        return this.remove;
    }

    public Pattern getIgnorePattern() {
        return this.ignorePattern;
    }
}
