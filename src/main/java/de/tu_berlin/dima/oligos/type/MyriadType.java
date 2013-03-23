package de.tu_berlin.dima.oligos.type;

public enum MyriadType {
  I16 ("I16", false),
  I16u ("I16u", false),
  I32 ("I32", false),
  I32u ("I32u", false),
  I64 ("I64", false),
  I64u ("I64u", false),
  Decimal ("Decimal", false),
  Char ("Char", true),
  String ("String", true),
  Date ("Date", false),
  Enum ("Enum", false);
  
  final String name;
  final boolean quoted;

  MyriadType(final String name, final boolean isQuoted) {
    this.name = name;
    this.quoted = isQuoted;
  }
  
  public String getTypeName() {
    return name;
  }

  public boolean isQuoted() {
    return quoted;
  }
}