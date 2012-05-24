package de.tu_berlin.dima.oligos.histogram.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import de.tu_berlin.dima.oligos.histogram.Histogram;

public class MyriadWriter implements HistogramFormat {
  
  public String getHeader() {
    return "";
  }

  public void read(Histogram hist, BufferedReader reader) throws IOException {
    // TODO Auto-generated method stub
    
  }

  public void write(Histogram hist, BufferedWriter writer) throws IOException {
    writer.write(getHeader());
    // TODO write the histogramm line by line
    // writer.write(...)
    // writer.newLine();
    writer.flush();
  }

  
}
