package fr.thipow.undercover.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public class GameUtils {

    public static void clearArmorStands(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ArmorStand) {
                    entity.remove();
                }
            }
        }
    }

    public static Component legacy(String legacyText) {
        return LegacyComponentSerializer.legacySection().deserialize(legacyText);
    }

}
