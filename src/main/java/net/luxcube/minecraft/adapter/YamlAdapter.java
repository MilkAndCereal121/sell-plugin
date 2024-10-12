package net.luxcube.minecraft.adapter;

import net.luxcube.minecraft.entity.SellerEntity;
import net.luxcube.minecraft.entity.history.SellHistory;
import net.luxcube.minecraft.entity.info.SellingInfo;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlAdapter {

  public static SellerEntity parsePlayer(@NotNull UUID uniqueId, @NotNull FileConfiguration configuration) {
    ConfigurationSection section = configuration.getConfigurationSection("info");

    Map<String, SellingInfo> infoMap = new HashMap<>();
    for (@NotNull String key : section.getKeys(false)) {
      ConfigurationSection infoSection = section.getConfigurationSection(key);
      if (infoSection == null) {
        continue;
      }

      SellingInfo info = SellingInfo.constructModel(infoSection);
      infoMap.put(key, info);
    }

    ConfigurationSection historySection = configuration.getConfigurationSection("history");

    Map<Material, SellHistory> historyMap = new HashMap<>();
    for (@NotNull String key : historySection.getKeys(false)) {
      ConfigurationSection historyItemSection = historySection.getConfigurationSection(key);
      if (historyItemSection == null) {
        continue;
      }

      SellHistory history = SellHistory.constructModel(historyItemSection);
      historyMap.put(Material.getMaterial(key), history);
    }

    return new SellerEntity(uniqueId, infoMap, historyMap);
  }

  public static void parsePlayer(@NotNull SellerEntity sellerEntity, @NotNull FileConfiguration configuration) {
    ConfigurationSection section = configuration.createSection("info");

    Map<String, SellingInfo> infoMap = sellerEntity.getInfoMap();
    for (@NotNull Map.Entry<String, SellingInfo> entry : infoMap.entrySet()) {
      SellingInfo info = entry.getValue();

      ConfigurationSection infoSection = section.createSection(entry.getKey());
      infoSection.set("current-level", info.getCurrentLevel());
      infoSection.set("earned", info.getEarned());
    }

    ConfigurationSection historySection = configuration.createSection("history");

    Map<Material, SellHistory> historyMap = sellerEntity.getHistoryMap();
    for (@NotNull Map.Entry<Material, SellHistory> entry : historyMap.entrySet()) {
      SellHistory history = entry.getValue();

      ConfigurationSection historyItemSection = historySection.createSection(
        entry.getKey()
          .name()
      );

      historyItemSection.set("total-amount", history.getTotalAmount());
      historyItemSection.set("total-price", history.getTotalPrice());
    }
  }

}
