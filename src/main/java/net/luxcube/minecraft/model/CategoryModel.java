package net.luxcube.minecraft.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.luxcube.minecraft.adapter.ItemStackAdapter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@AllArgsConstructor
public class CategoryModel {

  public static CategoryModel constructModel(@NotNull ConfigurationSection section) {
    ConfigurationSection progressIconSection = section.getConfigurationSection("progress-icon");
    checkArgument(progressIconSection != null, "Missing icon section");

    ConfigurationSection categoriesIconSection = section.getConfigurationSection("categories-icon");
    checkArgument(categoriesIconSection != null, "Missing icon section");

    ItemStack progressIcon = ItemStackAdapter.fromSection(progressIconSection),
      categoriesIcon = ItemStackAdapter.fromSection(categoriesIconSection);

    ConfigurationSection allowedTypesSection = section.getConfigurationSection("allowed-types");
    checkArgument(allowedTypesSection != null, "Missing allowed types section");

    Map<Material, Double> prices = new EnumMap<>(Material.class);
    for (@NotNull String key : allowedTypesSection.getKeys(false)) {
      Material material = Material.matchMaterial(key);
      checkArgument(material != null, "Invalid material: " + key);

      double price = allowedTypesSection.getDouble(key + ".price-per-unit", 0);

      prices.put(material, price);
    }

    double baseMultiplier = section.getDouble("base-multiplier", 1.0);
    checkArgument(baseMultiplier > 0, "Base multiplier must be greater than 0");

    int inventorySlot = section.getInt("inventory-slot", 37);
//    checkArgument(inventorySlot > 36, "Inventory slot must be greater than 36");

    String progressTitle = section.getString("progress-title", "Progress");

    return new CategoryModel(section.getName(), progressIcon, categoriesIcon, prices, progressTitle, baseMultiplier, inventorySlot);
  }

  private final String name;

  private final ItemStack progressIcon;
  private final ItemStack categoriesIcon;

  private final Map<Material, Double> prices;

  private final String progressTitle;

  private final double baseMultiplier;
  private final int inventorySlot;

  public double getMultiplier(int level) {
    return baseMultiplier + (0.1 * level);
  }

  public boolean contains(@NotNull Material material) {
    return prices.containsKey(material);
  }

}
