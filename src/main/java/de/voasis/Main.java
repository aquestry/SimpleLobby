package de.voasis;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import java.io.*;
import java.util.Properties;
import java.util.TreeMap;

public class Main {
    private static String vsecret;
    public static void main(String[] args) {
        loadConfig();
        MinecraftServer minecraftServer = MinecraftServer.init();

        int port = 25565;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p") && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("No Port given, using 25565");
                }
            }
        }

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        VelocityProxy.enable(vsecret);
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        instanceContainer.setChunkSupplier(LightingChunk::new);
        System.out.println("Starting on Port: " + port);
        minecraftServer.start("0.0.0.0", port);
    }

    public static void loadConfig() {
        File configFile = new File("config.properties");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Properties props = new Properties();
                props.load(reader);
                vsecret = props.getProperty("secret","123456");

            } catch (IOException ex) {
                System.out.println("Error loading config: " + ex.getMessage());
            }
        } else {
            setDefault();
            System.out.println("Default-Config created.");
        }
    }
    public static void setDefault() {
        Properties props = new Properties();
        props.setProperty("secret", "123456");

        Properties sortedProps = new Properties();
        sortedProps.putAll(new TreeMap<>(props));
        try (OutputStream out = new FileOutputStream("config.properties")) {
            sortedProps.store(out, "Server Configuration by Aquestry");
        } catch (IOException e) {
            System.out.println("Error creating config: " + e.getMessage());
        }
        loadConfig();
    }
}