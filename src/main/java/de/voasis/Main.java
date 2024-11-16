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
        File worldDir = new File("world");
        if (!worldDir.exists()) {
            worldDir.mkdirs();
            try {
                URL regionUrl = Main.class.getResource("/world.region");
                if (regionUrl != null) {
                    File regionDir = new File(worldDir, "region");
                    regionDir.mkdirs();
                    try (InputStream in = Main.class.getResourceAsStream("/world.region");
                         BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                        String resource;
                        while ((resource = reader.readLine()) != null) {
                            if (resource.endsWith(".mca")) {
                                try (InputStream fileStream = Main.class.getResourceAsStream("/world.region/" + resource)) {
                                    if (fileStream != null) {
                                        Files.copy(fileStream, new File(regionDir, resource).toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to copy region folder: " + e.getMessage());
                e.printStackTrace();
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