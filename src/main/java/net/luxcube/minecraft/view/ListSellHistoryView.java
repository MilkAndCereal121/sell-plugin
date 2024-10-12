package net.luxcube.minecraft.view;

import me.saiintbrisson.minecraft.*;
import net.luxcube.minecraft.SellPlugin;
import net.luxcube.minecraft.entity.SellerEntity;
import net.luxcube.minecraft.entity.history.SellHistory;
import net.luxcube.minecraft.util.ItemBuilder;
import net.luxcube.minecraft.util.ViewUtils;
import net.luxcube.minecraft.vo.SellVO;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.luxcube.minecraft.util.NumberFormatter.formatSuffixed;

public class ListSellHistoryView extends PaginatedView<Material> {

  private final SellPlugin sellPlugin;

  public ListSellHistoryView(@NotNull SellPlugin sellPlugin) {
    super(6 * 9, "SELL HISTORY");

    this.sellPlugin = sellPlugin;
    ViewUtils.cancelAllDefaultActions(this);

    setLayout(
      "OOOOOOOOO",
      "OOOOOOOOO",
      "OOOOOOOOO",
      "OOOOOOOOO",
      "OOOOOOOOO",
      "<XXXXXXX>"
    );

    setPreviousPageItem((paginatedViewContext, viewItem) -> {
      viewItem.onRender(render -> {
        render.setItem(
          sellPlugin.getSellVO()
            .previousPageItem()
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
          sellPlugin.getSellVO()
            .nextPageItem()
        );
      });

      viewItem.onClick(click -> {
        if (paginatedViewContext.hasNextPage()) {
          paginatedViewContext.switchToNextPage();
        }
      });
    });
  }

  @Override
  protected void onClick(@NotNull ViewSlotClickContext context) {
    // Check if the player clicked on an empty slot
    if (
      context.getItemWrapper()
        .isEmpty()
    ) {
      return;
    }

    Player player = context.getPlayer();
    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
  }

  @Override
  protected void onOpen(@NotNull OpenViewContext context) {
    SellVO sellVO = sellPlugin.getSellVO();
    context.setContainerTitle(sellVO.sellHistoryInventoryName());
  }

  @Override
  protected void onRender(@NotNull ViewContext context) {
    SellerEntity sellerEntity = getEntity(context);
    if (sellerEntity == null) {
      return;
    }

    List<Material> history = sellerEntity.getHistoryMap()
      .keySet()
      .stream()
      .toList();

    context.paginated()
      .setSource(history);
  }

  @Override
  protected void onItemRender(
    @NotNull PaginatedViewSlotContext<Material> paginatedViewSlotContext,
    @NotNull ViewItem viewItem,
    @NotNull Material material
  ) {
    viewItem.withItem(buildSpentItem(paginatedViewSlotContext, material));
  }

  private Object buildSpentItem(@NotNull ViewContext viewContext, @NotNull Material material) {
    SellerEntity sellerEntity = getEntity(viewContext);
    if (sellerEntity == null) {
      return null;
    }

    SellHistory sellHistory = sellerEntity.getHistory(material);
    if (sellHistory == null) {
      return null;
    }

    return new ItemBuilder(material)
      .lore(
        sellPlugin.getSellVO()
          .spentLore()
      ).placeholders(
        "%spent%", formatSuffixed(sellHistory.getTotalPrice()),
        "%amount%", formatSuffixed(sellHistory.getTotalAmount())
      ).result();
  }

  @Nullable
  private SellerEntity getEntity(@NotNull ViewContext viewContext) {
    return viewContext.get("seller_entity");
  }
}
