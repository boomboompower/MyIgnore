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

package me.boomboompower.myignore.commands;

import me.boomboompower.myignore.MyIgnoreMod;
import me.boomboompower.myignore.utils.ChatColor;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class CommandIgnore implements ICommand {

    @Override
    public String getCommandName() {
        return "ignore";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return ChatColor.RED + "/" + getCommandName() + " <help, list, add, remove, why>";
    }

    @Override
    public List<String> getCommandAliases() {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sendMessage(getCommandUsage(sender));
        } else {
            switch (args[0]) {
                case "list":
                    if (MyIgnoreMod.getInstance().getIgnoreMe().ignoreCount() > 0) {
                        int page = 1;
                        int pages = (int) Math.ceil((double) MyIgnoreMod.getInstance().getIgnoreMe().ignoreCount() / 7.0);

                        if (args.length > 1) {
                            try {
                                page = Integer.parseInt(args[1]);
                            } catch (NumberFormatException ignored) {
                                page = -1;
                            }
                        }

                        if (page < 1 || page > pages) {
                            sendMessage(ChatColor.RED + "Invalid page number.");
                        } else {
                            separator();

                            ChatComponentText mainTitle = new ChatComponentText(ChatColor.GOLD + "Ignored Players List " + ChatColor.GRAY
                                    + "[Page " + page + " of " + pages + "]" + ChatColor.GOLD + ":");

                            ChatComponentText nextPage = new ChatComponentText(ChatColor.AQUA + " " + ChatColor.STRIKETHROUGH + "->");

                            nextPage.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatColor.AQUA + "Open next page")));
                            nextPage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " list " + (page + 1 + "")));

                            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(mainTitle.appendSibling(nextPage));

                            MyIgnoreMod.getInstance().getIgnoreMe().getPlayerIgnores().entrySet().stream()
                                    .skip((page - 1) * 7)
                                    .limit(7)
                                    .forEach(ignore -> {
                                        ChatComponentText text = new ChatComponentText(ignore.getKey() + ": " + ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', ignore.getValue()));
                                        ChatComponentText remove = new ChatComponentText(ChatColor.WHITE + " - " + ChatColor.RED + ChatColor.BOLD.toString() + "\u2717");

                                        remove.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatColor.RED + "Remove " + ignore.getKey() + " from ignore list")));
                                        remove.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " remove " + ignore.getKey()));

                                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(text.appendSibling(remove));
                                    });

                            separator();
                        }
                    } else {
                        sendMessage(ChatColor.RED + "You haven't ignored anyone yet!");
                    }
                    break;
                case "add":
                    if (args.length == 1) {
                        sendMessage("&cUsage: /" + getCommandName() + " add <Player>");
                    } else {
                        if (!MyIgnoreMod.getInstance().getIgnoreMe().isIgnored(args[1])) {
                            if (args.length == 2) {
                                MyIgnoreMod.getInstance().getIgnoreMe().addPlayer(args[1]);
                                sendMessage("&aSuccessfully ignored %s", args[1]);
                            } else {
                                // There is a reason to add them, transfer args to a string
                                String reason = getArgsAsString(args, 2);
                                MyIgnoreMod.getInstance().getIgnoreMe().addPlayer(args[1], reason);
                                sendMessage("&aSuccessfully ignored %s for \"%s\"", args[1], reason);
                            }
                        } else {
                            sendMessage("&cYou have already ignored %s, run &b/" + getCommandName() + " remove <Player>&c to unignore them!", args[1]);
                        }
                    }

                    if (MyIgnoreMod.getInstance().isHypixel) {
                        exCommand("/ignore add " + args[1]);
                    }
                    break;
                case "rem":
                case "remove":
                    if (args.length == 1) {
                        sendMessage("&cUsage: /" + getCommandName() + " remove <Player>");
                    } else {
                        if (!MyIgnoreMod.getInstance().getIgnoreMe().isIgnored(args[1])) {
                            sendMessage("&cYou aren\'t ignoring %s, run &b/" + getCommandName() + " add <Player>&c to ignore them!", args[1]);
                        } else {
                            MyIgnoreMod.getInstance().getIgnoreMe().remove(args[1]);
                            sendMessage("&aSuccessfully unignored %s", args[1]);
                        }
                    }

                    if (MyIgnoreMod.getInstance().isHypixel) {
                        exCommand("/ignore remove " + args[1]);
                    }
                    break;
                case "why":
                    if (args.length == 1) {
                        sendMessage("&cUsage: /" + getCommandName() + " why <Player>");
                    } else {
                        if (!MyIgnoreMod.getInstance().getIgnoreMe().isIgnored(args[1])) {
                            sendMessage("&cYou aren\'t ignoring %s so no reason was found!", args[0]);
                        } else {
                            sendMessage("&c%s is ignored because: &7%s", args[1], MyIgnoreMod.getInstance().getIgnoreMe().getReason(args[1]));
                        }
                    }
                    break;
                case "clear":
                    sendMessage("&aCleared everyone from ignore list!");

                    for (String person : MyIgnoreMod.getInstance().getIgnoreMe().getPlayerIgnores().values()) {
                        exCommand("/ignore remove " + person);
                    }

                    MyIgnoreMod.getInstance().getIgnoreMe().removeAll();
                default:
                    sendMessage(getCommandUsage(sender));
            }
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

    private void separator() {
        sendMessage("&6&l&m----------------------------------");
    }

    private void sendMessage(String message, Object... replacements) {
        try {
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(ChatColor.translateAlternateColorCodes(String.format(message, replacements))));
        } catch (Exception ex) {
            // Fallback for if we forgot the replacement
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(ChatColor.translateAlternateColorCodes(message)));
        }
    }

    private void exCommand(String command) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
    }

    private String getArgsAsString(String[] args, int startpos) {
        StringBuilder builder = new StringBuilder();
        for (int i = startpos; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        return builder.toString().trim();
    }
}