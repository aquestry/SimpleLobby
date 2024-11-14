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
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var server = MinecraftServer.init();
        Path lobbyPath = Path.of("lobby");
        var instance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader(lobbyPath));
        var vsecret = System.getenv("PAPER_VELOCITY_SECRET");
        if (vsecret != null) {
            VelocityProxy.enable(vsecret);
            System.out.println("v-secret: " + vsecret);
        }
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(383.5, 41, 517.5, 90,0));
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, event -> event.setCancelled(true));
        instance.setChunkSupplier(LightingChunk::new);
        server.start("0.0.0.0", 25565);
    }
}
