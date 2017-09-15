/*
 *     Copyright (C) 2017 boomboompower
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.boomboompower.myignore;

import org.apache.commons.io.FileUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * IgnoreMe is a simple storage place for player ignores
 *
 * @author boomboompower
 * @version 1.0
 */
public class IgnoreMe {

    /** Don't want there to be multiple instances */
    private static boolean hasBeenCreated = false;

    /** Contains all player ignore values **/
    private LinkedHashMap<String, String> playerIgnores = new LinkedHashMap<>();

    /**
     * Default constructor for IgnoreMe, can only be called once
     *
     * @throws IllegalArgumentException if a new instance is made
     */
    public IgnoreMe() {
        if (hasBeenCreated) {
            throw new IllegalArgumentException("Only one instance of IgnoreMe can be made");
        }

        hasBeenCreated = true;
    }

    /**
     * Gets the players ignore reason and returns "No reason"
     *      if the player could not be found or they aren't ignored
     *
     * @param player Player to check
     * @return the reason the player is ignore, returns <i>Unknown</i>
     *              if no reason was specified
     */
    public String getReason(String player) {
        if (!isIgnored(player)) return "No reason";

        for (Map.Entry<String, String> all : this.playerIgnores.entrySet()) {
            if (all.getKey().equalsIgnoreCase(player)) {
                return all.getValue();
            }
        }
        return "No reason";
    }

    /**
     * Checks to see if the given player is already ignored
     *      (this is not case sensitive)
     *
     * @param player Player to check
     * @return true if the player is ignored
     */
    public boolean isIgnored(String player) {
        for (String all : playerIgnores.keySet()) {
            if (all.equalsIgnoreCase(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a player to the ignore list by using {@link #addPlayer(String, String)}
     *
     * @param player Player to add
     */
    public void addPlayer(String player) {
        this.addPlayer(player, null);
    }

    /**
     * Adds a player to the ignore list with the given reason
     *      then saves once finished
     *
     * @param player Player to add
     * @param reason Reason they are ignored
     */
    public void addPlayer(String player, String reason) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null!");
        }

        this.playerIgnores.put(player, reason == null ? "No reason" : reason);

        MyIgnoreMod.getInstance().getConfigLoader().saveIgnoreMe();
    }

    /**
     * Removes a player from the ignore list then saves
     *
     * @param player Player to remove
     */
    public void remove(String player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null!");
        }

        this.playerIgnores.remove(player);

        MyIgnoreMod.getInstance().getConfigLoader().saveIgnoreMe();
    }

    /**
     * Gets the size of the {@link LinkedHashMap} for looping
     *
     * @return size of linked list
     */
    public int ignoreCount() {
        return this.playerIgnores != null ? this.playerIgnores.size() : 0;
    }

    /**
     * Duplicates the LinkedHashMap and returns a temporary one
     *      this is to prevent the {@link LinkedHashMap#clear()} method
     *
     * @return a duplicate {@link LinkedHashMap}
     */
    public LinkedHashMap<String, String> getPlayerIgnores() {
        LinkedHashMap<String, String> tempHash = new LinkedHashMap<>();
        tempHash.putAll(this.playerIgnores);
        return tempHash;
    }

    /**
     * The current way of setting the {@link LinkedHashMap} playerIgnores variable
     *
     * @param in the list to load
     * @deprecated May be removed in the future
     */
    @Deprecated
    public void in(LinkedHashMap<String, String> in) {
        this.playerIgnores = in;
    }

    /**
     * Clears the ignore list & quietly deletes the config file
     *
     * @deprecated May be removed in the future
     */
    @Deprecated
    public void removeAll() {
        this.playerIgnores.clear();

        FileUtils.deleteQuietly(MyIgnoreMod.getInstance().getConfigLoader().getIgnoreMeFile());
    }
}
