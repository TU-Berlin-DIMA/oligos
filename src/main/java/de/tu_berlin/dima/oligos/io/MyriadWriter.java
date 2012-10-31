package de.tu_berlin.dima.oligos.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import de.tu_berlin.dima.oligos.type.util.ColumnId;
import de.tu_berlin.dima.oligos.type.util.Constraint;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;
import de.tu_berlin.dima.oligos.type.util.parser.ParserManager;

public class MyriadWriter implements Writer {
  
  private static final Logger LOGGER = Logger.getLogger(MyriadWriter.class);
  private static final char FILE_SEPARATOR = '/';
  @SuppressWarnings("serial")
  private static final Map<String, String> TYPE_MAPPING = new HashMap<String, String>(){{
    put("integer", "I32u");
    put("decimal", "Decimal");
    put("date", "Date");
    put("character", "String");
  }};
  
  private final String generatorName;
  private final File outputDirectory;
  private final Document document;
  private final Map<String, Set<Column<?>>> relations;
  private final ParserManager parserManager;
  private final Writer domainWriter;
  private final Writer distributionWriter;
  
  public MyriadWriter(final Map<String, Set<Column<?>>> relations
      , final File outputDirectory
      , final String generatorName) {
    this.generatorName = generatorName;
    this.outputDirectory = outputDirectory;
    this.document = createDocument();
    this.relations = relations;
    this.parserManager = ParserManager.getInstance();
    this.domainWriter = 
        new DomainFileWriter(new File(outputDirectory, "domains")
        , relations
        , "domain");
    this.distributionWriter =
        new DistributionFileWriter(new File(outputDirectory, "distributions")
        , relations
        , "distribution");
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
    Node parameters = document.getElementsByTagName("parameters").item(0);
    Node functions = document.getElementsByTagName("functions").item(0);
    Node enumSets = document.getElementsByTagName("enum_sets").item(0);
    Node recordSequences = document.getElementsByTagName("record_sequences").item(0);
    for (Entry<String, Set<Column<?>>> relEntry : relations.entrySet()) {
      String table = relEntry.getKey();
      long tableCardinality = calculateTableCardinality(relEntry.getValue());
      Element parameter = createParameter(table, tableCardinality);
      parameters.appendChild(parameter);
      Element randomSequence = createRandomSequence(table);
      Node recordType = randomSequence.getElementsByTagName("record_type").item(0);
      Node setterChain = randomSequence.getElementsByTagName("setter_chain").item(0);
      recordSequences.appendChild(randomSequence);
      for (Column<?> colStat : relEntry.getValue()) {
        String column = colStat.getColumn();
        ColumnId columnId = new ColumnId("", table, column);
        // create functions for current column
        Element func = createFunction(columnId, colStat);
        functions.appendChild(func);
        // create enum_set for column
        Element enumSet = createEnumSet(columnId, colStat);
        if (enumSet != null) {
          enumSets.appendChild(enumSet);
        }
        // create record types for current column
        Element field = createRecordType(columnId, colStat);
        recordType.appendChild(field);
        // create setters for current column
        Element setter = createSetter(columnId, colStat);
        setterChain.appendChild(setter);
      }
    }
    try {
      writeXml();
    } catch (TransformerException e) {
      LOGGER.error("Error while transforming XML document.", e);
    }
    domainWriter.write();
    distributionWriter.write();
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
    Element generator_prototype = document.createElement("generator_prototype");
    generator_prototype.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    generator_prototype.setAttribute("xmlns", "http://www.dima.tu-berlin.de/myriad/prototype");
    Element parameters = document.createElement("parameters");
    generator_prototype.appendChild(parameters);
    Element functions = document.createElement("functions");
    generator_prototype.appendChild(functions);
    Element enum_sets = document.createElement("enum_sets");
    generator_prototype.appendChild(enum_sets);
    Element record_sequences = document.createElement("record_sequences");
    generator_prototype.appendChild(record_sequences);
    document.appendChild(generator_prototype);
  }
  
