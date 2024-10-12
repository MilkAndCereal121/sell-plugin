package net.luxcube.minecraft.listener;

import lombok.RequiredArgsConstructor;
import net.luxcube.minecraft.SellPlugin;
import net.luxcube.minecraft.adapter.YamlAdapter;
import net.luxcube.minecraft.entity.SellerEntity;
import net.luxcube.minecraft.entity.info.SellingInfo;
import net.luxcube.minecraft.model.CategoryModel;
import net.luxcube.minecraft.model.registry.CategoryRegistry;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {

  private final SellPlugin sellPlugin;

  @EventHandler(priority = EventPriority.LOWEST)
  public void onAsyncPlayerPreLogin(@NotNull AsyncPlayerPreLoginEvent event) {
    UUID uniqueId = event.getUniqueId();

    SellerEntity sellerEntity;

    File file = new File(sellPlugin.getStorageFile(), uniqueId.toString() + ".yml");
    if (file.exists()) {
      FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
      sellerEntity = YamlAdapter.parsePlayer(
        uniqueId,
        fileConfiguration
      );
    } else {
      CategoryRegistry categoryRegistry = sellPlugin.getCategoryRegistry();

      Map<String, SellingInfo> infoMap = new HashMap<>(categoryRegistry.getCategoryModels().size());
      for (@NotNull CategoryModel model : categoryRegistry.getCategoryModels()) {
        infoMap.put(model.getName(), new SellingInfo(0, 0));
      }

      sellerEntity = new SellerEntity(uniqueId, infoMap, new EnumMap<>(Material.class));
    }

    sellPlugin.getSellerService()
      .put(sellerEntity);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    Player player = event.getPlayer();
    SellerEntity sellerEntity = sellPlugin.getSellerService()
      .getSellerEntity(player.getUniqueId());

    File file = new File(sellPlugin.getStorageFile(), player.getUniqueId() + ".yml");
    FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

    YamlAdapter.parsePlayer(sellerEntity, fileConfiguration);
    try {
      fileConfiguration.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
