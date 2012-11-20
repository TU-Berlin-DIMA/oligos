package de.tu_berlin.dima.oligos.stat;

import java.util.HashSet;
import java.util.Set;

import de.tu_berlin.dima.oligos.stat.distribution.histogram.Histogram;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class Column<T> {

  @SuppressWarnings("serial")
  public static final Set<String> SUPPORTED_TYPES = new HashSet<String>() {{
    add("integer");
    add("date");
    add("decimal");
  }};

  private final String schema;
  private final String table;
  private final String column;
  private final String type;
  private final Set<Constraint> constraints;
  private final T min;
  private final T max;
  private final long cardinality;
  private final long numNulls;
  private final Histogram<T> distribution;
  private final Parser<T> parser;
  //private final Map<T, Long> mostFrequent;
  

  public Column(final String schema, final String table, final String column
      , final String type, final Set<Constraint> constraints
      , final T min, final T max
      , final long cardinality, final long numNulls
      , final Histogram<T> distribution, final Parser<T> parser) {
    this.schema = schema;
    this.table = table;
    this.column = column;
    this.type = type;
    this.constraints = constraints;
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.numNulls = numNulls;
    this.distribution = distribution;
    this.parser = parser;
  }
  
  public String getSchema() {
    return schema;
  }
  
  public String getTable() {
    return table;
  }
  
  public String getColumn() {
    return column;
  }

  public String getQualifiedName() {
    return schema + "." + table + "." + column;
  }
  
  public String getType() {
    return type;
  }

  public Set<Constraint> getConstraints() {
    return constraints;
  }

  public T getMin() {
    return min;
  }

  public T getMax() {
    return max;
  }

  public long getCardinality() {
    return cardinality;
  }

  public long getNumNulls() {
    return numNulls;
  }
  
  public boolean isEnumerated() {
    if (SUPPORTED_TYPES.contains(getType())) {
      return false;
    } else {
      return true;
    }
  }
  
  public boolean isUnique() {
    if (constraints.contains(Constraint.PRIMARY_KEY) || constraints.contains(Constraint.UNIQUE)) {
      return true;
    } else {
      return false;
    }
  }
  
  public boolean hasDistribution() {
    return !distribution.isEmpty();
  }
  
  public long getNumberOfValues() {
    return distribution.getTotalNumberOfValues();
  }
  
  public Histogram<T> getDistribution() {
    return distribution;
  }

  public String asString(Object value) {
    return parser.toString(value);
  }

  @Override
  public String toString() {
    return getQualifiedName();
  }
}
