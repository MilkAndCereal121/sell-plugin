package net.luxcube.minecraft.entity.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

@Getter
@Setter
@AllArgsConstructor
public class SellingInfo {

  private int currentLevel;

  private double earned;

  public static SellingInfo constructModel(ConfigurationSection infoSection) {
    int currentLevel = infoSection.getInt("current-level", -1);
    if (currentLevel < 0) {
      throw new IllegalArgumentException("Invalid current level: " + currentLevel);
    }

    double earned = infoSection.getDouble("earned", 0.0);
    if (earned < 0) {
      throw new IllegalArgumentException("Invalid earned: " + earned);
    }

    return new SellingInfo(currentLevel, earned);
  }
}
