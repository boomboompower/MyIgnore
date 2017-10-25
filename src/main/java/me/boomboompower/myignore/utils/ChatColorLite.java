package me.boomboompower.myignore.utils;

import java.util.regex.Pattern;

/*
 * ChatColorLite is a minified version of the ChatColor class, that
 *      only contains used methods/variables
 */
public enum ChatColorLite {
    
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    MAGIC('k'),
    BOLD('l'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r');

    public static final char COLOR_CHAR = '\u00A7';

    private final String toString;

    ChatColorLite(char code) {
        this.toString = new String(new char[] {COLOR_CHAR, code});
    }

    @Override
    public String toString() {
        return this.toString;
    }

    /*
     * Removes all color codes from the given message
     */
    public static String stripColor(final String input) {
        if (input == null) {
            return null;
        }
        return Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]").matcher(input).replaceAll("");
    }

    /*
     * Replaces all the altColorChar codes with a color code symbol if the following character is a color code
     */
    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = ChatColorLite.COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

    /*
     * Color then strip the message, useful for removing all colors in the message.
     */
    public static String formatUnformat(char altColorChat, String message) {
        return stripColor(translateAlternateColorCodes(altColorChat, message));
    }
}