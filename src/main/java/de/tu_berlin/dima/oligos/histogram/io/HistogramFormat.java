package de.tu_berlin.dima.oligos.histogram.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import de.tu_berlin.dima.oligos.histogram.Histogram;

public interface HistogramFormat {
  void read(Histogram hist, BufferedReader reader) throws IOException;
  void write(Histogram hist, BufferedWriter writer) throws IOException;
}
