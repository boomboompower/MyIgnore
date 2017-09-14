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
     */
    public IgnoreMe() {
        if (hasBeenCreated) {
            throw new IllegalArgumentException("Only one instance of IgnoreMe can be made");
        }

        hasBeenCreated = true;
    }

    /**
     * Gets the players ignore reason
     *
     * @param player Player to check
     * @return the reason the player is ignore, returns <i>Unknown</i>
     *              if no reason was specified
     */
    public String getReason(String player) {
        return hasReason(player) ? this.playerIgnores.get(player) : "No reason";
    }

    /**
     * Checks to see if the player has a reason to be ignored
     *      returns true if a reason was found
     *
     * @param player Player to check
     * @return true if a reason was found
     */
    public boolean hasReason(String player) {
        return this.playerIgnores.containsValue(player);
    }

    /**
     * Checks to see if the given player is already ignored
     *
     * @param player Player to check
     * @return true if the player is ignored
     */
    public boolean isIgnored(String player) {
        return this.playerIgnores.containsKey(player);
    }

    /**
     * Adds a player to the ignore list
     *
     * @param player Player to add
     */
    public void addPlayer(String player) {
        this.addPlayer(player, null);
    }

    /**
     * Adds a player to the ignore list with
     *      a reason
     *
     * @param player Player to add
     * @param reason Reason they were added
     */
    public void addPlayer(String player, String reason) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null!");
        }

        this.playerIgnores.put(player, reason == null ? "No reason" : reason);

        MyIgnoreMod.getInstance().getConfigLoader().saveIgnoreMe();
    }

    /**
     * Removes a player to the ignore list
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

    public int ignoreCount() {
        return this.playerIgnores != null ? this.playerIgnores.size() : 0;
    }

    public LinkedHashMap<String, String> getPlayerIgnores() {
        LinkedHashMap<String, String> tempHash = new LinkedHashMap<>();
        tempHash.putAll(this.playerIgnores);
        return tempHash;
    }

    @Deprecated
    public void in(LinkedHashMap<String, String> in) {
        this.playerIgnores = in;
    }

    @Deprecated
    public void removeAll() {
        this.playerIgnores.clear();

        FileUtils.deleteQuietly(MyIgnoreMod.getInstance().getConfigLoader().getIgnoreMeFile());
    }
}
