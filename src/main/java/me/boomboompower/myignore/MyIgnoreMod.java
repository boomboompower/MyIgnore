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

import me.boomboompower.myignore.commands.CommandIgnore;
import me.boomboompower.myignore.config.ConfigLoader;
import me.boomboompower.myignore.utils.ChatColorLite;
import me.boomboompower.myignore.utils.ServerType;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = MyIgnoreMod.MOD_ID, version = MyIgnoreMod.MOD_VERSION, acceptedMinecraftVersions = "*")
public class MyIgnoreMod {

    public static final String MOD_ID = "myignore";
    public static final String MOD_VERSION = "1.4";

    public static final Logger LOGGER = LogManager.getLogger("IgnoreMe");

    private static final Pattern patternHypixelIgnoreAdd = Pattern.compile("Added (?<player>\\S{1,16}) to your ignore list\\.");
    private static final Pattern patternHypixelIgnoreRemove = Pattern.compile("Removed '(?<player>\\S{1,16})' from your ignore list\\.");

    private static final Pattern patternHypixelIgnoreAddFail = Pattern.compile("You've already ignored that player! /ignore remove Player to unignore them!");
    private static final Pattern patternHypixelIgnoreRemoveFail = Pattern.compile("You aren't ignoring that player! /ignore add Player to ignore!");

    private static final Minecraft mc = Minecraft.getMinecraft();

    @Mod.Instance
    private static MyIgnoreMod instance;

    private ConfigLoader configLoader;
    private IgnoreMe ignoreMe;

    private ServerType serverType = ServerType.UNKNOWN;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata data = event.getModMetadata();
        data.authorList = Collections.singletonList("boomboompower");
        data.version = MOD_VERSION;
        data.credits = "Thanks to boomboompower for actually updating this!";
        data.name = "MyIgnore";
        data.description = ChatColorLite.translateAlternateColorCodes('&', "&7Created by &6boomboompower &f| &7Run &9/ignore&7 to get started");

        this.ignoreMe = new IgnoreMe();
        this.configLoader = new ConfigLoader("mods/myignore/" + Minecraft.getMinecraft().getSession().getProfile().getId() + "/");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Minecraft.getMinecraft().addScheduledTask(() -> this.configLoader.loadIgnoreMe());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        registerEvents(this);
        registerCommands(new CommandIgnore());
    }

    // EVENTS START

    @SubscribeEvent
    public void onClientConnectToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (mc.getCurrentServerData() != null) {
            String ip = mc.getCurrentServerData().serverIP.toLowerCase();
            if (ip.endsWith(".hypixel.net")) {
                this.serverType = ServerType.HYPIXEL;
            } else if (ip.endsWith(".badlion.net")) {
                this.serverType = ServerType.BADLION;
            } else if (ip.endsWith(".opiamc.net")) {
                this.serverType = ServerType.OPIA;
            } else {
                this.serverType = ServerType.UNKNOWN;
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        this.serverType = ServerType.UNKNOWN;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatReceive(ClientChatReceivedEvent event) {
        String message = ChatColorLite.stripColor(event.message.getUnformattedText());

        if (message.isEmpty() || event.isCanceled()) {
            return;
        }

        Matcher matcher = this.serverType.getIgnorePattern().matcher(message);

        if (matcher.matches()) {
            String player = matcher.group("player");

            if (this.ignoreMe.isIgnored(player)) {
                event.setCanceled(true);
            }
            return;
        }

        if (this.serverType == ServerType.HYPIXEL && isPickleMessage(message)) {
            event.setCanceled(true);
        }

        if (this.serverType == ServerType.BADLION && message.startsWith("You have") && message.endsWith("your ignore list.")) {
            event.setCanceled(true);
        }
    }

    // EVENTS END

    // OOP START

    private void registerEvents(Object... events) {
        Arrays.stream(events).forEach(MinecraftForge.EVENT_BUS::register);
    }

    private void registerCommands(ICommand... commands) {
        Arrays.stream(commands).forEach(ClientCommandHandler.instance::registerCommand);
    }

    public ConfigLoader getConfigLoader() {
        return this.configLoader;
    }

    public static MyIgnoreMod getInstance() {
        return instance;
    }

    public IgnoreMe getIgnoreMe() {
        return this.ignoreMe;
    }

    public ServerType getServerType() {
        return this.serverType;
    }

    private boolean isPickleMessage(String input) {
        return patternHypixelIgnoreAdd.matcher(input).matches() ||
                patternHypixelIgnoreRemove.matcher(input).matches() ||
                patternHypixelIgnoreAddFail.matcher(input).matches() ||
                patternHypixelIgnoreRemoveFail.matcher(input).matches();
    }

    // OOP END
}
