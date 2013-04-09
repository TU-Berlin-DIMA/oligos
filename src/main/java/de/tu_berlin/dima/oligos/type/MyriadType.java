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
