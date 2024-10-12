package net.luxcube.minecraft.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberFormatter {
  private static final Pattern PATTERN = Pattern.compile("^(\\d+\\.?\\d*)(\\D+)");

  private static final List<String> FORMAT_SUFFIXES = List.of("", "K", "M", "B", "T", "Q", "QQ");
  private static final List<String> PARSE_SUFFIXES = List.of("", "K", "KK", "B", "T", "Q", "QQ");

  private static final NumberFormat SIMPLE_FORMAT = new DecimalFormat("###,###");

  private static final NumberFormat SUFFIXED_FORMAT = new DecimalFormat("#.##");

  public static String formatSuffixed(double value) {
    int index = 0;

    double tmp;
    while ((tmp = value / 1000) >= 1) {
      value = tmp;
      ++index;
    }

    return SUFFIXED_FORMAT.format(value) + FORMAT_SUFFIXES.get(index);
  }

  public static String formatSuffixed(int value) {
    return formatSuffixed((double) value);
  }

  public static String formatSuffixed(BigInteger value) {
    return formatSuffixed(value.doubleValue());
  }

  public static String formatSuffixed(BigDecimal value) {
    return formatSuffixed(value.doubleValue());
  }


  public static String formatSimple(Double value) {
    return SIMPLE_FORMAT.format(value);
  }

  public static String formatSimple(int value) {
    return SIMPLE_FORMAT.format(value);
  }

  public static String formatInteger(int value) {
    return SIMPLE_FORMAT.format(value);
  }

  public static Double parseString(String value) {
    try {
      return Double.parseDouble(value);
    } catch (Exception ignored) {
    }

    Matcher matcher = PATTERN.matcher(value);
    if (!matcher.find()) {
      return null;
    }

    double amount = Double.parseDouble(matcher.group(1));
    String suffix = matcher.group(2);

    int index = PARSE_SUFFIXES.indexOf(suffix.toUpperCase());
    if (index <= 0) {
      return 0D;
    }

    return amount * Math.pow(1000, index);
  }
}