  private Element createParameter(String key, long value) {
    Element parameter = document.createElement("parameter");
    parameter.setAttribute("key", getBaseCardinalityParam(key, false));
    parameter.setTextContent(String.valueOf(value));    
    return parameter;
  }
  
  private Element createFunction(ColumnId columnId, Column<?> columnStat) {
    Parser<?> parser = parserManager.getParser(columnId);
    Element func = document.createElement("function");
    String funcName = getFunctionKey(columnId);
    String funcType = "";
    String colType = getMyriadType(columnStat.getType());
    if (columnStat.isUnique()) {
      funcType = "uniform_probability[" + colType + "]";
      String min = parser.toString(columnStat.getMin());
      //String max = parser.toString(columnStat.getMax());
      String max = "${%customer.sequence.cardinality% + " + min + "}";
      Element argMin = document.createElement("argument");
      argMin.setAttribute("key", "x_min");
      argMin.setAttribute("type", colType);
      argMin.setAttribute("value", min);
      Element argMax = document.createElement("argument");
      argMax.setAttribute("key", "x_max");
      argMax.setAttribute("type", colType);
      argMax.setAttribute("value", max);
      func.appendChild(argMin);
      func.appendChild(argMax);
    } else if (columnStat.isEnumerated()) {
      funcType = "combined_probability[Enum]";
      Element arg = document.createElement("argument");
      arg.setAttribute("key", "path");
      arg.setAttribute("type", "String");
      arg.setAttribute("value", getDistributionPath(columnId));
      func.appendChild(arg);
    } else {
      funcType = "combined_probability[" + colType + "]";
      Element arg = document.createElement("argument");
      arg.setAttribute("key", "path");
      arg.setAttribute("type", "String");
      arg.setAttribute("value", getDistributionPath(columnId));
      func.appendChild(arg);
    }
    func.setAttribute("key", funcName);
    func.setAttribute("type", funcType);
    return func;
  }
  
