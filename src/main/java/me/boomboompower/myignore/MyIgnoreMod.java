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
import me.boomboompower.myignore.listeners.ChatReceived;
import me.boomboompower.myignore.listeners.ClientConnected;
import me.boomboompower.myignore.utils.ChatColor;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

@Mod(modid = MyIgnoreMod.MOD_ID, version = MyIgnoreMod.MOD_VERSION, acceptedMinecraftVersions = "*")
public class MyIgnoreMod {

    public static final String MOD_ID = "myignore_boom";
    public static final String MOD_VERSION = "1.1";

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
            registerEvents(new ChatReceived(), new ClientConnected());
            registerCommands(new CommandIgnore());
        });
    }

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

    public static void sendMessageToPlayer(String message, Object... replacements) {
        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(String.format(message, replacements)));
    }
}
