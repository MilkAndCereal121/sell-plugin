package net.luxcube.minecraft.vo;

import net.luxcube.minecraft.adapter.ItemStackAdapter;
import net.luxcube.minecraft.util.Colors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static net.luxcube.minecraft.util.Colors.translateHex;

public record SellVO(
  @NotNull String licenseKey,
  @NotNull String listCategoryInventoryName,
  @NotNull String sellHistoryInventoryName,
  @NotNull Map<ProgressStatus, ItemStack> statusToItem,
  @NotNull Map<String, String> messages,
  char completeChar,
  char incompleteChar,
  @NotNull String filledPrefix,
  @NotNull String emptyPrefix,
  int maxBars,
  @NotNull ItemStack nextPageItem,
  @NotNull ItemStack previousPageItem,
  @NotNull List<String> spentLore
  ) {

  @NotNull
  public static SellVO constructObject(@NotNull FileConfiguration fileConfiguration) {
    ConfigurationSection inventoryNameSection = fileConfiguration.getConfigurationSection("inventories");
    checkArgument(inventoryNameSection != null, "Inventories section not found");

    String listCategoryInventoryName = inventoryNameSection.getString("list-category-title"),
      sellHistoryInventoryName = inventoryNameSection.getString("sell-history-title");

    checkArgument(!listCategoryInventoryName.isEmpty(), "List category inventory name is empty");
    checkArgument(!sellHistoryInventoryName.isEmpty(), "Sell history inventory name is empty");

    listCategoryInventoryName = translateHex(listCategoryInventoryName);
    sellHistoryInventoryName = translateHex(sellHistoryInventoryName);

    ConfigurationSection progressBarSection = fileConfiguration.getConfigurationSection("progress-bar");
    checkArgument(progressBarSection != null, "Progress bar section not found");

    char completeChar = progressBarSection.getString("complete-char")
      .charAt(0),
      incompleteChar = progressBarSection.getString("incomplete-char")
        .charAt(0);

    String filledPrefix = progressBarSection.getString("filled-prefix"),
      emptyPrefix = progressBarSection.getString("empty-prefix");

    filledPrefix = translateHex(filledPrefix);
    emptyPrefix = translateHex(emptyPrefix);

    int maxBars = progressBarSection.getInt("max-bars");
    checkArgument(maxBars > 0, "Max bars must be greater than 0");

    ConfigurationSection progressItemsSection = fileConfiguration.getConfigurationSection("progress-items");
    checkArgument(progressItemsSection != null, "Progress items section not found");

    Map<ProgressStatus, ItemStack> statusToItem = new EnumMap<>(ProgressStatus.class);
    for (@NotNull ProgressStatus status : ProgressStatus.values()) {
      ConfigurationSection itemSection = progressItemsSection.getConfigurationSection(status.name());
      checkArgument(itemSection != null, "Item section not found for status " + status.name());

      ItemStack item = ItemStackAdapter.fromSection(itemSection);
      checkArgument(item != null, "Item not found for status " + status.name());

      statusToItem.put(status, item);
    }

    ConfigurationSection messagesSection = fileConfiguration.getConfigurationSection("messages");
    checkArgument(messagesSection != null, "Messages section not found");

    Map<String, String> messages = new HashMap<>();
    for (@NotNull String key : messagesSection.getKeys(false)) {
      checkArgument(!key.isEmpty(), "Message key is empty");

      messages.put(key, translateHex(messagesSection.getString(key)));
    }

    ConfigurationSection historyInventorySection = fileConfiguration.getConfigurationSection("history-inventory");
    checkArgument(historyInventorySection != null, "History inventory section not found");

    ConfigurationSection nextPageItemSection = historyInventorySection.getConfigurationSection("next-page-item");
    checkArgument(nextPageItemSection != null, "Next page item section not found");

    ItemStack nextPageItem = ItemStackAdapter.fromSection(nextPageItemSection);
    checkArgument(nextPageItem != null, "Next page item not found");

    ConfigurationSection previousPageItemSection = historyInventorySection.getConfigurationSection("previous-page-item");
    checkArgument(previousPageItemSection != null, "Previous page item section not found");

    ItemStack previousPageItem = ItemStackAdapter.fromSection(previousPageItemSection);
    checkArgument(previousPageItem != null, "Previous page item not found");

    List<String> spentLore = historyInventorySection.getStringList("spent-lore");
    spentLore.replaceAll(Colors::translateHex);

    return new SellVO(
      fileConfiguration.getString("license-key", "null"),
      listCategoryInventoryName,
      sellHistoryInventoryName,
      statusToItem,
      messages,
      completeChar,
      incompleteChar,
      filledPrefix,
      emptyPrefix,
      maxBars,
      nextPageItem,
      previousPageItem,
      spentLore
    );
  }

  @NotNull
  public ItemStack getItem(@NotNull ProgressStatus status) {
    return statusToItem.get(status)
      .clone();
  }

  @Nullable
  public String getMessage(@NotNull String key) {
    return messages.get(key);
  }

  @Nullable
  public String getMessage(@NotNull String key, @NotNull Object... args) {
    String message = messages.get(key);
    if (message == null) {
      return null;
    }

    if (args.length % 2 != 0) {
      throw new IllegalArgumentException("Invalid arguments");
    }

    for (int index = 0; index < args.length; index += 2) {
      message = message.replace("%" + args[index] + "%", args[index + 1].toString());
    }

    return message;
  }

  public enum ProgressStatus {
    WORKING,
    INCOMPLETE,
    COMPLETE;
  }

}