  private Element createEnumSet(ColumnId columnId, Column<?> colStat) {
    if (colStat.isEnumerated()) {
      Element enumSet = document.createElement("enum_set");
      enumSet.setAttribute("key", columnId.getQualifiedName());
      Element arg = document.createElement("argument");
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
    Element randomSequence = document.createElement("random_sequence");
    randomSequence.setAttribute("key", table.toLowerCase());
    Element recordType = document.createElement("record_type");
    randomSequence.appendChild(recordType);
    Element setterChain = document.createElement("setter_chain");
    randomSequence.appendChild(setterChain);
    Element cardinalityEstimator = document.createElement("cardinality_estimator");
    cardinalityEstimator.setAttribute("type", "linear_scale_estimator");
    Element cardArg = document.createElement("argument");
    cardArg.setAttribute("key", "base_cardinality");
    cardArg.setAttribute("type", "I64u");
    cardArg.setAttribute("value", getBaseCardinalityParam(table, true));
    cardinalityEstimator.appendChild(cardArg);
    randomSequence.appendChild(cardinalityEstimator);
    Element sequenceIterator = document.createElement("sequence_iterator");
    sequenceIterator.setAttribute("type", "partitioned_iterator");
    randomSequence.appendChild(sequenceIterator);
    return randomSequence;
  }
  
  private Element createRecordType(ColumnId columnId, Column<?> colStat) {
    Element field = document.createElement("field");
    field.setAttribute("name", columnId.getColumn());
    if (colStat.isEnumerated()) {
      field.setAttribute("type", "Enum");
      field.setAttribute("enumref", columnId.getQualifiedName());
    } else {
      field.setAttribute("type", getMyriadType(colStat.getType()));
    }
    return field;
  }
  
  private Element createSetter(ColumnId columnId, Column<?> colStat) {
    Element setter = document.createElement("setter");
    setter.setAttribute("key", getSetterKey(columnId));
    setter.setAttribute("type", "field_setter");
    Element fieldArg = document.createElement("argument");
    fieldArg.setAttribute("key", "field");
    fieldArg.setAttribute("type", "field_ref");
    fieldArg.setAttribute("ref", getFieldRef(columnId));
    setter.appendChild(fieldArg);
    // create clustered value provider for all key columns
    if (colStat.getConstraints().contains(Constraint.PRIMARY_KEY)) {
      Element clusteredValueProvider = document.createElement("argument");
      clusteredValueProvider.setAttribute("key", "value");
      clusteredValueProvider.setAttribute("type", "clustered_value_provider");
      Element probability = document.createElement("argument");
      probability.setAttribute("key", "probability");
      probability.setAttribute("type", "function_ref");
      probability.setAttribute("ref", getFunctionKey(columnId));
      clusteredValueProvider.appendChild(probability);
      Element cardinality = document.createElement("argument");
      cardinality.setAttribute("key", "cardinality");
      cardinality.setAttribute("type", "const_range_provider");
      Element min = document.createElement("argument");
      min.setAttribute("key", "min");
      min.setAttribute("type", "I64u");
      min.setAttribute("value", String.valueOf(0));
      cardinality.appendChild(min);
      Element max = document.createElement("argument");
      max.setAttribute("key", "max");
      max.setAttribute("type", "I64u");
      max.setAttribute("value", "%" + columnId.getTable() + ".sequence.cardinality%");
      cardinality.appendChild(max);
      clusteredValueProvider.appendChild(cardinality);
      setter.appendChild(clusteredValueProvider);
    }
    // create random value provider for all non key columns
    else {
      Element randomValueProvider = document.createElement("argument");
      randomValueProvider.setAttribute("key", "value");
      randomValueProvider.setAttribute("type", "random_value_provider");
      Element probability = document.createElement("argument");
      probability.setAttribute("key", "probability");
      probability.setAttribute("type", "function_ref");
      probability.setAttribute("ref", getFunctionKey(columnId));
      randomValueProvider.appendChild(probability);
      setter.appendChild(randomValueProvider);
    }
    return setter;
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
  
  private long calculateTableCardinality(Set<Column<?>> relation) {
    long cardinality = 0l;
    for (Column<?> column : relation) {
      cardinality = Math.max(cardinality, column.getCardinality());
    }
    return cardinality;
  }
  
  private String getMyriadType(String internalType) {
    return TYPE_MAPPING.get(internalType);
  }
  
  private String getBaseCardinalityParam(String table, boolean isRef) {
    String param = table.toLowerCase() + ".sequence.base_cardinality";
    return isRef ? ("%" + param + "%") : param;
  }
  
  private String getFunctionKey(ColumnId column) {
    return "Pr[" + column.getQualifiedName() + "]";
  }
  
  private String getDistributionPath(ColumnId column) {
    return "${%ENV.config-dir% + \"" + getRelativeDistributionPath(column) + "\"}";
  }
  
  private String getRelativeDistributionPath(ColumnId columnId) {
    return FILE_SEPARATOR +  "distributions" + FILE_SEPARATOR
        + columnId.getQualifiedName(FILE_SEPARATOR) + ".distribution";
  }
  
  private String getDomainPath(ColumnId column) {
    return "${%ENV.config-dir% + \"" + getRelativeDomainPath(column) + "\"}";
  }
  
  private String getRelativeDomainPath(ColumnId columnId) {
    return FILE_SEPARATOR +  "domains" + FILE_SEPARATOR
        + columnId.getQualifiedName(FILE_SEPARATOR) + ".domain";
  }
  
  private String getSetterKey(ColumnId columnId) {
    return "set_" + columnId.getColumn();
  }
  
  private String getFieldRef(ColumnId columnId) {
    return columnId.getTable() + ":" + columnId.getColumn();
  }
}
