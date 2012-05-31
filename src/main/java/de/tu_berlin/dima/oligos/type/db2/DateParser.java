package de.tu_berlin.dima.oligos.type.db2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.tu_berlin.dima.oligos.type.Parser;

public class DateParser implements Parser<Date> {

  private final SimpleDateFormat inputFormat;
  private final SimpleDateFormat outputFormat;

  public DateParser(final String inputFormat, final String outputFormat) {
    this.inputFormat = new SimpleDateFormat(inputFormat);
    this.outputFormat = new SimpleDateFormat(outputFormat);
  }

  @Override
  public Date parse(String input) {
    Date date = new Date(0);
    try {
      date = inputFormat.parse(input.replaceAll("\'", ""));
    } catch (ParseException e) {
      throw new IllegalArgumentException("Could not parse " + date
          + " with pattern" + inputFormat.toPattern());
    }
    return date;
  }

  @Override
  public String format(Date input) {
    return outputFormat.format(input);
  }

}
