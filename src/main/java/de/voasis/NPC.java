package de.voasis;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Map;

public class NPC extends Entity {

    private final String nameNPC;
    private final PlayerSkin cachedSkin;

    public NPC(String name, Instance instance, Pos spawn, String skinName) {
        super(EntityType.PLAYER);
        cachedSkin = PlayerSkin.fromUsername(skinName);
        nameNPC = name;
        setBoundingBox(0, 0, 0);
        setNoGravity(true);
        setInstance(instance);
        scheduleNextTick(entity -> teleport(spawn));
        Main.nebulaAPI.createNametag(this, "<bold>"+nameNPC+"</bold>");
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();
        properties.add(new PlayerInfoUpdatePacket.Property("textures", cachedSkin.getTextures(), cachedSkin.getSignature()));
        var entry = new PlayerInfoUpdatePacket.Entry(getUuid(), nameNPC, properties, false, 0, GameMode.CREATIVE, null, null, 1);
        player.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry));
        super.updateNewViewer(player);
        player.sendPackets(new EntityMetaDataPacket(getEntityId(), Map.of(17, Metadata.Byte((byte) 127))));
    }
}