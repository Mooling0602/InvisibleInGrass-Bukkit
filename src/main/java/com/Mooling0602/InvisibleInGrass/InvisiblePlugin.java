package com.Mooling0602.InvisibleInGrass;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class InvisiblePlugin extends JavaPlugin {

    private static final String METADATA_KEY = "InvisibleByPlugin";

    @Override
    public void onEnable() {
        if (isFolia()) {
            getServer().getGlobalRegionScheduler().runAtFixedRate(this, task ->
                getServer().getOnlinePlayers().forEach(this::processPlayer),
            1, 1);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getOnlinePlayers().forEach(InvisiblePlugin.this::processPlayer);
                }
            }.runTaskTimer(this, 1, 1);
        }
    }

    private void processPlayer(Player player) {
        if (isFolia()) {
            player.getScheduler().run(this, scheduledTask -> checkPlayer(player), null);
        } else {
            checkPlayer(player);
        }
    }

    private void checkPlayer(Player player) {
        boolean inGrass = isInTallGrass(player);
        boolean sneaking = player.isSneaking();

        if (inGrass && sneaking) {
            handleInGrass(player);
        } else {
            handleOutGrass(player);
        }
    }

    private void handleInGrass(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            applyEffect(player);
        }
    }

    private void handleOutGrass(Player player) {
        if (player.hasMetadata(METADATA_KEY)) {
            removeEffect(player);
        }
    }

    private void applyEffect(Player player) {
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.INVISIBILITY, 
            20 * 60,
            0, 
            true, 
            true, 
            true
        ));
        player.setMetadata(METADATA_KEY, new FixedMetadataValue(this, true));
    }

    private void removeEffect(Player player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removeMetadata(METADATA_KEY, this);
    }

    private boolean isInTallGrass(Player player) {
        Block block = player.getLocation().getBlock();
        Material type = block.getType();
        return type == Material.TALL_GRASS || 
               type == Material.FERN || 
               type == Material.LARGE_FERN;
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}