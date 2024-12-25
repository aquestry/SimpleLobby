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
import net.minestom.server.scoreboard.Sidebar;

public class NebulaAPI {
    public NebulaAPI() {
        Main.globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> event.getPlayer().getPassengers().forEach(Entity::remove));
        Main.globalEventHandler.addListener(PlayerPluginMessageEvent.class, event -> {
            String identifier = event.getIdentifier();
            String message = event.getMessageString();
            System.out.println("Channel: " + identifier + " Message: " + message);
            if(identifier.equals("nebula:main")) {
                String playerName = message.split(":")[0];
                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
                if(player != null && player.getPassengers().isEmpty()) {
                    String newName = message.split(":")[1].split("#")[2];
                    System.out.println("Player: " + playerName + " New Name: " + newName);
                    createNametag(player, newName + player.getUsername());
                }
            }
            if (identifier.equals("nebula:scoreboard")) {
                String[] scoreboardParts = message.split(":");
                if (scoreboardParts.length >= 3) {
                    String playerName = scoreboardParts[0];
                    String title = scoreboardParts[1];
                    String[] lines = scoreboardParts[2].split("#");
                    Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
                    if (player != null) {
                        Sidebar main = new Sidebar(MiniMessage.miniMessage().deserialize(title));
                        for (int i = 0; i < lines.length; i++) {
                            main.createLine(new Sidebar.ScoreboardLine(
                                    "",
                                    Main.mm.deserialize(lines[i]),
                                    lines.length - i,
                                    Sidebar.NumberFormat.blank()
                            ));
                        }
                        main.addViewer(player);
                        System.out.println("Scoreboard set for player: " + playerName);
                    } else {
                        System.err.println("Player not found: " + playerName);
                    }
                } else {
                    System.err.println("Invalid scoreboard message format: " + message);
                }
            }
        });
    }
    public void createNametag(Object entityHolder, String name) {
        Component displayName = Main.mm.deserialize(name);
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