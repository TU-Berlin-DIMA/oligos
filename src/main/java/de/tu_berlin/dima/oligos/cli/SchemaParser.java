package de.tu_berlin.dima.oligos.cli;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

import de.tu_berlin.dima.oligos.SparseSchema;
import de.tu_berlin.dima.oligos.SparseSchema.SparseSchemaBuilder;

@BuildParseTree
public class SchemaParser extends BaseParser<SparseSchema> {

  Rule Id() {
    return OneOrMore(NoneOf("(),"));
  }

  Rule SchemaSequence() {
    Var<SparseSchemaBuilder> builder = new Var<SparseSchemaBuilder>(new SparseSchemaBuilder());
    return Sequence(
        SchemaDefinition(builder),
        ZeroOrMore(Sequence(Ch(','), SchemaDefinition(builder))),
        EOI,
        push(builder.get().build()));
  }

  /**
   * Parses a schema definition, with an optional {@link #TableSequence() table sequence}
   * clause, given by<br />
   * <b><code>
   * SD &lt;- SCHEMA_NAME ['('{@link #TableSequence() TABLES}')']?
   * </code></b>
   * <br /><br />
   * Examples:
   * <ul>
   *   <li><code>SCHEMA_ID (table_def_a)</code></li>
   *   <li><code>SCHEMA_ID (table_def_a, table_def_b)</code></li>
   *   <li><code>SCHEMA_ID</code></li>
   * </ul>
   * @return
   */
  Rule SchemaDefinition(Var<SparseSchemaBuilder> builder) {
    Var<String> schema = new Var<String>();
    return Sequence(
        Schema(schema, builder),
        Optional(Sequence(Ch('('), TableSequence(schema, builder), Ch(')'))));
  }

  Rule Schema(Var<String> schema, Var<SparseSchemaBuilder> builder) {
    return Sequence(
        Id(),
        schema.set(match()),
        builder.set(builder.get().addSchema(schema.get())));
  }

  /**
   * Parses a table sequence, with one or more {@link #TableDefinition() table definitions},
   * given by<br />
   * <b><code>
   * TS &lt;- {@link #TableDefinition() TABLE} [',' {@link #TableDefinition() TABLE}]*
   * </code></b>
   * <br /><br />
   * Examples:
   * <ul>
   *   <li><code>TABLE_DEFINITION_A, TABLE_DEFINITION_B</code></li>
   *   <li><code>TABLE_DEFINITION_A</code></li>
   * </ul>
   * @return
   */
  Rule TableSequence(Var<String> schema, Var<SparseSchemaBuilder> builder) {
    return Sequence(
        TableDefinition(schema, builder),
        ZeroOrMore(Sequence(Ch(','), TableDefinition(schema, builder))));
  }

  /**
   * Parses a table definition, with an optional
   * {@link #ColumnSequence() column sequence}, given by<br />
   * <b><code>
   * TD &lt;- {@link #Table() TABLE_NAME} ['('{@link #ColumnSequence() COLUMNS}')']?
   * </code></b>
   * <br /><br />
   * Examples:
   * <ul>
   *   <li><code>TABLE_NAME</code></li>
   *   <li><code>TABLE_NAME (column_a, column_b)</code></li>
   *   <li><code>TABLE_NAME (column_a)</code></li>
   * </ul>
   * @return
   */
  Rule TableDefinition(Var<String> schema, Var<SparseSchemaBuilder> builder) {
    Var<String> table = new Var<String>();
    return Sequence(
        Table(schema, table, builder),
        Optional(Sequence(Ch('('), ColumnSequence(schema, table, builder), Ch(')'))));
  }

  Rule Table(Var<String> schema, Var<String> table, Var<SparseSchemaBuilder> builder) {
    return Sequence(
        Id(),
        // add the matched table name with the given schema name to the
        // sparse schema
        table.set(match()),
        builder.set(builder.get().addTable(schema.get(), table.get())));
  }

  Rule ColumnSequence(Var<String> schema, Var<String> table, Var<SparseSchemaBuilder> builder) {
    return Sequence(
        Column(schema, table, builder),
        ZeroOrMore(Sequence(Ch(','), Column(schema, table, builder))));
  }

  Rule Column(Var<String> schema, Var<String> table, Var<SparseSchemaBuilder> builder) {
    return Sequence(
        Id(),
        // add the matched column name with the given schema and table name
        // to the sparse schema
        builder.set(builder.get().addColumn(schema.get(), table.get(), match())));
  }

  public static SparseSchema parse(final String schemaDefinition) {
    String expression = schemaDefinition.replaceAll("\\s", "");
    SchemaParser parser = Parboiled.createParser(SchemaParser.class);
    ReportingParseRunner<SparseSchema> runner = new ReportingParseRunner<SparseSchema>(parser.SchemaSequence());
    ParsingResult<SparseSchema> result = runner.run(expression);
    if (result.matched) {
      return result.resultValue;
    }
    else {
      // TODO throw parseException with hints what went wrong
      return (new SparseSchemaBuilder()).build();
    }
  }
}
