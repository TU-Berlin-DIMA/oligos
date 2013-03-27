package de.tu_berlin.dima.oligos.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.Schema;
import de.tu_berlin.dima.oligos.stat.Table;
import de.tu_berlin.dima.oligos.type.MyriadType;
import de.tu_berlin.dima.oligos.type.Types;
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Constraint;

public class MyriadWriter implements Writer {
  
  private static final Logger LOGGER = Logger.getLogger(MyriadWriter.class);
  private static final char FILE_SEPARATOR = '/';

  private final String generatorName;
  private final File outputDirectory;
  private final Document document;
  private final Schema schema;
  
  public MyriadWriter(final Schema schema
      , final File outputDirectory
      , final String generatorName) {
    this.generatorName = generatorName;
    this.outputDirectory = outputDirectory;
    this.document = createDocument();
    this.schema = schema;
    createSkeleton();
  }
  
  private Document createDocument() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder;
      builder = factory.newDocumentBuilder();
      return builder.newDocument();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException();
    }
  }
  
  public void write() throws IOException {
    String schemaName = schema.getName();
    Node parameters = document.getElementsByTagName(Tag.Parameters.getName()).item(0);
    Node functions = document.getElementsByTagName(Tag.Functions.getName()).item(0);
    Node enumSets = document.getElementsByTagName(Tag.EnumSets.getName()).item(0);
    Node recordSequences = document.getElementsByTagName(Tag.RecordSequences.getName()).item(0);
    for (Table table : schema) {
      String tableName = table.getTable();
      long tableCardinality = table.getCardinality();
      Element parameter = createParameter(tableName, tableCardinality);
      parameters.appendChild(parameter);
      Element randomSequence = createRandomSequence(tableName);
      Node recordType = randomSequence.getElementsByTagName("record_type").item(0);
      Node outputFormat = randomSequence.getElementsByTagName("output_format").item(0);
      Node setterChain = randomSequence.getElementsByTagName("setter_chain").item(0);
      recordSequences.appendChild(randomSequence);
      for (Column<?> column : table) {
        String columnName = column.getColumn();
        ColumnId columnId = new ColumnId(schemaName, tableName, columnName);
        // create functions for current column if not reference
        // and write distribution (domain) file
        if (!schema.isReference(columnId)) {
          Element func = createFunction(column);
          functions.appendChild(func); 
          File distFile = new File(outputDirectory, getRelativeDistributionPath(column.getId()));
          File domainFile = new File(outputDirectory, getRelativeDomainPath(column.getId()));
          DistributionWriter distWriter = new DistributionWriter(column, distFile, domainFile);
          LOGGER.info("Write distribution for " + columnId.getQualifiedName() + " to " + distFile.getPath());
          distWriter.write();
        }
        // create enum_set for column
        Element enumSet = createEnumSet(column);
        if (enumSet != null) {
          enumSets.appendChild(enumSet);
        }
        // create record types for current column
        Element field = createRecordType(column);
        recordType.appendChild(field);
        // create output format for current column
        // TODO 
        Element argOutFormat = createKeyTypeRefArgument("field", "field_ref", getFieldRef(columnId));
        outputFormat.appendChild(argOutFormat);
        // create setters for current column
        if (schema.isReference(columnId)) {
          Element reference = createReference(column);
          recordType.appendChild(reference);
          Element refSetter = createReferenceSetter(column);
          setterChain.appendChild(refSetter);
        }
        Element setter = createSetter(column);
        setterChain.appendChild(setter);
      }
    }
    try {
      writeXml();
    } catch (TransformerException e) {
      LOGGER.error("Error while transforming XML document.", e);
    }
  }

  private Element createElement(Tag tag) {
    String tagName = tag.getName();
    return document.createElement(tagName);
  }
  
  /**
   * Generates a XML skeleton as follows:<br />
   * <code><pre>
   * &lt;generator_prototype&gt;
   *   &lt;parameters /&gt;
   *   &lt;functions /&gt;
   *   &lt;enum_sets /&gt;
   *   &lt;record_sequences /&gt;
   * &lt;/generator_prototype&gt;
   * </pre></code>
   */
  private void createSkeleton() {
    Element generator_prototype = createElement(Tag.GeneratorPrototype);
    generator_prototype.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    generator_prototype.setAttribute("xmlns", "http://www.dima.tu-berlin.de/myriad/prototype");
    Element parameters = createElement(Tag.Parameters);
    generator_prototype.appendChild(parameters);
    Element functions = createElement(Tag.Functions);
    generator_prototype.appendChild(functions);
    Element enum_sets = createElement(Tag.EnumSets);
    generator_prototype.appendChild(enum_sets);
    Element record_sequences = createElement(Tag.RecordSequences);
    generator_prototype.appendChild(record_sequences);
    document.appendChild(generator_prototype);
  }
  
  private Element createParameter(String key, long value) {
    Element parameter = createElement(Tag.Parameter);
    parameter.setAttribute("key", getBaseCardinalityParam(key, false));
    parameter.setTextContent(String.valueOf(value));    
    return parameter;
  }
  
  private Element createFunction(Column<?> column) {
    ColumnId columnId = column.getId();
    Element func = createElement(Tag.Function);
    String funcName = getFunctionKey(columnId);
    String funcType = "";
    String colType = getMyriadType(column).getTypeName();
    if (column.isUnique()) {
      funcType = "uniform_probability[" + colType + "]";
      String min = column.getMin().toString();
      String max = "${%" + columnId.getTable().toLowerCase() + ".sequence.cardinality% + " + min + "}";
      Element argMin = createElement(Tag.Argument);
      argMin.setAttribute("key", "x_min");
      argMin.setAttribute("type", colType);
      argMin.setAttribute("value", min);
      Element argMax = createElement(Tag.Argument);
      argMax.setAttribute("key", "x_max");
      argMax.setAttribute("type", colType);
      argMax.setAttribute("value", max);
      func.appendChild(argMin);
      func.appendChild(argMax);
    } else if (column.isEnumerated()) {
      funcType = "combined_probability[Enum]";
      Element arg = createElement(Tag.Argument);
      arg.setAttribute("key", "path");
      arg.setAttribute("type", "String");
      arg.setAttribute("value", getDistributionPath(columnId));
      func.appendChild(arg);
    } else {
      funcType = "combined_probability[" + colType + "]";
      Element arg = createElement(Tag.Argument);
      arg.setAttribute("key", "path");
      arg.setAttribute("type", "String");
      arg.setAttribute("value", getDistributionPath(columnId));
      func.appendChild(arg);
    }
    func.setAttribute("key", funcName);
    func.setAttribute("type", funcType);
    return func;
  }
  
  private Element createEnumSet(Column<?> column) {
    if (column.isEnumerated()) {
      ColumnId columnId = column.getId();
      Element enumSet = createElement(Tag.EnumSet);
      enumSet.setAttribute("key", columnId.getQualifiedName().toLowerCase());
      Element arg = createElement(Tag.Argument);
      arg.setAttribute("key", "path");
      arg.setAttribute("type", "String");
      arg.setAttribute("value", getDomainPath(columnId));
      enumSet.appendChild(arg);
      return enumSet;
    } else {
      return null;
    }
  }
  
  private Element createRandomSequence(String table) {
    // create random sequence (skeleton) from given table name
    Element randomSequence = createElement(Tag.RandomSequence);
    randomSequence.setAttribute("key", table.toLowerCase());
    // create and append record_type
    Element recordType = createElement(Tag.RecordType);
    randomSequence.appendChild(recordType);
    // create and append output_format
    Element outputFormat = createElement(Tag.OutputFormat);
    outputFormat.setAttribute("type", "csv");
    Element argQuot = createKeyTypeValueArgument("quoted", "Bool", "false");
    Element argDelim = createKeyTypeValueArgument("delimiter", "Char", "|");
    outputFormat.appendChild(argQuot);
    outputFormat.appendChild(argDelim);
    randomSequence.appendChild(outputFormat);
    // create and append setter_chain
    Element setterChain = createElement(Tag.SetterChain);
    randomSequence.appendChild(setterChain);
    // create and append cardinality_estimator
    Element cardinalityEstimator = createElement(Tag.CardinalityEstimator);
    cardinalityEstimator.setAttribute("type", "linear_scale_estimator");
    Element cardArg = createElement(Tag.Argument);
    cardArg.setAttribute("key", "base_cardinality");
    cardArg.setAttribute("type", "I64u");
    cardArg.setAttribute("value", getBaseCardinalityParam(table, true));
    cardinalityEstimator.appendChild(cardArg);
    randomSequence.appendChild(cardinalityEstimator);
    // create and append sequence_iterator
    Element sequenceIterator = createElement(Tag.SequenceIterator);
    sequenceIterator.setAttribute("type", "partitioned_iterator");
    randomSequence.appendChild(sequenceIterator);
    return randomSequence;
  }
  
  private Element createRecordType(Column<?> column) {
    ColumnId columnId = column.getId();
    Element field = createElement(Tag.Field);
    field.setAttribute("name", "col_" + columnId.getColumn().toLowerCase());
    if (column.isEnumerated()) {
      field.setAttribute("type", "Enum");
      field.setAttribute("enumref", columnId.getQualifiedName().toLowerCase());
    } else {
      field.setAttribute("type", getMyriadType(column).getTypeName());
    }
    return field;
  }

  private Element createReference(Column<?> column) {
    ColumnId columnId = column.getId();
    Element reference = createElement(Tag.Reference);
    reference.setAttribute("name", "col_" + columnId.getColumn().toLowerCase() + "_ref");
    reference.setAttribute("type", getReferenceType(columnId));
    return reference;
  }

  private Element createReferenceSetter(Column<?> column) {
    ColumnId columnId = column.getId();
    Element setter = createElement(Tag.Setter);
    setter.setAttribute("key", getSetterKey(columnId) + "_ref");
    setter.setAttribute("type", "reference_setter");
    Element fieldArg =
        createKeyTypeRefArgument("reference", "reference_ref", getReferenceRef(columnId).toLowerCase());
    setter.appendChild(fieldArg);
    Element randomReferenceProvider = createRandomReferenceProvider(column);
    setter.appendChild(randomReferenceProvider);
    return setter;
  }
  
  private Element createSetter(Column<?> column) {
    ColumnId columnId = column.getId();
    Element setter = createElement(Tag.Setter);
    setter.setAttribute("key", getSetterKey(columnId));
    setter.setAttribute("type", "field_setter");
    Element fieldArg = createKeyTypeRefArgument("field", "field_ref", getFieldRef(columnId));
    setter.appendChild(fieldArg);
    // create reference provider for references, i.e. foreign keys
    if (schema.isReference(columnId)) {
      Element contextValueProvider = createContextValueProvider(column);
      setter.appendChild(contextValueProvider);
    }
    // TODO create CONST_VALUE_PROVIDER[<TYPE>] for all columns with low2key/ high2key = NULL
    // <attribute key="value" type="<TYPE>" value="NULL" />
    // if (column.getMin() == Null || column.getMax() == Null)

    // create clustered value provider for all key columns
    // TODO unique columns too
    // FIXME one single attribute constraints, NO MULTIATTRIBUTE CONSTRAINTS
    // ARE SUPPORTED
    else if (column.getConstraints().contains(Constraint.PRIMARY_KEY)) {
      Element clusteredValueProvider = createClusteredValueProvider(column);
      setter.appendChild(clusteredValueProvider);
    }
    // create random value provider for all non key columns
    else {
      Element randomValueProvider = createRandomValueProvider(column);
      setter.appendChild(randomValueProvider);
    }    
    return setter;
  }

  private Element createKeyTypeArgument(final String key, final String type) {
    Element argument = createElement(Tag.Argument);
    argument.setAttribute("key", key);
    argument.setAttribute("type", type);
    return argument;
  }

  private Element createKeyTypeValueArgument(
      final String key,
      final String type,
      final String value) {
    Element argument = createKeyTypeArgument(key, type);
    argument.setAttribute("value", value);
    return argument;
  }

  private Element createKeyTypeRefArgument(final String key, final String type
      , final String ref) {
    Element setterArg = createKeyTypeArgument(key, type);
    setterArg.setAttribute("ref", ref);
    return setterArg;
  }

  private Element createClusteredValueProvider(final Column<?> column) {
    ColumnId columnId = column.getId();
    String myriadType = getMyriadType(column).getTypeName();
    Element clusteredValueProvider = createElement(Tag.Argument);
    clusteredValueProvider.setAttribute("key", "value");
    clusteredValueProvider.setAttribute("type", "clustered_value_provider[" + myriadType + "]");
    Element probability = 
        createKeyTypeRefArgument("probability", "function_ref", getFunctionKey(columnId));
    clusteredValueProvider.appendChild(probability);
    Element cardinality = createKeyTypeArgument("cardinality", "const_range_provider[I64u]");
    Element min = createKeyTypeArgument("min", "I64u");
    min.setAttribute("value", String.valueOf(0));
    cardinality.appendChild(min);
    Element max = createKeyTypeArgument("max", "I64u");
    max.setAttribute("value", "%" + columnId.getTable().toLowerCase() + ".sequence.cardinality%");
    cardinality.appendChild(max);
    clusteredValueProvider.appendChild(cardinality);
    return clusteredValueProvider;
  }

  private Element createRandomValueProvider(final Column<?> column) {
    ColumnId columnId = column.getId();
    String myriadType = getMyriadType(column).getTypeName();
    Element randomValueProvider = createKeyTypeArgument("value", "random_value_provider[" + myriadType + "]");
    Element probability = 
        createKeyTypeRefArgument("probability", "function_ref", getFunctionKey(columnId));
    randomValueProvider.appendChild(probability);
    return randomValueProvider;
  }

  private Element createRandomValueProvider(final ColumnId columnId, final String type) {
    Element randomValueProvider = createKeyTypeArgument("value", "random_value_provider[" + type + "]");
    Element probability = 
        createKeyTypeRefArgument("probability", "function_ref", getFunctionKey(columnId));
    randomValueProvider.appendChild(probability);
    return randomValueProvider;
  }

  private Element createContextValueProvider(final Column<?> column) {
    ColumnId columnId = column.getId();
    String myriadType = getMyriadType(column).getTypeName();
    Element contextValueProvider =
        createKeyTypeArgument("value", "context_field_value_provider[" + myriadType + "]");
    String ref =
        getReferenceRef(columnId) + ":col_" + schema.getReferencedColumn(columnId).getColumn().toLowerCase();
    Element fieldArg = createKeyTypeRefArgument("field", "field_ref", ref);
    contextValueProvider.appendChild(fieldArg);
    return contextValueProvider;
  }

  private Element createRandomReferenceProvider(final Column<?> column) {
    ColumnId columnId = column.getId();
    Element randomReferenceProvider = 
        createKeyTypeArgument("value", "random_reference_provider");
    Element predicate = createKeyTypeArgument("predicate", "equality_predicate_provider");
    Element binder = createKeyTypeArgument("binder", "predicate_value_binder");
    ColumnId reffedCol = schema.getReferencedColumn(columnId);
    Element field = createKeyTypeRefArgument("field", "field_ref", getFieldRef(reffedCol));
    String type = getMyriadType(column).getTypeName();
    Element valueProvider = createRandomValueProvider(reffedCol, type);
    binder.appendChild(field);
    binder.appendChild(valueProvider);
    predicate.appendChild(binder);
    randomReferenceProvider.appendChild(predicate);
    return randomReferenceProvider;
  }
  
  private void writeXml() throws TransformerException, IOException {
    // create XML file and parent folders
    File xmlFile = new File(outputDirectory, generatorName + "-prototype.xml");
    xmlFile.mkdirs();
    xmlFile.delete();
    xmlFile.createNewFile();
    
    // set up a transformer
    TransformerFactory transfac = TransformerFactory.newInstance();
    Transformer trans = transfac.newTransformer();
    trans.setOutputProperty(OutputKeys.INDENT, "yes");
    trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    // create string from XML tree
    document.setXmlStandalone(true);
    FileWriter fw = new FileWriter(xmlFile);
    StreamResult result = new StreamResult(fw);
    DOMSource source = new DOMSource(document);
    trans.transform(source, result);
  }
  
  private MyriadType getMyriadType(Column<?> column) {
    if (column.isEnumerated()) {
      return MyriadType.Enum;
    } else {
      Class<?> clazz = column.getTypeInfo().getType();
      return Types.getMyriadType(clazz);
    }
  }

  private String getBaseCardinalityParam(String table, boolean isRef) {
    String param = table.toLowerCase() + ".sequence.base_cardinality";
    return isRef ? ("%" + param + "%") : param;
  }
  
  private String getFunctionKey(ColumnId column) {
    return "Pr[" + column.getQualifiedName().toLowerCase() + "]";
  }
  
  private String getDistributionPath(ColumnId column) {
    return "${%ENV.config-dir% + \"" + getRelativeDistributionPath(column) + "\"}";
  }
  
  private String getRelativeDistributionPath(ColumnId columnId) {
    return FILE_SEPARATOR +  "distributions" + FILE_SEPARATOR
        + columnId.getQualifiedName(FILE_SEPARATOR).toLowerCase() + ".distribution";
  }
  
  private String getDomainPath(ColumnId column) {
    return "${%ENV.config-dir% + \"" + getRelativeDomainPath(column) + "\"}";
  }
  
  private String getRelativeDomainPath(ColumnId columnId) {
    return FILE_SEPARATOR +  "domains" + FILE_SEPARATOR
        + columnId.getQualifiedName(FILE_SEPARATOR).toLowerCase() + ".domain";
  }
  
  private String getSetterKey(ColumnId columnId) {
    return "set_" + columnId.getColumn().toLowerCase();
  }
  
  private String getFieldRef(ColumnId columnId) {
    return (columnId.getTable() + ":" + "col_" + columnId.getColumn()).toLowerCase();
  }

  private String getReferenceRef(ColumnId columnId) {
    return getFieldRef(columnId) + "_ref";
  }

  private String getReferenceType(ColumnId columnId) {
    return schema.getReferencedColumn(columnId).getTable().toLowerCase();
  }

  public enum Tag {
    GeneratorPrototype("generator_prototype"),
    Parameters("parameters"),
      Parameter("parameter"),
    Functions("functions"),
      Function("function"),
    EnumSets("enum_sets"),
      EnumSet("enum_set"),
    Argument("argument"),
    RecordSequences("record_sequences"),
      RandomSequence("random_sequence"),
        RecordType("record_type"),
          Field("field"),
          Reference("reference"),
        OutputFormat("output_format"),
        SetterChain("setter_chain"),
          Setter("setter"),
        CardinalityEstimator("cardinality_estimator"),
        SequenceIterator("sequence_iterator");

    private final String name;
    
    private Tag(final String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
