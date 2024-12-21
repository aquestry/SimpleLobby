package de.voasis;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class NametagEntity extends Entity {
    public static final Tag<NametagEntity> NAMETAG_TAG = Tag.Transient("nametag");
    public NametagEntity(Player player) {
        super(EntityType.INTERACTION);
        InteractionMeta meta = (InteractionMeta) this.getEntityMeta();
        meta.setCustomNameVisible(true);
        Entity test = new Entity(EntityType.TEXT_DISPLAY);
        if (player.getDisplayName() != null) {
            meta.setCustomName(player.getDisplayName());
        } else {
            meta.setCustomName(Component.text("Loading..."));
        }
        meta.setHeight(0.1F);
        meta.setWidth(0.1F);
        meta.setPose(EntityPose.SNIFFING);
        player.setTag(NAMETAG_TAG, this);
        player.addPassenger(this);
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        this.scheduler().scheduleNextTick(() -> {
            if (player.isDead()) return;
            player.sendPacket(new EntityMetaDataPacket(this.getEntityId(), Map.of(9, Metadata.Float(99999999))));
        });
    }
}