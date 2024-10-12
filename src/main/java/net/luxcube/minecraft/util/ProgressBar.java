package net.luxcube.minecraft.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ProgressBar {

  public static Bar generateProgressBar(
    @NotNull String filledPrefix,
    @NotNull String emptyPrefix,
    char filledBar,
    char emptyBar,
    double currentValue,
    double maxValue,
    int barLength
  ) {
    if (currentValue < 0) {
      currentValue = 0;
    } else if (currentValue > maxValue) {
      currentValue = maxValue;
    }

    double percentage = (double) currentValue / maxValue;
    int filledBars = (int) (percentage * barLength);

    return new Bar(
      filledPrefix + String.valueOf(filledBar).repeat(Math.max(0, filledBars)) +
        emptyPrefix +
        String.valueOf(emptyBar).repeat(barLength-filledBars),
      filledBars == barLength
    );
  }

  @AllArgsConstructor
  @Getter
  public static class Bar {
    private final String barString;
    private final boolean isFull;
  }

}

