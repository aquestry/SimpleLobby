package de.voasis;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Map;

public class NPC extends Entity {
    private final String nameNPC;
    public NPC(String name, Instance instance, Pos spawn) {
        super(EntityType.PLAYER);
        nameNPC = name;
        setBoundingBox(0, 0, 0);
        setNoGravity(true);
        setInstance(instance);
        scheduleNextTick(entity -> teleport(spawn));
        Entity entity = new Entity(EntityType.TEXT_DISPLAY);
        TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();
        meta.setText(MiniMessage.miniMessage().deserialize("<bold>"+nameNPC+"</bold>"));
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setBackgroundColor(0x00000000);
        meta.setShadow(true);
        meta.setTranslation(new Vec(0, 0.3, 0));
        this.addPassenger(entity);
    }
    @Override
    public void updateNewViewer(@NotNull Player player) {
        var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();
        var entry = new PlayerInfoUpdatePacket.Entry(getUuid(), nameNPC, properties, false, 0, GameMode.CREATIVE, null, null, 1);
        player.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry));
        super.updateNewViewer(player);
        player.sendPackets(new EntityMetaDataPacket(getEntityId(), Map.of(17, Metadata.Byte((byte) 127))));
    }
}