package de.voasis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;

public class NameTagHandler {
    public NameTagHandler() {
        Main.globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> event.getPlayer().getPassengers().forEach(Entity::remove));
        Main.globalEventHandler.addListener(PlayerPluginMessageEvent.class, event -> {
            String identifier = event.getIdentifier();
            String message = event.getMessageString();
            if(!identifier.equals("nebula:main")) { return; }
            System.out.println("Channel: " + identifier + " Message:");
            System.out.println(message);
            String playerName = message.split(":")[0];
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
            if(player != null && player.getPassengers().isEmpty()) {
                String newName = message.split(":")[1].split("#")[2];
                System.out.println("Player: " + playerName + " New Name: " + newName);
                newNametag(player, newName + player.getUsername());
            }
        });
    }
    public void newNametag(Object entityHolder, String name) {
        Component displayName = MiniMessage.miniMessage().deserialize(name);
        if (entityHolder instanceof Player player) {
            player.setDisplayName(displayName);
            createNametag(player, displayName);
        } else if (entityHolder instanceof NPC npc) {
            npc.setCustomName(displayName);
            createNametag(npc, displayName);
        }
    }
    private void createNametag(Object entityHolder, Component displayName) {
        Entity entity = new Entity(EntityType.TEXT_DISPLAY);
        TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();
        meta.setText(displayName);
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setBackgroundColor(0x00000000);
        meta.setShadow(true);
        meta.setTranslation(new Vec(0, 0.3, 0));
        if (entityHolder instanceof Player player) {
            player.addPassenger(entity);
        } else if (entityHolder instanceof NPC npc) {
            npc.addPassenger(entity);
        }
    }
}