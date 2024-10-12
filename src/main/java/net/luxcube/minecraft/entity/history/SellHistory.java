package net.luxcube.minecraft.entity.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkArgument;

@AllArgsConstructor
@Getter
@Setter
public class SellHistory {

  public static SellHistory constructModel(@NotNull ConfigurationSection historyItemSection) {
    int totalAmount = historyItemSection.getInt("total-amount");
    checkArgument(totalAmount > 0, "total-amount must be greater than 0");

    double totalPrice = historyItemSection.getDouble("total-price");
    return new SellHistory(totalPrice, totalAmount);
  }

  private double totalPrice;
  private int totalAmount;


  public void sum(int amount, double basePrice) {
    totalAmount += amount;
    totalPrice += amount * basePrice;
  }

}
