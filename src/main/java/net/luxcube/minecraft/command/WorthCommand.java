package net.luxcube.minecraft.command;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import me.saiintbrisson.minecraft.ViewFrame;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import net.luxcube.minecraft.view.WorthView;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class WorthCommand {

  private final ViewFrame viewFrame;

  @Command(
    name = "worth",
    target = CommandTarget.PLAYER
  )
  public void handleWorthCommand(
    Context<Player> context
  ) {

    viewFrame.open(
      WorthView.class,
      context.getSender(),
      ImmutableMap.of(
        "sort_type", WorthView.SortType.HIGHEST_PRICE
      )
    );

  }

}
