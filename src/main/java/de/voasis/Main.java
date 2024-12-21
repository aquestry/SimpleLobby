package de.voasis;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import java.nio.charset.StandardCharsets;

public class Main {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    public static void main(String[] args) {
        Pos npcSpawn = new Pos(0.5, 1, 8.5, 180, 0);
        var server = MinecraftServer.init();
        var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK));
        var vsecret = System.getenv("PAPER_VELOCITY_SECRET");
        if (vsecret != null) { VelocityProxy.enable(vsecret); }
        instance.setChunkSupplier(LightingChunk::new);
        new NPC(instance, npcSpawn);
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(0.5, 2, 0.5));
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, event -> event.getPlayer().getPassengers().forEach(Entity::remove));
        MinecraftServer.getGlobalEventHandler().addListener(EntityAttackEvent.class, event -> {
            if(event.getEntity() instanceof Player player) {
                if(event.getTarget().getPosition().equals(npcSpawn)) {
                    String message = "queue:" + player.getUsername() + ":Parkour";
                    PluginMessagePacket packet = new PluginMessagePacket(
                            "nebula:main",
                            message.getBytes(StandardCharsets.UTF_8)
                    );
                    player.sendPacket(packet);
                }
            }
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, event -> {
            String identifier = event.getIdentifier();
            String message = event.getMessageString();
            Player player = event.getPlayer();
            if(!identifier.equals("nebula:main")) {
                return;
            }
            System.out.println("Channel: " + identifier + " Message:");
            System.out.println(message);
            Entity display = new Entity(EntityType.INTERACTION);
            player.setDisplayName(mm.deserialize(message.split(":")[1].split("#")[2] + player.getUsername()));
            InteractionMeta displayMeta = (InteractionMeta) display.getEntityMeta();
            displayMeta.setInvisible(true);
            displayMeta.setCustomName(player.getDisplayName());
            displayMeta.setCustomNameVisible(true);
            player.addPassenger(display);
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, event -> event.setCancelled(true));
        server.start("0.0.0.0", 25565);
    }
}