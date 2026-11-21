package ru.driftcars;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DriftCarsPlugin extends JavaPlugin implements Listener {

    private final Map<UUID, DriftCar> activeCars = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                activeCars.values().forEach(DriftCar::tick);
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("car")) {
            ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class, as -> {
                as.setInvisible(true);
                as.setInvulnerable(true);
                as.setGravity(true);
                as.getEquipment().setHelmet(new ItemStack(Material.RED_CONCRETE));
            });

            stand.addPassenger(player);
            activeCars.put(stand.getUniqueId(), new DriftCar(stand, player));
            player.sendMessage("§aМашина заспавнена! Управление: WASD, Пробел — ручник (дрифт).");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (event.getDismounted() instanceof ArmorStand stand) {
            DriftCar car = activeCars.remove(stand.getUniqueId());
            if (car != null) {
                stand.remove();
            }
        }
    }
}
