package de.voasis;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;

public class NameTagHandler {
    public NameTagHandler() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, event -> event.getPlayer().getPassengers().forEach(Entity::remove));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, event -> {
            String identifier = event.getIdentifier();
            String message = event.getMessageString();
            if(!identifier.equals("nebula:main")) { return; }
            System.out.println("Channel: " + identifier + " Message:");
            System.out.println(message);
            Entity display = new Entity(EntityType.INTERACTION);
            String playerName = message.split(":")[0];
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
            if(player != null && player.getPassengers().isEmpty()) {
                player.setDisplayName(MiniMessage.miniMessage().deserialize(message.split(":")[1].split("#")[2] + player.getUsername()));
                InteractionMeta displayMeta = (InteractionMeta) display.getEntityMeta();
                displayMeta.setInvisible(true);
                displayMeta.setCustomName(player.getDisplayName());
                displayMeta.setCustomNameVisible(true);
                player.addPassenger(display);
            }
        });
    }
}