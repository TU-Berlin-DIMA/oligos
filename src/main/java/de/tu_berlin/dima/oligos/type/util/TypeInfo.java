/*******************************************************************************
 * Copyright 2013 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tu_berlin.dima.oligos.type.util;

import java.util.HashSet;
import java.util.Set;

public class TypeInfo {
  
  private final String typeName;
  private final int length;
  private final int scale;
  private final Class<?> type;
  
  public TypeInfo(final String typeName, final int length, final int scale, final Class<?> type) {
    this.typeName = typeName;
    this.length = length;
    this.scale = scale;
    this.type = type;
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

  public Class<?> getType() {
    return type;
  }
  
  public String getDbTypeString() {
    String typeString = typeName;
    Set<String> specialTypes = new HashSet<String>();
    specialTypes.add("decimal");
    specialTypes.add("char");
    specialTypes.add("varchar");
    if (typeName.equalsIgnoreCase("char") || typeName.equalsIgnoreCase("varchar")) {
      return typeString + "(" + length + ")";
    } else if (typeName.equalsIgnoreCase("decimal")) {
      return typeString + "(" + length + ", " + scale + ")";
    } else {
      return typeString;
    }
  }
  
  @Override
  public String toString() {
    return typeName + " -> " + type.getSimpleName() + " (" + length + ", " + scale + ")";
  }
}
