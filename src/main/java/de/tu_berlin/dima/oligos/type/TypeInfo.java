package de.tu_berlin.dima.oligos.type;

public class TypeInfo {
  
  private final String typeName;
  private final int length;
  private final int scale;
  
  public TypeInfo(final String typeName, final int length, final int scale) {
    this.typeName = typeName;
    this.length = length;
    this.scale = scale;
  }
  
  public String getTypeName() {
    return typeName;
  }
  
  public int getLength() {
    return length;
  }
  
  public int getScale() {
    return scale;
  }
  
  @Override
  public String toString() {
    return typeName + "(" + length + ", " + scale + ")";
  }
}
