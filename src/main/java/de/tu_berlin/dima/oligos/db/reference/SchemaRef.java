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
package de.tu_berlin.dima.oligos.db.reference;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Plain object representing a column reference in a relational system.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 */
public class SchemaRef {

  private final String schema;
  
  public SchemaRef(final String schema) {
    Preconditions.checkArgument(schema != null,
        "A schema name must not be null. Use EmtpyRef instead.");
    this.schema = schema;
  }

  /**
   * Retrieves the schema reference.
   * @return
   */
  public SchemaRef getSchema() {
    return this;
  }

  /**
   * Retrieves the name of the schema.
   * @return
   */
  public String getSchemaName() {
    return schema;
  }

  
  
  @Override
  public int hashCode() {
    return Objects.hashCode(schema);
  }

  /**
   * Indicates whether two <code>SchemaRef</code>s are equal or not. If the given
   * object is an instance of a subclass of <code>SchemaRef</code> it is casted
   * down to a schema and only the schema part is then checked for equality.
   * Subsequently the same happens for all subclasses of <code>SchemaRef</code>.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (obj instanceof SchemaRef) {
      SchemaRef other = (SchemaRef) obj;
      return Objects.equal(this.schema, other.schema);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return getSchemaName();
  }
}
