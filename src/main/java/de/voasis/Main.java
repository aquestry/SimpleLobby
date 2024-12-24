package de.voasis;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static NameTagHandler nameTagHandler;
    public static void main(String[] args) {
        var server = MinecraftServer.init();
        var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK));
        var vsecret = System.getenv("PAPER_VELOCITY_SECRET");
        if (vsecret != null) { VelocityProxy.enable(vsecret); }
        instance.setChunkSupplier(LightingChunk::new);
        nameTagHandler = new NameTagHandler();
        NPC parkourNPC = new NPC("Parkour", instance, new Pos(1.5, 1, 8.5, 180, 0), "BastiGHG");
        NPC duelsNPC = new NPC("Duels", instance, new Pos(-1.5, 1, 8.5, 180, 0), "Wichtiger");
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(0.5, 2, 0.5));
        });
        MinecraftServer.getGlobalEventHandler().addListener(EntityAttackEvent.class, event -> {
            if(event.getEntity() instanceof Player player) {
                if(event.getTarget().equals(parkourNPC)) {
                    System.out.println("Parkour got clicked!");
                    String message = "queue:" + player.getUsername() + ":Parkour";
                    PluginMessagePacket packet = new PluginMessagePacket(
                            "nebula:main",
                            message.getBytes(StandardCharsets.UTF_8)
                    );
                    player.sendPacket(packet);
                }
                if(event.getTarget().equals(duelsNPC)) {
                    System.out.println("Duels got clicked!");
                    String message = "queue:" + player.getUsername() + ":Duels";
                    PluginMessagePacket packet = new PluginMessagePacket(
                            "nebula:main",
                            message.getBytes(StandardCharsets.UTF_8)
                    );
                    player.sendPacket(packet);
                }
            }
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, event -> event.setCancelled(true));
        server.start("0.0.0.0", 25565);
    }
}