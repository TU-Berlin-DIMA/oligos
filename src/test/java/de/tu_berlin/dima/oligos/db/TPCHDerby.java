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
package de.tu_berlin.dima.oligos.db;

import java.io.InputStream;

public class TPCHDerby extends AbstractDbDerby {

  private final static String DATABASE_NAME = "TPCH";
  private final static String SCHEMA_URL = "/tpch/schema.sql";
  private final static String CONSTRAINTS_URL = "/tpch/constraints.sql";

  private final TPCHModel model;
  
  public TPCHDerby() {
    this(false);
  }

  public TPCHDerby(boolean verbose) {
    super(verbose);
    this.model = new TPCHModel();
  }

  public TPCHModel getModel() {
    return model;
  }

  @Override
  public String getDataBaseName() {
    return DATABASE_NAME;
  }

  @Override
  public InputStream getSchemaScript() {
    return getClass().getResourceAsStream(SCHEMA_URL);
  }

  @Override
  public InputStream getConstraintScript() {
    return getClass().getResourceAsStream(CONSTRAINTS_URL);
  }

}
