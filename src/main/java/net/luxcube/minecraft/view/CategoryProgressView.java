package net.luxcube.minecraft.view;

import me.saiintbrisson.minecraft.OpenViewContext;
import me.saiintbrisson.minecraft.View;
import me.saiintbrisson.minecraft.ViewContext;
import net.luxcube.minecraft.SellPlugin;
import net.luxcube.minecraft.entity.info.SellingInfo;
import net.luxcube.minecraft.model.CategoryModel;
import net.luxcube.minecraft.util.ItemBuilder;
import net.luxcube.minecraft.util.ProgressBar;
import net.luxcube.minecraft.util.ViewUtils;
import net.luxcube.minecraft.vo.SellVO;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.luxcube.minecraft.util.Colors.translateHex;
import static net.luxcube.minecraft.util.NumberFormatter.formatSuffixed;

public class CategoryProgressView extends View {

  private static final ItemStack BACK_ITEM = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
    .name(translateHex("#e00202ʙᴀᴄᴋ"))
    .lore("§fClick to return")
    .result();

  private static final ItemStack PANEL_ITEM = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
    .name("§7")
    .result();

  private static final int[] PROGRESS_SLOTS = new int[]{
    10, 19, 28, 37, 38, 39, 30,
    21, 12, 13, 14, 23, 32, 41,
    42, 43, 34, 25, 16, 7
  };

  private static final int[] PANEL_SLOTS = new int[]{
    0, 2, 3, 4, 5, 6, 8,
    9, 11, 15, 17, 18, 20,
    22, 24, 26, 27, 29, 31,
    33, 35, 36, 40, 44, 46,
    47, 48, 49, 50, 51, 52,
  };

  private static final int BACK_SLOT = 45;
  private static final int CATEGORY_SLOT = 1;

  private final SellPlugin sellPlugin;

  public CategoryProgressView(@NotNull SellPlugin sellPlugin) {
    super(6 * 9, "ᴘʀᴏɢʀᴇss");

    this.sellPlugin = sellPlugin;
    ViewUtils.cancelAllDefaultActions(this);

    for (int slot : PANEL_SLOTS) {
      slot(slot, PANEL_ITEM);
    }

    slot(BACK_SLOT, BACK_ITEM)
      .onClick(this::handleBackClick);

    slot(CATEGORY_SLOT)
      .onRender(render -> render.setItem(buildCategoryIcon(render)));
  }

  @Nullable
  private SellingInfo getInfo(@NotNull ViewContext viewContext) {
    return viewContext.get("selling_info");
  }

  @Nullable
  private CategoryModel getCategory(@NotNull ViewContext viewContext) {
    return viewContext.get("category_model");
  }


  @Override
  protected void onOpen(@NotNull OpenViewContext context) {
    CategoryModel category = getCategory(context);
    context.setContainerTitle(category.getProgressTitle());
  }

  @Override
  protected void onRender(@NotNull ViewContext context) {
    for (int index = 0; index < PROGRESS_SLOTS.length; index++) {
      ItemStack item = buildProgressIcon(context, index);

      context.slot(PROGRESS_SLOTS[index], item);
    }
  }

  private void handleBackClick(@NotNull ViewContext viewContext) {
    Player player = viewContext.getPlayer();
    viewContext.open(
      ListCategoryModelView.class,
      Map.of(
        "seller_entity",
        sellPlugin.getSellerService()
          .getSellerEntity(player.getUniqueId())
      )
    );
  }

  @Nullable
  private ItemStack buildCategoryIcon(@NotNull ViewContext viewContext) {
    CategoryModel category = getCategory(viewContext);
    if (category == null) {
      return null;
    }

    SellingInfo sellingInfo = getInfo(viewContext);
    int currentLevel = sellingInfo.getCurrentLevel();

    ItemStack item = category.getProgressIcon()
      .clone();

    double price = currentLevel >= sellPlugin.getLevelPrices().size() ? 1 : sellPlugin.getLevelPrices()
      .get(currentLevel)
      .doubleValue();

    double nextMultiplier = category.getMultiplier(currentLevel);

    SellVO sellVO = sellPlugin.getSellVO();
    String progressBar = ProgressBar.generateProgressBar(
      sellVO.filledPrefix(),
      sellVO.emptyPrefix(),
      sellVO.completeChar(),
      sellVO.incompleteChar(),
      (long) Math.floor(sellingInfo.getEarned()),
      price,
      sellVO.maxBars()
    ).getBarString();

    float progress = (float) (sellingInfo.getEarned() / price);
    progress = Math.min(Math.max(progress, 0), 1);

    return new ItemBuilder(item)
      .placeholders(
        "%multiplier%", "%.2f".formatted(nextMultiplier),
        "%progress-bar%", progressBar,
        "%progress%", "%.2f".formatted(progress * 100)
      ).result();
  }

  @Nullable
  private ItemStack buildProgressIcon(@NotNull ViewContext viewContext, int index) {
    SellingInfo info = getInfo(viewContext);
    if (info == null) {
      return null;
    }

    CategoryModel category = getCategory(viewContext);

    double multiplier = category.getMultiplier(index + 1);
    double price = sellPlugin.getLevelPrices()
      .get(index);

    SellVO sellVO = sellPlugin.getSellVO();

    SellVO.ProgressStatus status = index == info.getCurrentLevel()
      ? SellVO.ProgressStatus.WORKING
      : index < info.getCurrentLevel()
      ? SellVO.ProgressStatus.COMPLETE
      : SellVO.ProgressStatus.INCOMPLETE;

    String progressBar = ProgressBar.generateProgressBar(
      sellVO.filledPrefix(),
      sellVO.emptyPrefix(),
      sellVO.completeChar(),
      sellVO.incompleteChar(),
      (long) Math.floor(info.getEarned()),
      price,
      sellVO.maxBars()
    ).getBarString();

    double percentage = info.getEarned() / price;
    percentage = Math.min(Math.max(percentage, 0), 1);

    return new ItemBuilder(sellVO.getItem(status))
      .placeholders(
        "%progress-bar%", progressBar,
        "%multiplier%", "%.2f".formatted(multiplier),
        "%progress%", "%.2f".formatted(percentage * 100),
        "%current-spent%", formatSuffixed(info.getEarned()),
        "%needing-spent%", formatSuffixed(price)
      ).result();
  }

}
