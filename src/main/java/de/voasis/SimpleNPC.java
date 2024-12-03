package de.voasis;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

public class SimpleNPC extends EntityCreature {
    public SimpleNPC(Instance instance, Pos position, Component name) {
        super(EntityType.PLAYER);
        setInstance(instance, position);
        setNoGravity(true);
        setCustomNameVisible(true);
        setCustomName(name);
    }
}