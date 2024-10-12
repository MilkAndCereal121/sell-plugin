package net.luxcube.minecraft.service;

import net.luxcube.minecraft.entity.SellerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class SellerService {

  private final Map<UUID, SellerEntity> entities = new Hashtable<>();

  @NotNull
  public SellerEntity getSellerEntity(@NotNull UUID uniqueId) {
    return entities.get(uniqueId);
  }

  public void put(@NotNull SellerEntity sellerEntity) {
    entities.put(sellerEntity.getUniqueId(), sellerEntity);
  }

  public void remove(@NotNull SellerEntity sellerEntity) {
    entities.remove(sellerEntity.getUniqueId());
  }

  public void remove(@NotNull UUID uniqueId) {
    entities.remove(uniqueId);
  }

  public boolean contains(@NotNull UUID uniqueId) {
    return entities.containsKey(uniqueId);
  }

  public Collection<SellerEntity> getSellerEntities() {
    return entities.values();
  }

}
