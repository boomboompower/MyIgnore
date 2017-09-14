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

package me.boomboompower.myignore.listeners;

import me.boomboompower.myignore.MyIgnoreMod;
import me.boomboompower.myignore.utils.ChatColor;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatReceived {

    private static final Pattern chatPattern = Pattern.compile("(?<rank>\\[.+] )?(?<player>\\S{1,16}): (?<message>.*)");
    private static final Pattern hypixelIgnorePatternSucceed = Pattern.compile("Added (?<player>\\S{1,16}) to your ignore list\\.");
    private static final Pattern hypixelIgnorePatternFail = Pattern.compile("Can't find a player by the name of '(?<player>\\S{1,16})'");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatReceive(ClientChatReceivedEvent event) {
        String message = ChatColor.stripColor(event.message.getUnformattedText());

        if (message.isEmpty()) {
            return;
        }

        Matcher matcher = chatPattern.matcher(message);

        if (matcher.matches()) {
            String player = matcher.group("player");

            if (MyIgnoreMod.getInstance().getIgnoreMe().isIgnored(player)) {
                event.setCanceled(true);
            }
            return;
        }

        if (hypixelIgnorePatternSucceed.matcher(message).matches() || hypixelIgnorePatternFail.matcher(message).matches()) {
            event.setCanceled(true);
        }
    }
}
