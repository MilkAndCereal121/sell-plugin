package net.luxcube.minecraft.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.luxcube.minecraft.entity.history.SellHistory;
import net.luxcube.minecraft.entity.info.SellingInfo;
import net.luxcube.minecraft.model.CategoryModel;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SellerEntity {

  private final UUID uniqueId;

  private final Map<String, SellingInfo> infoMap;

  private final Map<Material, SellHistory> historyMap;

  @Nullable
  public SellingInfo getInfo(@NotNull String key) {
    return infoMap.get(key);
  }

  @Nullable
  public SellingInfo getInfo(@NotNull CategoryModel categoryModel) {
    return getInfo(categoryModel.getName());
  }

  @Nullable
  public SellHistory getHistory(@NotNull Material material) {
    return historyMap.get(material);
  }

  public void commit(@NotNull Material material, int total, double price) {
    SellHistory history = historyMap.computeIfAbsent(material, key -> new SellHistory(0, 0));
    history.sum(total, price);
    historyMap.put(material, history);
  }
}
