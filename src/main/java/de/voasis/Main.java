package de.voasis;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting world initialization...");

        File worldDir = new File("world");
        System.out.println("World directory path: " + worldDir.getAbsolutePath());
        worldDir.mkdirs();

        File regionDir = new File(worldDir, "region");
        System.out.println("Region directory path: " + regionDir.getAbsolutePath());
        regionDir.mkdirs();

        // Liste aller möglichen MCA-Dateien
        List<String> mcaFiles = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                mcaFiles.add(String.format("r.%d.%d.mca", x, z));
            }
        }

        // Versuche jede mögliche MCA-Datei zu kopieren
        ClassLoader classLoader = Main.class.getClassLoader();
        for (String mcaFile : mcaFiles) {
            try (InputStream is = classLoader.getResourceAsStream("world.region/" + mcaFile)) {
                if (is != null) {
                    System.out.println("Found and copying: " + mcaFile);
                    File outFile = new File(regionDir, mcaFile);
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    System.out.println("Successfully copied: " + mcaFile);
                }
            } catch (IOException e) {
                System.out.println("Could not copy " + mcaFile + ": " + e.getMessage());
            }
        }

        // Liste die tatsächlich kopierten Dateien auf
        System.out.println("Content of region directory:");
        File[] files = regionDir.listFiles();
        if (files != null) {
            for (File file : files) {
                System.out.println("- " + file.getName() + " (" + file.length() + " bytes)");
            }
        }

        var server = MinecraftServer.init();
        var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkLoader(new AnvilLoader(worldDir.getPath()));

        var vsecret = System.getenv("PAPER_VELOCITY_SECRET");
        if (vsecret != null) { VelocityProxy.enable(vsecret); }

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(0, 41, 517.5, 90, 0));
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, event -> event.setCancelled(true));

        instance.setChunkSupplier(LightingChunk::new);
        server.start("0.0.0.0", 25565);
    }
}