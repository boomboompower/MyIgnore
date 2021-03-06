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

package me.boomboompower.myignore.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import me.boomboompower.myignore.MyIgnoreMod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ConfigLoader {

    /**
     * A Splitter that splits a string on the first ":".  For example, "a:b:c" would split into ["a", "b:c"].
     */
    private static final Splitter signSplitter = Splitter.on(':').limit(2);

    private File ignoreFile;
    private File optionsFile;

    public ConfigLoader(String directory) {
        directory = directory.replace('/', File.separatorChar);

        File e = new File(directory);
        if (!e.exists()) {
            e.mkdirs();
        }

        this.optionsFile = new File(directory + "options.mi");
        this.ignoreFile = new File(directory + "ignore.mi");
    }

    public void loadIgnoreMe() {
        try {
            TreeMap<String, String> tempHash = new TreeMap<>();
            if (exists(this.ignoreFile)) {
                BufferedReader reader = new BufferedReader(new FileReader(this.ignoreFile));

                for (String s : reader.lines().collect(Collectors.toList())) {
                    String[] astring = Iterables.toArray(signSplitter.split(s), String.class);

                    if (astring != null && astring.length == 2) {
                        tempHash.put(astring[0], astring[1]);
                    }
                }
            }
            MyIgnoreMod.getInstance().getIgnoreMe().in(tempHash);
        } catch (Exception ex) {
            saveIgnoreMe();
        }
    }

    public void saveIgnoreMe() {
        try {
            FileWriter e = new FileWriter(this.ignoreFile);
            BufferedWriter writer = new BufferedWriter(e);

            TreeMap<String, String> players = MyIgnoreMod.getInstance().getIgnoreMe().getPlayerIgnores();

            for (Map.Entry<String, String> entry : players.entrySet()) {
                writer.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
            }

            writer.close();
            e.close();
        } catch (Exception ex) {
            log("Could not save IgnoreMe.");
            ex.printStackTrace();
        }
    }

    public boolean exists(File file) {
        return Files.exists(Paths.get(file.getPath()));
    }

    public File getToggleFile() {
        return this.optionsFile;
    }

    public File getIgnoreMeFile() {
        return ignoreFile;
    }

    protected void log(String message, Object... replace) {
        System.out.println(String.format("[ConfigLoader] " + message, replace));
    }
}
