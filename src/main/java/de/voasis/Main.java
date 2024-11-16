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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting world initialization...");

        File worldDir = new File("world");
        System.out.println("World directory path: " + worldDir.getAbsolutePath());
        worldDir.mkdirs();

        File regionDir = new File(worldDir, "region");
        System.out.println("Region directory path: " + regionDir.getAbsolutePath());
        regionDir.mkdirs();

        try {
            // Get the resource path
            URI uri = Main.class.getResource("/world.region").toURI();
            System.out.println("Resource URI: " + uri);

            Path resourcePath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                resourcePath = fileSystem.getPath("/world.region");
            } else {
                resourcePath = Paths.get(uri);
            }

            // Walk through all files in the resources directory
            try (Stream<Path> walk = Files.walk(resourcePath, 1)) {
                walk.filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                String fileName = path.getFileName().toString();
                                if (fileName.endsWith(".mca")) {
                                    System.out.println("Copying: " + fileName);
                                    try (InputStream is = Main.class.getResourceAsStream("/world.region/" + fileName)) {
                                        if (is != null) {
                                            Files.copy(is, new File(regionDir, fileName).toPath(),
                                                    StandardCopyOption.REPLACE_EXISTING);
                                            System.out.println("Successfully copied: " + fileName);
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                System.err.println("Error copying file: " + e.getMessage());
                            }
                        });
            }
        } catch (URISyntaxException | IOException e) {
            System.err.println("Error accessing resources: ");
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