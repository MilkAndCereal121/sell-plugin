package net.luxcube.minecraft.view;

import me.saiintbrisson.minecraft.OpenViewContext;
import me.saiintbrisson.minecraft.View;
import me.saiintbrisson.minecraft.ViewContext;
import me.saiintbrisson.minecraft.ViewSlotClickContext;
import net.luxcube.minecraft.SellPlugin;
import net.luxcube.minecraft.entity.SellerEntity;
import net.luxcube.minecraft.entity.info.SellingInfo;
import net.luxcube.minecraft.model.CategoryModel;
import net.luxcube.minecraft.util.ItemBuilder;
import net.luxcube.minecraft.util.ProgressBar;
import net.luxcube.minecraft.vo.SellVO;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.luxcube.minecraft.util.NumberFormatter.formatSuffixed;

public class ListCategoryModelView extends View {

  private final SellPlugin sellPlugin;

  public ListCategoryModelView(@NotNull SellPlugin sellPlugin) {
    super(5 * 9, "ᴘʟᴀᴄᴇ ɪᴛᴇᴍs ɪɴ ʜᴇʀᴇ ᴛᴏ sᴇʟʟ");

    setCancelOnDrop(true);
    this.sellPlugin = sellPlugin;

    List<CategoryModel> categoryModels = sellPlugin.getCategoryRegistry()
      .getCategoryModels();

    for (@NotNull CategoryModel categoryModel : categoryModels) {
      slot(categoryModel.getInventorySlot())
        .onRender(render -> render.setItem(buildCategoryItem(render, categoryModel)))
        .onClick(click -> handleClickCategory(click, categoryModel));
    }
  }

  @Override
  protected void onOpen(@NotNull OpenViewContext context) {
    SellVO sellVO = sellPlugin.getSellVO();
    context.setContainerTitle(sellVO.listCategoryInventoryName());
  }

  @Override
  protected void onClose(@NotNull ViewContext context) {
    SellerEntity sellerEntity = getEntity(context);

    double income = 0d;

    Player player = context.getPlayer();
    ItemStack cursor = player.getItemOnCursor();
    if (cursor.getAmount() != 0) {
      player.getInventory().addItem(cursor.clone());
      player.setItemOnCursor(null);
    }

    Inventory inventory = player.getOpenInventory()
      .getTopInventory();

    Map<Material, Integer> items = new HashMap<>();

    for (int slot = 0; slot < 36; slot++) {
      ItemStack item = inventory.getItem(slot);
      if (item == null || item.getType() == Material.AIR) {
        continue;
      }

      CategoryModel categoryModel = sellPlugin.getCategoryRegistry()
        .from(item.getType());

      if (categoryModel == null) {
        player.getInventory().addItem(item);
        continue;
      }

      items.put(item.getType(), items.getOrDefault(item.getType(), 0) + item.getAmount());
    }

    for (@NotNull Map.Entry<Material, Integer> entry : items.entrySet()) {
      Material material = entry.getKey();

      CategoryModel categoryModel = sellPlugin.getCategoryRegistry()
        .from(material);

      if (categoryModel == null) {
        continue;
      }

      SellingInfo sellingInfo = sellerEntity.getInfo(categoryModel);

      double multiplier = categoryModel.getMultiplier(sellingInfo.getCurrentLevel());
      if (multiplier < 0) {
        multiplier = 1d;
      }

      double price = categoryModel.getPrices()
        .getOrDefault(material, 0D) * entry.getValue();

      sellingInfo.setEarned(sellingInfo.getEarned() + (price * multiplier));

      int currentLevel = sellingInfo.getCurrentLevel(),
        levelPrice = sellPlugin.getLevelPrices()
        .get(Math.min(currentLevel, sellPlugin.getLevelPrices().size() - 1))
          .intValue();

      while(sellingInfo.getEarned() >= levelPrice) {
        if (currentLevel >= sellPlugin.getLevelPrices().size() - 1) {
          break;
        }

        currentLevel++;

        levelPrice = sellPlugin.getLevelPrices()
          .get(Math.min(currentLevel, sellPlugin.getLevelPrices().size() - 1))
          .intValue();
      }

      if (currentLevel != sellingInfo.getCurrentLevel()) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        sellingInfo.setCurrentLevel(currentLevel);
      }

      sellerEntity.commit(material, entry.getValue(), (int) Math.round(price * multiplier));

      income += price * multiplier;
    }

    if (income <= 0) {
      return;
    }

    sellPlugin.getEconomy()
      .depositPlayer(player, income);

    String soldMessage = sellPlugin.getSellVO()
      .getMessage("sold-total", "amount", formatSuffixed(income));

    player.sendMessage(soldMessage);
    player.sendActionBar(soldMessage);
    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
  }

  @Nullable
  private SellerEntity getEntity(@NotNull ViewContext viewContext) {
    return viewContext.get("seller_entity");
  }

  @Nullable
  private ItemStack buildCategoryItem(@NotNull ViewContext viewContext, @NotNull CategoryModel categoryModel) {
    SellerEntity sellerEntity = getEntity(viewContext);
    if (sellerEntity == null) {
      return null;
    }

    SellingInfo sellingInfo = sellerEntity.getInfo(categoryModel);
    if (sellingInfo == null) {
      return null;
    }

    int currentLevel = sellingInfo.getCurrentLevel();

    ItemStack item = categoryModel.getCategoriesIcon()
      .clone();

    double price = currentLevel >= sellPlugin.getLevelPrices().size() ? 1 : sellPlugin.getLevelPrices()
      .get(currentLevel);

    double nextMultiplier = categoryModel.getMultiplier(currentLevel);

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

    float progress = (float) ((float) sellingInfo.getEarned() / price);
    progress = Math.min(Math.max(progress, 0), 1);

    ItemBuilder itemBuilder = new ItemBuilder(item);
    itemBuilder.placeholders(
      "%multiplier%", "%.2f".formatted(nextMultiplier),
      "%progress-bar%", progressBar,
      "%progress%", "%.2f".formatted(progress * 100)
    );

    return itemBuilder.result();
  }

  private void handleClickCategory(@NotNull ViewSlotClickContext viewContext, @NotNull CategoryModel categoryModel) {
    SellerEntity sellerEntity = getEntity(viewContext);
    if (sellerEntity == null) {
      return;
    }

    Player player = viewContext.getPlayer();
    player.setItemOnCursor(null);
    player.closeInventory();

    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(sellPlugin, () -> openCategoryProgressView(player, sellerEntity, categoryModel));
  }

  private void openCategoryProgressView(
    @NotNull Player player,
    @NotNull SellerEntity sellerEntity,
    @NotNull CategoryModel categoryModel
  ) {
    sellPlugin.getViewFrame()
      .open(
      CategoryProgressView.class,
      player,
      Map.of(
        "selling_info", sellerEntity.getInfo(categoryModel),
        "category_model", categoryModel
      )
    );
  }
}
