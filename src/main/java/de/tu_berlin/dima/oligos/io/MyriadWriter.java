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

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import de.tu_berlin.dima.oligos.stat.Bucket;
import de.tu_berlin.dima.oligos.stat.Column;
import de.tu_berlin.dima.oligos.stat.histogram.Histogram;
import de.tu_berlin.dima.oligos.stat.histogram.Histograms;
import de.tu_berlin.dima.oligos.type.TypeManager;
import de.tu_berlin.dima.oligos.type.util.parser.Parser;

public class MyriadWriter {
  
  private static final char FILE_SEPARATOR = '/';
  @SuppressWarnings("serial")
  private static final Map<String, String> TYPE_MAPPING = new HashMap<String, String>(){{
    put("integer", "I32u");
    put("decimal", "Decimal");
    put("date", "Date");
    put("character", "String");
  }};

  private final String outputDirectory;
  private final String domainDirectory;
  private final String distributionDirectory;
  private final Map<String, Set<Column<?>>> relations;
  private final Document document;

  private Map<String, Object> parameters;

  public MyriadWriter(Map<String, Set<Column<?>>> relations, String outputDirectory) {
    try {
    this.outputDirectory = outputDirectory;
    this.domainDirectory = outputDirectory + FILE_SEPARATOR + "domain";
    this.distributionDirectory = outputDirectory + FILE_SEPARATOR
        + "distribution";
    this.relations = relations;
    this.document = createXmlDocument();
    this.parameters = Maps.newHashMap();
    } catch (ParserConfigurationException pce) {
      throw new RuntimeException("");
    }
  }

  public void addParameter(String key, Object value) {
    parameters.put(key, value);
  }

  public void write() throws IOException {
    try {
      // create folders
      createDirectory(outputDirectory);
      createDirectory(domainDirectory);
      createDirectory(distributionDirectory);
      
      // create prototype xml document
      Element root = document.createElement("generator_prototype");
      
      // write parameters to xml
      Element params = document.createElement("paramaters");
      createParameters(params);
      root.appendChild(params);

      // write function to xml
      Element funcs = document.createElement("functions");
      createFunctions(funcs);
      root.appendChild(funcs);
      
      // write enum_sets to xml
      Element enums = document.createElement("enum_sets");
      createEnumSets(enums);
      root.appendChild(enums);
      
      // write record sequences
      Element recs = document.createElement("record_sequences");
      createRecordSequences(recs);
      root.appendChild(recs);

      // transfrom xml to String
      document.appendChild(root);      
      writeXml(document);
      writeDomains();
      writeDistributions();
    } catch (TransformerException tfe) {
      tfe.printStackTrace(System.err);
    }
  }

  private void createDirectory(String directory) {

  }

