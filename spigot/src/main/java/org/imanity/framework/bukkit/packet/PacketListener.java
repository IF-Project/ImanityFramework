package org.imanity.framework.bukkit.packet;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;

public interface PacketListener {

    Class<?>[] type();

    default boolean read(Player player, WrappedPacket packet) {
        return true;
    }

    default boolean write(Player player, WrappedPacket packet) {
        return true;
    }

}
