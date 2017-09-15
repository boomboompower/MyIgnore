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
import me.boomboompower.myignore.utils.ChatColor;

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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = MyIgnoreMod.MOD_ID, version = MyIgnoreMod.MOD_VERSION, acceptedMinecraftVersions = "*")
public class MyIgnoreMod {

    public static final String MOD_ID = "myignore_boom";
    public static final String MOD_VERSION = "1.2";

    private static final Pattern chatPattern = Pattern.compile("(?<rank>\\[.+] )?(?<player>\\S{1,16}): (?<message>.*)");
    private static final Pattern hypixelIgnorePatternSucceed = Pattern.compile("Added (?<player>\\S{1,16}) to your ignore list\\.");
    private static final Pattern hypixelIgnorePatternFail = Pattern.compile("Can't find a player by the name of '(?<player>\\S{1,16})'");

    private static final Minecraft mc = Minecraft.getMinecraft();

    @Mod.Instance
    private static MyIgnoreMod instance;

    private ConfigLoader configLoader;
    private IgnoreMe ignoreMe;

    public boolean isHypixel;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata data = event.getModMetadata();
        data.authorList = Collections.singletonList("boomboompower");
        data.version = MOD_VERSION;
        data.credits = ChatColor.translateAlternateColorCodes("Run &9/ignore&r to get started");
        data.name = "MyIgnore";
        data.description = ChatColor.translateAlternateColorCodes("&bCreated by boomboompower");

        this.ignoreMe = new IgnoreMe();
        this.configLoader = new ConfigLoader("mods" + File.separator + "myignore" + File.separator + Minecraft.getMinecraft().getSession().getProfile().getId() + File.separator);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            this.configLoader.loadOptions();
            this.configLoader.loadIgnoreMe();
        });
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            registerEvents(this);
            registerCommands(new CommandIgnore());
        });
    }

    // EVENTS START

    @SubscribeEvent
    public void onClientConnectToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (!event.isLocal && mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.endsWith(".hypixel.net")) {
            isHypixel = true;
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        isHypixel = false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatReceive(ClientChatReceivedEvent event) {
        String message = ChatColor.stripColor(event.message.getUnformattedText());

        if (message.isEmpty()) {
            return;
        }

        Matcher matcher = chatPattern.matcher(message);

        if (matcher.matches()) {
            String player = matcher.group("player");

            if (ignoreMe.isIgnored(player)) {
                event.setCanceled(true);
            }
            return;
        }

        if (hypixelIgnorePatternSucceed.matcher(message).matches() || hypixelIgnorePatternFail.matcher(message).matches()) {
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

    // OOP END
}
