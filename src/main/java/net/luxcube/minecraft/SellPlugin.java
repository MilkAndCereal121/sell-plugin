package net.luxcube.minecraft;

import lombok.Getter;
import me.saiintbrisson.bukkit.command.BukkitFrame;
import me.saiintbrisson.minecraft.ViewFrame;
import net.luxcube.minecraft.adapter.YamlAdapter;
import net.luxcube.minecraft.command.SellCommand;
import net.luxcube.minecraft.command.WorthCommand;
import net.luxcube.minecraft.entity.SellerEntity;
import net.luxcube.minecraft.listener.ConnectionListener;
import net.luxcube.minecraft.model.registry.CategoryRegistry;
import net.luxcube.minecraft.service.SellerService;
import net.luxcube.minecraft.util.License;
import net.luxcube.minecraft.view.CategoryProgressView;
import net.luxcube.minecraft.view.ListCategoryModelView;
import net.luxcube.minecraft.view.ListSellHistoryView;
import net.luxcube.minecraft.view.WorthView;
import net.luxcube.minecraft.vo.SellVO;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Getter
public class SellPlugin extends JavaPlugin {

  @NotNull
  public static SellPlugin getInstance() {
    return getPlugin(SellPlugin.class);
  }

  private List<Double> levelPrices;

  private SellerService sellerService;
  private CategoryRegistry categoryRegistry;

  private ViewFrame viewFrame;
  private Economy economy;

  private File storageFile;

  private SellVO sellVO;

  @Override
  public void onLoad() {
    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }

    saveDefaultConfig();

    storageFile = new File(getDataFolder(), "storage");
    if (!storageFile.exists()) {
      storageFile.mkdirs();
    }

    this.sellVO = SellVO.constructObject(getConfig());

    this.levelPrices = getConfig()
      .getDoubleList("level-prices");

    categoryRegistry = new CategoryRegistry(this);
  }

  @Override
  public void onEnable() {

    if (!License.isLicenseValid(getSellVO().licenseKey())) {
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    economy = Bukkit.getServicesManager()
      .load(Economy.class);

    this.sellerService = new SellerService();

    Bukkit.getPluginManager()
      .registerEvents(new ConnectionListener(this), this);

    this.viewFrame = ViewFrame.of(
      this,
      new ListCategoryModelView(this),
      new CategoryProgressView(this),
      new ListSellHistoryView(this),
      new WorthView(this)
    );
    this.viewFrame.register();

    BukkitFrame bukkitFrame = new BukkitFrame(this);
    bukkitFrame.registerCommands(
      new SellCommand(this),
      new WorthCommand(viewFrame)
    );
  }

  @Override
  public void onDisable() {
    HandlerList.unregisterAll(this);

    for (@NotNull SellerEntity sellerEntity : sellerService.getSellerEntities()) {
      File file = new File(storageFile, sellerEntity.getUniqueId() + ".yml");
      FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

      YamlAdapter.parsePlayer(sellerEntity, fileConfiguration);
      try {
        fileConfiguration.save(file);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
