package fr.thipow.undercover.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import fr.thipow.undercover.Undercover;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 * Utility class for managing glowing effects on players using ProtocolLib.
 * This class provides methods to add and reset glowing effects for players.
 *
 * @author Thipow
 */

public class GlowUtils {

    private static final ProtocolManager protocolManager = Undercover.getInstance().getProtocolManager();

    private static final Map<Player, Player> glowingTargets = new HashMap<>();

    /**
     * Adds a glowing effect to the target player for the viewer.
     *
     * @param viewer The player who will see the glowing effect.
     * @param target The player who will receive the glowing effect.
     */
    public static void addGlow(Player viewer, Player target) {
        if (protocolManager != null) {
            glowingTargets.put(viewer, target);

            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, target.getEntityId());

            // 0x40 = glowing flag
            byte entityFlags = 0x40;

            WrappedDataValue data = new WrappedDataValue(0, Registry.get(Byte.class), entityFlags);
            packet.getDataValueCollectionModifier().write(0, Collections.singletonList(data));

            protocolManager.sendServerPacket(viewer, packet);
        }
    }

    /**
     * Resets the glowing effect for the viewer, removing the glow from the target player.
     *
     * @param viewer The player whose glowing effect will be reset.
     */
    public static void resetGlow(Player viewer) {
        if (protocolManager != null) {
            Player target = glowingTargets.remove(viewer);
            if (target == null) {
                return;
            }

            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, target.getEntityId());

            // 0x00 = no flags, removing the glowing effect
            byte entityFlags = 0x00;

            WrappedDataValue data = new WrappedDataValue(0, Registry.get(Byte.class), entityFlags);
            packet.getDataValueCollectionModifier().write(0, Collections.singletonList(data));

            protocolManager.sendServerPacket(viewer, packet);
        }
    }

}
