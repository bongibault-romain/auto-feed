package lt.bongibau.autofeed;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Vector;

public final class AutoFeedPlugin extends JavaPlugin implements Listener {

    private final HashMap<Material, Vector<Double>> foodValues = new HashMap<Material, Vector<Double>>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        register("APPLE", 4.0, 2.4);
        register("BAKED_POTATO", 5.0, 6.0);
        register("BEETROOT", 1.0, 1.2);
        register("BREAD", 5.0, 6.0);
        register("CAKE", 2.0, 0.4);
        register("CARROT", 3.0, 3.6);
        register("CHORUS_FRUIT", 4.0, 2.4);
        register("COOKED_CHICKEN", 6.0, 7.2);
        register("COOKED_COD", 5.0, 6.0);
        register("COOKED_MUTTON", 6.0, 9.6);
        register("COOKED_PORKCHOP", 8.0, 12.8);
        register("COOKED_RABBIT", 5.0, 6.0);
        register("COOKED_SALMON", 6.0, 9.6);
        register("COOKIE", 2.0, 0.4);
        register("DRIED_KELP", 1.0, 0.6);
        register("GOLDEN_CARROT", 6.0, 14.4);
        register("HONEY_BOTTLE", 6.0, 1.2);
        register("MELON_SLICE", 2.0, 1.2);
        register("POTATO", 1.0, 0.6);
        register("PUMPKIN_PIE", 8.0, 4.8);
        register("BEEF", 3.0, 1.8);
        register("CHICKEN", 2.0, 1.2);
        register("COD", 2.0, 0.4);
        register("MUTTON", 2.0, 1.2);
        register("PORKCHOP", 3.0, 1.8);
        register("RABBIT", 3.0, 1.8);
        register("SALMON", 2.0, 0.4);
        register("COOKED_BEEF", 8.0, 12.8);
        register("SWEET_BERRIES", 2.0, 0.4);
        register("TROPICAL_FISH", 1.0, 0.2);
    }

    private void register(String materialString, double foodLevel, double saturation) {
        Material material = Material.matchMaterial(materialString);
        
        Vector<Double> values = new Vector<Double>();
        values.add(foodLevel);
        values.add(saturation);
        foodValues.put(material, values);
    }

    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent e) {
        HumanEntity entity = e.getEntity();

        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;

        if (!player.hasPermission("autofeed.feed")) return;

        Inventory inventory = player.getInventory();

        // Check if the player food level is below the maximum
        if (player.getFoodLevel() >= 20) return;

        // Retrieve the item in the player's inventory that has the highest food level
        ItemStack bestItem = null;
        int playerFoodLevel = player.getFoodLevel();
        int maxFoodLevel = 20;
        double bestFoodLevel = 0.0;
        double bestSaturation = 0.0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;

            Material material = item.getType();
            if (!foodValues.containsKey(material)) continue;

            Vector<Double> values = foodValues.get(material);
            double foodLevel = values.get(0);
            double saturation = values.get(1);

            if ((playerFoodLevel + foodLevel > maxFoodLevel) && playerFoodLevel > 6) continue;

            if (foodLevel > bestFoodLevel || (foodLevel == bestFoodLevel && saturation > bestSaturation)) {
                bestItem = item;
                bestFoodLevel = foodLevel;
                bestSaturation = saturation;
            }
        }

        if (bestItem == null) return;

        // Consume the item
        int amount = bestItem.getAmount();
        if (amount > 1) {
            bestItem.setAmount(amount - 1);
        } else {
            bestItem.setAmount(1);
            inventory.remove(bestItem);
        }

        // Set the food level and saturation
        e.setCancelled(true);
        player.setFoodLevel((int) bestFoodLevel + player.getFoodLevel());
        player.setSaturation((float) bestSaturation + player.getSaturation());
    }
}
