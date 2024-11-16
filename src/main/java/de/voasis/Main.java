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
import java.net.URL;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting world initialization...");
        File worldDir = new File("world");
        System.out.println("World directory path: " + worldDir.getAbsolutePath());
        if (!worldDir.exists()) {
            System.out.println("Creating world directory...");
            worldDir.mkdirs();
        }
        File regionDir = new File(worldDir, "region");
        System.out.println("Region directory path: " + regionDir.getAbsolutePath());
        if (!regionDir.exists()) {
            System.out.println("Creating region directory...");
            regionDir.mkdirs();
        }
        try {
            System.out.println("Available resources in JAR:");
            URL resourcesUrl = Main.class.getResource("/world.region");
            System.out.println("Resource URL: " + resourcesUrl);

            ClassLoader classLoader = Main.class.getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("world.region")) {
                if (is == null) {
                    System.out.println("Could not find world.region directory in resources");
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Found resource: " + line);
                    }
                }
            }

            String[] mcaFiles = {
                    "r.0.0.mca", "r.0.-1.mca", "r.-1.0.mca", "r.-1.-1.mca",
                    "r.1.0.mca", "r.1.-1.mca", "r.0.1.mca", "r.-1.1.mca"
            };

            for (String mcaFile : mcaFiles) {
                try (InputStream mcaStream = classLoader.getResourceAsStream("world.region/" + mcaFile)) {
                    if (mcaStream != null) {
                        System.out.println("Found MCA file: " + mcaFile);
                        File targetFile = new File(regionDir, mcaFile);
                        Files.copy(mcaStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Copied " + mcaFile + " to " + targetFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.err.println("Error copying " + mcaFile + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error during resource copying:");
            e.printStackTrace();
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