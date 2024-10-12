package net.luxcube.minecraft.command;

import lombok.RequiredArgsConstructor;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import net.luxcube.minecraft.SellPlugin;
import net.luxcube.minecraft.view.ListCategoryModelView;
import net.luxcube.minecraft.view.ListSellHistoryView;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@RequiredArgsConstructor
public class SellCommand {

  private final SellPlugin sellPlugin;

  @Command(
    name = "sell",
    target = CommandTarget.PLAYER
  )
  public void handleSellCommand(@NotNull Context<Player> context) {
    Player player = context.getSender();
    sellPlugin.getViewFrame()
      .open(
        ListCategoryModelView.class,
        player,
        Map.of(
          "seller_entity",
          sellPlugin.getSellerService()
            .getSellerEntity(player.getUniqueId())
        )
      );
  }

  @Command(
    name = "sellhistory",
    target = CommandTarget.PLAYER
  )
  public void handleSellHistoryCommand(@NotNull Context<Player> context) {
    Player player = context.getSender();
    sellPlugin.getViewFrame()
      .open(
        ListSellHistoryView.class,
        player,
        Map.of(
          "seller_entity",
          sellPlugin.getSellerService()
            .getSellerEntity(player.getUniqueId())
        )
      );
  }

  @Command(
    name = "sell.reload",
    permission = "donutsmp.reload"
  )
  public void handleReloadCommand(@NotNull Context<CommandSender> context) {
    sellPlugin.reloadConfig();
    sellPlugin.onLoad();
    context.sendMessage("§aPlugin reloaded.");
  }

}
