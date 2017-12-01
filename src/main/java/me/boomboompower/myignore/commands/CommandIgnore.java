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

import me.boomboompower.myignore.IgnoreMe;
import me.boomboompower.myignore.MyIgnoreMod;
import me.boomboompower.myignore.utils.ChatColorLite;
import me.boomboompower.myignore.utils.ServerType;

import net.minecraft.client.Minecraft;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.text.Collator;
import java.util.*;

public class CommandIgnore implements ICommand {

    private static final IgnoreMe ignore = MyIgnoreMod.getInstance().getIgnoreMe();
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    @Override
    public String getCommandName() {
        return "ignore";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return ChatColorLite.RED + "Usage: /" + getCommandName() + " " + asArguments(getSubcommannds());
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>();
    }

    public List<String> getSubcommannds() {
        return Arrays.asList("help", "list", "add", "remove", "why", "edit");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sendMessage(getCommandUsage(sender));
        } else {
            switch (args[0]) {
                case "list":
                    if (ignore.ignoreCount() > 0) {
                        int page = 1;
                        int pages = (int) Math.ceil((double) ignore.ignoreCount() / 7.0);

                        if (args.length > 1) {
                            try {
                                page = Integer.parseInt(args[1]);
                            } catch (NumberFormatException ignored) {
                                page = -1;
                            }
                        }

                        if (page < 1 || page > pages) {
                            sendMessage(ChatColorLite.RED + "Invalid page number.");
                        } else {
                            TreeMap<String, String> list = ignore.getPlayerIgnores();

                            list.entrySet().removeIf(next -> next.getValue().contains("[HIDDEN]"));

                            separator();

                            ChatComponentText mainTitle = new ChatComponentText(ChatColorLite.GOLD + "Ignored Players List " + ChatColorLite.GRAY
                                    + "[Page " + page + " of " + pages + "]" + ChatColorLite.GOLD + ":");

                            ChatComponentText nextPage = new ChatComponentText(ChatColorLite.AQUA + " " + ChatColorLite.STRIKETHROUGH + "->");

                            nextPage.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatColorLite.AQUA + "Open next page")));
                            nextPage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " list " + (page + 1 + "")));

                            if (page < pages) {
                                mc.thePlayer.addChatComponentMessage(mainTitle.appendSibling(nextPage));
                            } else {
                                mc.thePlayer.addChatComponentMessage(mainTitle);
                            }

                            list.entrySet().stream()
                                    .skip((page - 1) * 7)
                                    .limit(7)
                                    .forEach(ignore -> {
                                        boolean perm = ignore.getValue().contains("[PERM]");
                                        String value = ChatColorLite.formatUnformat('&', ignore.getValue().replace("[PERM]", "")).isEmpty() ? "No reason" : ChatColorLite.translateAlternateColorCodes('&', ignore.getValue());

                                        ChatComponentText text = new ChatComponentText(ignore.getKey() + ": " + ChatColorLite.GOLD + value + (!perm ? ChatColorLite.WHITE + " -" : ""));

                                        ChatComponentText remove = new ChatComponentText(ChatColorLite.RED + ChatColorLite.BOLD.toString() + " \u2717");

                                        remove.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatColorLite.RED + "Remove " + ignore.getKey() + " from ignore list")));
                                        remove.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " remove " + ignore.getKey()));

                                        mc.thePlayer.addChatComponentMessage((!perm ? text.appendSibling(remove) : text));
                                    });

                            separator();
                        }
                    } else {
                        sendMessage(ChatColorLite.RED + "You haven't ignored anyone yet!");
                    }
                    break;
                case "add":
                    if (args.length == 1) {
                        sendMessage("&cUsage: /" + getCommandName() + " add " + asArguments(Collections.singletonList("Player")));
                    } else {
                        if (!ignore.isIgnored(args[1])) {
                            if (args.length == 2) {
                                ignore.addPlayer(args[1]);
                                sendMessage("&aSuccessfully ignored %s", args[1]);
                            } else {
                                // There is a reason to add them, transfer args to a string
                                String reason = getArgsAsString(args, 2);
                                ignore.addPlayer(args[1], reason);
                                sendMessage("&aSuccessfully ignored %s for \"%s\"", args[1], reason);
                            }

                            if (MyIgnoreMod.getInstance().getServerType() != ServerType.UNKNOWN && args.length > 2 && args[2].equalsIgnoreCase("-server")) {
                                exCommand(String.format(MyIgnoreMod.getInstance().getServerType().getAddToIgnoreCommand(), args[1]));
                            }
                        } else {
                            sendMessage("&cYou have already ignored %s, run &b/" + getCommandName() + " remove Player&c to unignore them!", args[1]);
                        }
                    }
                    break;
                case "rem":
                case "remove":
                    if (args.length == 1) {
                        sendMessage("&cUsage: /" + getCommandName() + " remove " + asArguments(Collections.singletonList("Player")));
                    } else {
                        if (!ignore.isIgnored(args[1])) {
                            sendMessage("&cYou aren\'t ignoring %s, run &b/" + getCommandName() + " add Player&c to ignore them!", args[1]);
                        } else if (ignore.getReason(args[1]).toUpperCase().endsWith("[PERM]")) {
                            sendMessage("&cYou cannot unignore that player while they have the permanent attribute.");
                        } else {
                            ignore.remove(args[1]);
                            sendMessage("&aSuccessfully unignored %s", args[1]);

                            if (MyIgnoreMod.getInstance().getServerType() != ServerType.UNKNOWN && args.length > 2 && args[2].equalsIgnoreCase("-server")) {
                                exCommand(String.format(MyIgnoreMod.getInstance().getServerType().getRemoveFromIgnoreCommand(), args[1]));
                            }
                        }
                    }
                    break;
                case "why":
                    if (args.length == 1) {
                        sendMessage("&cUsage: /" + getCommandName() + " why " + asArguments(Collections.singletonList("Player")));
                    } else {
                        if (!ignore.isIgnored(args[1])) {
                            sendMessage("&cNo ignore reason was found for " + args[1]);
                        } else {
                            if (ignore.getReason(args[1]).equals(IgnoreMe.DEFUALT_MESSAGE)) {
                                sendMessage("&7%s&c does not have an ignore reason.", args[1]);
                            } else {
                                sendMessage("&cThe current ignore reason for &7%s&c is &7%s", args[1], ignore.getReason(args[1]));
                            }
                        }
                    }
                    break;
                case "edit":
                    if (args.length == 1) {
                        sendMessage("&cUsage: /" + getCommandName() + " edit " + asArguments(Collections.singletonList("Player")) + " " + asArguments(Collections.singletonList("Reason")));
                    } else {
                        if (!ignore.isIgnored(args[1])) {
                            sendMessage("&7%s&c is not currently ignored!", args[1]);
                        } else {
                            if (args.length > 2) {
                                String reason = getArgsAsString(args, 2);
                                if (ignore.updateReason(args[1], reason)) {
                                    sendMessage("&aSuccessfully updated %s%s ignore reason to \"%s\"", args[1], (args[1].endsWith("s") ? "\'" : "\'s"), reason);
                                } else {
                                    sendMessage("&cFailed to update %s%s ignore reason!", args[1], (args[1].endsWith("s")));
                                }
                            } else {
                                sendMessage("&cUsage: /" + getCommandName() + " edit " + asArguments(Collections.singletonList("Player")) + " " + asArguments(Collections.singletonList("Reason")));
                            }
                        }
                    }
                    break;
                case "clear":
                    sendMessage("&aCleared everyone from ignore list!");

                    if (args.length > 1 && MyIgnoreMod.getInstance().getServerType() != ServerType.UNKNOWN && args[1].equalsIgnoreCase("-server")) {
                        for (String person : ignore.getPlayerIgnores().values()) {
                            exCommand(String.format(MyIgnoreMod.getInstance().getServerType().getRemoveFromIgnoreCommand(), person));
                        }
                    }

                    ignore.removeAll();
                    break;
                case "unregistercommand":
                    if (args.length > 1 && args[1].equalsIgnoreCase("--confirm")) {
                        try {
                            ClientCommandHandler handler = ClientCommandHandler.instance;
                            Set<ICommand> set = ReflectionHelper.getPrivateValue(CommandHandler.class, handler, "commandSet");
                            Map<String, ICommand> map = ReflectionHelper.getPrivateValue(CommandHandler.class, handler, "commandMap");
                            for (ICommand command : set) {
                                if (command.getCommandName().equalsIgnoreCase(getCommandName())) {
                                    set.remove(command);
                                    break;
                                }
                            }
                            for (String command : map.keySet()) {
                                if (command.equalsIgnoreCase(getCommandName())) {
                                    map.remove(command);
                                    break;
                                }
                            }
                            ReflectionHelper.setPrivateValue(CommandHandler.class, handler, set, "commandSet");
                            ReflectionHelper.setPrivateValue(CommandHandler.class, handler, map, "commandMap");
                            sendMessage("&aCommand removal successfully executed");
                        } catch (Exception ex) {
                            sendMessage("&cCommand removal was unsuccessful");
                        }
                        break;
                    }
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
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, getSubcommannds());
        } else if (args.length == 2) {
            switch (args[0]) {
                case "add":
                    return CommandBase.getListOfStringsMatchingLastWord(args, getPlayerNames());
                case "rem":
                case "why":
                case "edit":
                case "remove":
                    return CommandBase.getListOfStringsMatchingLastWord(args, ignore.getPlayerIgnores().keySet());
                case "list":
                    return CommandBase.getListOfStringsMatchingLastWord(args, getPageCount());
            }
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return args.length > 0 && args[0].equalsIgnoreCase("add");
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
            mc.thePlayer.addChatComponentMessage(new ChatComponentText(ChatColorLite.translateAlternateColorCodes('&', String.format(message, replacements))));
        } catch (Exception ex) {
            try {
                // Fallback for if we forgot the replacement
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(ChatColorLite.translateAlternateColorCodes('&', message)));
            } catch (Exception ex1) {
                MyIgnoreMod.LOGGER.error("Failed to send the player a chat message", ex1);
            }
        }
    }

    private void exCommand(String command) {
        mc.thePlayer.sendChatMessage(command);
    }

    private String getArgsAsString(String[] args, int startpos) {
        StringBuilder builder = new StringBuilder();
        for (int i = startpos; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        return builder.toString().trim();
    }

    private String asArguments(List<String> input) {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        Iterator<String> iterator = input.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            builder.append(next);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.append(">").toString().trim();
    }

    private boolean isOne(String input, String... choices) {
        for (String s : choices) {
            if (s.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getPlayerNames() {
        if (mc.theWorld == null) {
            return new ArrayList<>();
        } else {
            List<String> names = new ArrayList<>();
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (!mc.thePlayer.equals(player)) {
                    names.add(player.getName());
                }
            }
            names.sort(Collator.getInstance());

            return names;
        }
    }

    private List<String> getPageCount() {
        if (ignore.getPlayerIgnores().isEmpty()) return new ArrayList<>();

        List<String> list = new ArrayList<>();
        for (int i = 1; i <= (int) Math.ceil((double) ignore.ignoreCount() / 7.0); i++) {
            list.add(i + "");
        }
        return list;
    }
}