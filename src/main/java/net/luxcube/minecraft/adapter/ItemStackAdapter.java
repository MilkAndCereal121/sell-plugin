package net.luxcube.minecraft.adapter;

import net.luxcube.minecraft.util.Colors;
import net.luxcube.minecraft.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemStackAdapter {

  public static ItemStack fromSection(@NotNull ConfigurationSection section) {
    Material type;
    try {
      type = Material.valueOf(section.getString("type"));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid material type: " + section.getString("type"));
    }

    int amount = section.getInt("amount", 1);
    if (amount < 1) {
      throw new IllegalArgumentException("Invalid amount: " + amount);
    }

    ItemBuilder item = new ItemBuilder(type)
      .amount(amount);

    String displayName = section.getString("display-name");
    if (displayName != null && !displayName.isEmpty()) {
      displayName = Colors.translateHex(displayName);
      item.name(displayName);
    }

    List<String> lore = section.getStringList("lore");
    if (lore != null && !lore.isEmpty()) {
      lore.replaceAll(Colors::translateHex);
      item.lore(lore);
    }

    ItemFlag[] itemFlags = section.getStringList("flags")
      .stream()
      .map(ItemFlag::valueOf)
      .toArray(ItemFlag[]::new);

    item.itemFlags(itemFlags);

    int customModelData = section.getInt("custom-model-data");
    if (customModelData > 0) {
      item.customModelData(customModelData);
    }

    List<String> enchantments = section.getStringList("enchantments");
    if (!enchantments.isEmpty()) {
      for (String enchantment : enchantments) {
        String[] split = enchantment.split(":");
        if (split.length != 2) {
          throw new IllegalArgumentException("Invalid enchantment: " + enchantment);
        }

        Enchantment enchant = Enchantment.getByName(split[0]);
        item.enchantment(enchant, Integer.parseInt(split[1]));
      }
    }

    return item.result();
  }

}
