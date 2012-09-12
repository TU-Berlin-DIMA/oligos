package de.tu_berlin.dima.oligos.io;

import java.io.File;
import java.io.StringWriter;
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

import com.google.common.collect.Maps;

import de.tu_berlin.dima.oligos.stat.Column;

public class MyriadWriter {

  private final String outputDirectory;
  private final String domainDirectory;
  private final String distributionDirectory;
  private final Map<String, Set<Column<?>>> relations;
  private final Document document;

  private Map<String, Object> parameters;

  public MyriadWriter(Map<String, Set<Column<?>>> relations, String outputDirectory) {
    try {
    this.outputDirectory = outputDirectory;
    this.domainDirectory = outputDirectory + File.pathSeparator + "domain";
    this.distributionDirectory = outputDirectory + File.pathSeparator
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

  public void write() {
    try {
      // create folders
      createDirectory(outputDirectory);
      createDirectory(domainDirectory);
      createDirectory(distributionDirectory);
      
      // create prototype xml document
      Element root = document.createElement("generator_prototype");
      
      // write parameters to xml
      Element params = document.createElement("paramaters");
      writeParameters(params);
      root.appendChild(params);

      // write function to xml
      Element funcs = document.createElement("functions");
      writeFunctions(funcs);
      root.appendChild(funcs);
      
      // write enum_sets to xml
      Element enums = document.createElement("enum_sets");
      writeEnumSets(enums);
      root.appendChild(enums);
      
      // write record sequences
      Element recs = document.createElement("record_sequences");
      writeRecordSequences(recs);
      root.appendChild(recs);

      // transfrom xml to String
      document.appendChild(root);
      String xmlString = getXmlString(document);
      System.out.println(xmlString);
      
      // TODO write xml to disc
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

  private void writeParameters(Element params) {
    for (Entry<String, Object> e : parameters.entrySet()) {
      Element param = document.createElement("parameter");
      param.setAttribute("key", e.getKey());
      Text text = document.createTextNode(e.getValue().toString());
      param.appendChild(text);
      params.appendChild(param);
    }
  }

  private void writeFunctions(Element funcs) {
    for (Entry<String, Set<Column<?>>> e : relations.entrySet()) {
      String table = e.getKey();
      Set<Column<?>> columns = e.getValue();
      Comment comment = document.createComment("functions for " + table);
      funcs.appendChild(comment);
      for (Column<?> col : columns) {
        Element func = document.createElement("function");
        func.setAttribute("key", "Pr[" + col.getName() + "]");
        // TODO get actual type, resp. MyriadType
        func.setAttribute("type", "");
        if (col.isUnique()) {
          Element argMin = document.createElement("argument");
          Element argMax = document.createElement("argument");
          argMin.setAttribute("x_min", col.getMin().toString());
          argMax.setAttribute("x_max", col.getMax().toString());
          func.appendChild(argMin);
          func.appendChild(argMax);
        } else if (col.isEnumerated()) {
          Element argPath = document.createElement("argument");
          argPath.setAttribute("key", "path");
          argPath.setAttribute("type", "String");
          argPath.setAttribute("value", domainDirectory + File.pathSeparator
              + col.getName() + ".dist");
          func.appendChild(argPath);
        }
        funcs.appendChild(func);
      }
    }
  }

  private void writeEnumSets(Element enums) {
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
          argPath.setAttribute("value", domainDirectory + File.pathSeparator
              + col.getName() + ".domain");
          enumSet.appendChild(argPath);
          enums.appendChild(enumSet);
        }
      }
    }
  }

  private void writeRecordSequences(Element recs) {
    for (Entry<String, Set<Column<?>>> e : relations.entrySet()) {
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
       // TODO insert Myriad Type
          field.setAttribute("type", "");
        }
        recType.appendChild(field);
      }
      rand.appendChild(recType);
      
      Element hydrs = document.createElement("hydrators");
      for (Column<?> col : columns) {
        Element hyd = document.createElement("hydrator");
        hyd.setAttribute("key", "set_" + col.getName());
        // TODO set actual myriad hydrator operator
        hyd.setAttribute("type", "");
        Element argField = document.createElement("argument");
        argField.setAttribute("key", "field");
        argField.setAttribute("type", "field_ref");
        argField.setAttribute("ref", col.getName());
        hyd.appendChild(argField);
        Element argProb = document.createElement("argument");
        argProb.setAttribute("key", "probability");
        argProb.setAttribute("type", "function_ref");
        argProb.setAttribute("ref", "Pr[" + col.getName() + "]");
        hyd.appendChild(argProb);
        hydrs.appendChild(hyd);
      }
      rand.appendChild(hydrs);
      
      // TODO add hydrator plan
      recs.appendChild(rand);
    }
  }

  private String getXmlString(Document document) throws TransformerException {
    // set up a transformer
    TransformerFactory transfac = TransformerFactory.newInstance();
    Transformer trans = transfac.newTransformer();
    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    trans.setOutputProperty(OutputKeys.INDENT, "yes");

    // create string from xml tree
    StringWriter sw = new StringWriter();
    StreamResult result = new StreamResult(sw);
    DOMSource source = new DOMSource(document);
    trans.transform(source, result);
    return sw.toString();
  }
}