  public static Document createXmlDocument()
      throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    builder = factory.newDocumentBuilder();
    return builder.newDocument();
  }

  private void createParameters(Element params) {
    for (Entry<String, Object> e : parameters.entrySet()) {
      Element param = document.createElement("parameter");
      param.setAttribute("key", e.getKey());
      Text text = document.createTextNode(e.getValue().toString());
      param.appendChild(text);
      params.appendChild(param);
    }
  }

  private void createFunctions(Element funcs) {
    for (Entry<String, Set<Column<?>>> e : relations.entrySet()) {
      String table = e.getKey();
      Set<Column<?>> columns = e.getValue();
      Comment comment = document.createComment("functions for " + table);
      funcs.appendChild(comment);
      for (Column<?> col : columns) {
        Element func = document.createElement("function");
        func.setAttribute("key", "Pr[" + col.getName() + "]");
        // Functions for columns with unique values
        if (col.isUnique()) {
          func.setAttribute("type", "uniform_probability");
          Element argMin = document.createElement("argument");
          Element argMax = document.createElement("argument");
          argMin.setAttribute("key", "x_min");
          argMin.setAttribute("value", col.getMin().toString());
          argMin.setAttribute("type", getMyriadType(col.getType()));
          argMax.setAttribute("key", "x_max");
          argMax.setAttribute("value", col.getMax().toString());
          argMax.setAttribute("type", getMyriadType(col.getType()));
          func.appendChild(argMin);
          func.appendChild(argMax);
        } 
        // Functions for columns with "enumerated" values
        else if (col.isEnumerated()) {
          func.setAttribute("type", "combined_probability[Enum]");
          Element argPath = document.createElement("argument");
          argPath.setAttribute("key", "path");
          argPath.setAttribute("type", "String");
          argPath.setAttribute("value", col.getName() + ".domain");
          func.appendChild(argPath);
        } 
        // Functions for "ordinary" columns with full statistics 
        else {
          func.setAttribute("type", "combined_probability[" + getMyriadType(col.getType()) + "]");
          Element argPath = document.createElement("argument");
          argPath.setAttribute("key", "path");
          argPath.setAttribute("type", "String");
          argPath.setAttribute("value", col.getName() + ".distribution");
          func.appendChild(argPath);
        }
        funcs.appendChild(func);
      }
    }
  }

  private void createEnumSets(Element enums) {
    for (Entry<String, Set<Column<?>>> e : relations.entrySet()) {
      String table = e.getKey();
      Set<Column<?>> columns = e.getValue();
      Comment comment = document.createComment("enumerated attributes for " + table);
      enums.appendChild(comment);
      for (Column<?> col : columns) {
        if (col.isEnumerated()) {
          Element enumSet = document.createElement("enum_set");
          enumSet.setAttribute("key", col.getName());
          Element argPath = document.createElement("argument");
          argPath.setAttribute("key", "path");
          argPath.setAttribute("type", "String");
          argPath.setAttribute("value", domainDirectory + FILE_SEPARATOR
              + col.getName() + ".domain");
          enumSet.appendChild(argPath);
          enums.appendChild(enumSet);
        }
      }
    }
  }

  private void createRecordSequences(Element recs) {
    for (Entry<String, Set<Column<?>>> e : relations.entrySet()) {
      long tableCol = 0l;
      String table = e.getKey();
      Set<Column<?>> columns = e.getValue();
      Comment comment = document.createComment("record sequence for " + table);
      recs.appendChild(comment);
      Element rand = document.createElement("random_sequence");
      rand.setAttribute("key", table);
      Element recType = document.createElement("record_type");
      for (Column<?> col : columns) {
        Element field = document.createElement("field");
        field.setAttribute("name", col.getColumn());
        if (col.isEnumerated()) {
          field.setAttribute("type", "Enum");
          field.setAttribute("enumref", col.getName());
        } else {
          field.setAttribute("type", getMyriadType(col.getType()));
        }
        recType.appendChild(field);
        tableCol = Math.max(col.getNumberOfValues(), tableCol);
      }
      rand.appendChild(recType);
      
      Element hydrs = document.createElement("hydrators");
      for (Column<?> col : columns) {
        Element hyd = document.createElement("hydrator");
        hyd.setAttribute("key", "set_" + col.getColumn());
        hyd.setAttribute("type", "simple_clustered_hydrator");
        Element argField = document.createElement("argument");
        argField.setAttribute("key", "field");
        argField.setAttribute("type", "field_ref");
        argField.setAttribute("ref", col.getColumn());
        hyd.appendChild(argField);
        Element argProb = document.createElement("argument");
        argProb.setAttribute("key", "probability");
        argProb.setAttribute("type", "function_ref");
        argProb.setAttribute("ref", "Pr[" + col.getName() + "]");
        hyd.appendChild(argProb);
        hydrs.appendChild(hyd);
      }
      rand.appendChild(hydrs);
      
      Element hydrPlan = document.createElement("hydratorPlan");
      for (Column<?> col : columns) {
        Element hydrRef = document.createElement("hydrator_ref");
        hydrRef.setAttribute("ref", col.getName());
        hydrPlan.appendChild(hydrRef);
      }
      rand.appendChild(hydrPlan);
      createCardinalityEstimator(rand, tableCol);
      createGeneratorTasks(rand, table);
      recs.appendChild(rand);
    }
  }

  private void createGeneratorTasks(Element rand, String table) {
    Element genTasks = document.createElement("generator_tasks");
    Element genTask = document.createElement("generator_task");
    genTask.setAttribute("key", table + ".generate");
    genTask.setAttribute("type", "partitioned_iterator");
    genTasks.appendChild(genTask);
    rand.appendChild(genTasks);
  }
  
  private void createCardinalityEstimator(Element rand, long cardinality) {
    Element cardEstimator = document.createElement("cardinality_estimator");
    cardEstimator.setAttribute("type", "linear_scale_estimator");
    Element cardEstArg = document.createElement("argument");
    cardEstArg.setAttribute("key", "base_cardinality");
    cardEstArg.setAttribute("type", "String");
    cardEstArg.setAttribute("value", cardinality + "");
    cardEstimator.appendChild(cardEstArg);
    rand.appendChild(cardEstimator);
  }
  
  private String getMyriadType(String internalType) {
    return TYPE_MAPPING.get(internalType);
  }
  
  private void writeXml(Document document) throws TransformerException, IOException {
    // Create xml file and parent folders
    File xmlFile = new File(outputDirectory + FILE_SEPARATOR + "prototype.xml");
    Files.createParentDirs(xmlFile);
    Files.touch(xmlFile);

    // set up a transformer
    TransformerFactory transfac = TransformerFactory.newInstance();
    Transformer trans = transfac.newTransformer();
    //trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    trans.setOutputProperty(OutputKeys.STANDALONE, "yes");
    trans.setOutputProperty(OutputKeys.INDENT, "yes");
    trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    // create string from xml tree
    FileWriter fw = new FileWriter(xmlFile);
    StreamResult result = new StreamResult(fw);
    DOMSource source = new DOMSource(document);
    trans.transform(source, result);
  }
  
  private void writeDomains() throws IOException {
    for (Entry<String, Set<Column<?>>> e : relations.entrySet()) {
      Set<Column<?>> columns = e.getValue();
      for (Column<?> col : columns) {
        if (col.isEnumerated()) {
          // TODO write the domain file
          File domainFile = new File(domainDirectory + FILE_SEPARATOR + col.getTable() + "." + col.getColumn());
          Files.createParentDirs(domainFile);
          Files.touch(domainFile);
          Files.write("", domainFile, Charsets.UTF_8);
          Map<?, Long> domain = col.getDomain();
          long total = col.getNumberOfValues();
          Files.append ("# numberofexactvals: " + domain.size(), domainFile, Charsets.UTF_8);
          Files.append("\n", domainFile, Charsets.UTF_8);
          Files.append ("# numberofbins: 0", domainFile, Charsets.UTF_8);
          Files.append("\n", domainFile, Charsets.UTF_8);
          Files.append ("# nullprobability: " + col.getNumNulls() / (double) total, domainFile, Charsets.UTF_8);
          Files.append("\n", domainFile, Charsets.UTF_8);
          for (Entry<?, Long> d : domain.entrySet()) {
            String value = d.getKey().toString();
            long count = d.getValue();
            Files.append ((double) count / total + "\t" + value, domainFile, Charsets.UTF_8);
            Files.append("\n", domainFile, Charsets.UTF_8);
          }
          Files.append("\n", domainFile, Charsets.UTF_8);
        }
      }
    }
  }
  
  private void writeDistributions() throws IOException {
    TypeManager typeManager = TypeManager.getInstance();
    for (Entry<String, Set<Column<?>>> e : relations.entrySet()) {
      Set<Column<?>> columns = e.getValue();
      for (Column<?> col : columns) {
        if (!col.isEnumerated()) {
          // TODO write the distribution file
          Parser<?> parser = typeManager.getParser("", col.getTable(), col.getColumn());
          Histogram<?> distribution = col.getDistribution();
          Map<?, Long> mostFrequent = Histograms.getMostFrequent(distribution);
          double total = Double.valueOf(distribution.getTotalNumberOfValues());
          
          File distributionFile = new File(distributionDirectory + FILE_SEPARATOR + col.getTable() + "." + col.getColumn() + ".distribution");
          Files.createParentDirs(distributionFile);
          Files.touch(distributionFile);
          Files.write("", distributionFile, Charsets.UTF_8);
          Files.append ("# numberofexactvals: " + mostFrequent.size(), distributionFile, Charsets.UTF_8);
          Files.append("\n", distributionFile, Charsets.UTF_8);
          Files.append ("# numberofbins: " + (distribution.getNumberOfBuckets() - mostFrequent.size()), distributionFile, Charsets.UTF_8);
          Files.append("\n", distributionFile, Charsets.UTF_8);
          Files.append ("# nullprobability: " + col.getNumNulls() / total, distributionFile, Charsets.UTF_8);
          Files.append("\n", distributionFile, Charsets.UTF_8);
          for (Entry<?, Long> mf : mostFrequent.entrySet()) {
            String value = parser.toString(mf.getKey());
            Long count = mf.getValue();
            Files.append(count / total + "\t" + value, distributionFile, Charsets.UTF_8);
            Files.append("\n", distributionFile, Charsets.UTF_8);
          }
          for (Bucket<?> buck : distribution) {
            if (!buck.getLowerBound().equals(buck.getUpperBound())) {
              String lBound = parser.toString(buck.getLowerBound());
              String uBound = parser.toString(buck.getUpperBound());
              long count = buck.getFrequency();
              Files.append(count / total + "\t" + lBound + "\t" + uBound, distributionFile, Charsets.UTF_8);
              Files.append("\n", distributionFile, Charsets.UTF_8);
            }
          }
          Files.append("\n", distributionFile, Charsets.UTF_8);
        }
      }
    }
  }
}
