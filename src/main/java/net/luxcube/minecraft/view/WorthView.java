package net.luxcube.minecraft.view;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import me.saiintbrisson.minecraft.*;
import net.luxcube.minecraft.SellPlugin;
import net.luxcube.minecraft.util.ItemBuilder;
import net.luxcube.minecraft.util.NumberFormatter;
import net.luxcube.minecraft.util.ViewUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.luxcube.minecraft.util.Colors.translateHex;

public class WorthView extends PaginatedView<WorthView.Entry> {

  private final static ItemStack NEXT_PAGE = new ItemBuilder(Material.ARROW)
    .name(translateHex("&aɴᴇxᴛ"))
    .lore(translateHex("&fClick to go to the next page"))
    .result();

  private final static ItemStack PREVIOUS_PAGE = new ItemBuilder(Material.ARROW)
    .name(translateHex("&aᴘʀᴇᴠɪᴏᴜs"))
    .lore(translateHex("&fClick to go to the previous page"))
    .result();

  private final SellPlugin sellPlugin;

  public WorthView(@NotNull SellPlugin sellPlugin) {

    this.sellPlugin = sellPlugin;
    ViewUtils.cancelAllDefaultActions(this);

    setLayout(
      "OOOOOOOOO",
      "OOOOOOOOO",
      "OOOOOOOOO",
      "OOOOOOOOO",
      "OOOOOOOOO",
      "XXX<^>XXX"
    );

    setPreviousPageItem((paginatedViewContext, viewItem) -> {
      viewItem.onRender(render -> {
        render.setItem(
          PREVIOUS_PAGE
        );
      });

      viewItem.onClick(click -> {
        if (paginatedViewContext.hasPreviousPage()) {
          paginatedViewContext.switchToPreviousPage();
        }
      });
    });

    setNextPageItem((paginatedViewContext, viewItem) -> {
      viewItem.onRender(render -> {
        render.setItem(
          NEXT_PAGE
        );
      });

      viewItem.onClick(click -> {
        if (paginatedViewContext.hasNextPage()) {
          paginatedViewContext.switchToNextPage();
        }
      });
    });

    setLayout('^', viewItem -> {
      viewItem.onRender(render -> {
        render.setItem(
          new ItemBuilder(Material.ANVIL)
            .name(translateHex("&aɪᴛᴇᴍ ᴘʀɪᴄᴇs"))
            .lore(
             translateHex("&fClick to sort"),
              "",
              translateHex(getSortType(render).getDescription())
            ).result()
        );
      }).onClick(click -> {

        Player player = click.getPlayer();
        player.closeInventory();

        SortType sortType = switch (getSortType(click)) {
          case BY_NAME -> SortType.LOWEST_PRICE;
          case LOWEST_PRICE -> SortType.HIGHEST_PRICE;
          case HIGHEST_PRICE -> SortType.BY_NAME;
        };

        sellPlugin.getViewFrame().open(
          WorthView.class,
          player,
          ImmutableMap.of(
            "sort_type", sortType
          )
        );

      });
    });

  }

  @Override
  protected void onOpen(@NotNull OpenViewContext context) {
    context.setContainerSize(6 * 9);
    context.setContainerTitle(translateHex("&8ɪᴛᴇᴍ ᴘʀɪᴄᴇs"));
  }

  @Override
  protected void onRender(@NotNull ViewContext context) {

    // To avoid dupes
    Map<Material, Entry> entries = new HashMap<>();

    sellPlugin.getCategoryRegistry()
      .getCategoryModels()
      .forEach(categoryModel -> {
        for (Map.Entry<Material, Double> materialDoubleEntry : categoryModel.getPrices().entrySet()) {
          entries.put(materialDoubleEntry.getKey(), new Entry(materialDoubleEntry.getKey(), materialDoubleEntry.getValue()));
        }
      });

    SortType sortType = getSortType(context);
    List<Entry> sorted;

    if (sortType == SortType.BY_NAME) {
      sorted = entries.values().stream()
        .sorted(Comparator.comparing(e -> e.getMaterial().name()))
        .toList();
    } else if (sortType == SortType.HIGHEST_PRICE) {
      sorted = entries.values().stream()
        .sorted(Comparator.comparingDouble(Entry::getWorth).reversed())
        .toList();
    } else {
      sorted = entries.values().stream()
        .sorted(Comparator.comparingDouble(Entry::getWorth))
        .toList();
    }

    context.paginated()
      .setSource(sorted);

  }

  @Override
  protected void onItemRender(
    @NotNull PaginatedViewSlotContext<Entry> context,
    @NotNull ViewItem viewItem,
    @NotNull Entry entry
  ) {

    viewItem.onRender(render -> {
      render.setItem(
        new ItemBuilder(entry.getMaterial())
          .name(translateHex("&f" + getFancy(entry.getMaterial().name())))
          .lore(translateHex("&fPrice:&a $" + NumberFormatter.formatSuffixed(entry.getWorth())))
          .result()
      );
    });

  }

  @Data
  public static class Entry {
    private final Material material;
    private final double worth;
  }

  public SortType getSortType(@NotNull ViewContext viewContext) {
    return viewContext.get("sort_type");
  }

  public enum SortType {
    BY_NAME("&7(By Name)"),
    LOWEST_PRICE("&7(Lowest Price)"),
    HIGHEST_PRICE("&7(Highest Price)"),
    ;

    private final String description;

    SortType(@NotNull String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  private String getFancy(@NotNull String name) {
    name = name.replace("_", " ")
      .toLowerCase();

    return capitalizeFirstLetters(name);
  }

  private String capitalizeFirstLetters(String input) {
    String[] words = input.split("\\s+");
    StringBuilder output = new StringBuilder();

    for (String word : words) {
      String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
      output.append(capitalizedWord).append(" ");
    }

    return output.toString().trim();
  }


}
