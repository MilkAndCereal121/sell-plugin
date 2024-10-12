package net.luxcube.minecraft.model.registry;

import net.luxcube.minecraft.SellPlugin;
import net.luxcube.minecraft.model.CategoryModel;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryRegistry {

  private final List<CategoryModel> categoryModels;

  public CategoryRegistry(@NotNull SellPlugin sellPlugin) {
    this.categoryModels = new ArrayList<>();

    FileConfiguration config = sellPlugin.getConfig();

    ConfigurationSection categoriesSection = config.getConfigurationSection("categories");
    for (@NotNull String key : categoriesSection.getKeys(false)) {
      ConfigurationSection categorySection = categoriesSection.getConfigurationSection(key);
      if (categorySection == null) {
        throw new IllegalArgumentException("Missing category section: " + key);
      }

      CategoryModel categoryModel = CategoryModel.constructModel(categorySection);
      categoryModels.add(categoryModel);
    }
  }

  public void add(@NotNull CategoryModel categoryModel) {
    categoryModels.add(categoryModel);
  }

  public void remove(@NotNull CategoryModel categoryModel) {
    categoryModels.remove(categoryModel);
  }

  @Nullable
  public CategoryModel from(@NotNull Material material) {
    return categoryModels.stream()
      .filter(categoryModel -> categoryModel.contains(material))
      .findFirst()
      .orElse(null);
  }

  public void clear() {
    categoryModels.clear();
  }

  public List<CategoryModel> getCategoryModels() {
    return Collections.unmodifiableList(categoryModels);
  }

}
