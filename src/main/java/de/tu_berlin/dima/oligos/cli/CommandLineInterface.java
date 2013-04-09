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
package de.tu_berlin.dima.oligos.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;

import de.tu_berlin.dima.oligos.Oligos;
import de.tu_berlin.dima.oligos.SparseSchema;
import de.tu_berlin.dima.oligos.db.JdbcConnector;

public class CommandLineInterface {

  private static final Options OPTS = new Options()
      .addOption("u", "username", true, "Username for database connection")
      .addOption("p", "password", true, "Password for database connection")
      .addOption("h", "hostname", true, "Connect to given host")
      .addOption("D", "database", true, "Use given database")
      .addOption("P", "port", true, "Database port")
      .addOption("o", "output", true, "Path to the output folder")
      .addOption("g", "generator", true, "Name of the generator")
      .addOption("", "help", false, "Show help");
  private static final String USAGE = Oligos.class.getSimpleName() +
      " -u <user> -h <host> -d <database> -p <port> -g <generator name> SCHEMA";
  private static final String HEADER = Oligos.class.getSimpleName()
      + " is a application to infer statistical information from a database catalog.";

  private final String[] inputString;
  private final HelpFormatter helpFormatter;
  private CommandLine commandLine;

  private JdbcConnector jdbcConnector;
  private String username;
  private String password;
  private File outputDirectory;
  private String generatorName;
  private SparseSchema inputSchema;

  public CommandLineInterface(String[] args) {
    this.inputString = args;
    this.helpFormatter = new HelpFormatter();
  }

  public boolean parse() throws ParseException {
    CommandLineParser parser = new PosixParser();
    commandLine = parser.parse(OPTS, inputString);
    if (checkOptions(commandLine, helpFormatter)) {
      // get database credentials
      String hostname = commandLine.getOptionValue("hostname");
      int port = Integer.parseInt(commandLine.getOptionValue("port"));
      String database = commandLine.getOptionValue("database");
      String dbDriver = JdbcConnector.IBM_JDBC_V4;
      this.username = commandLine.getOptionValue("username");
      this.password = commandLine.getOptionValue("password");
      this.jdbcConnector = new JdbcConnector(hostname, port, database, dbDriver);
      // get output information
      this.outputDirectory = new File(commandLine.getOptionValue("output"));
      this.generatorName = commandLine.getOptionValue("generator");
      // get the input schema
      String schemaSequence = StringUtils.join(commandLine.getArgs());
      this.inputSchema = SchemaParser.parse(schemaSequence);
      return true;
    } else {
      return false;
    }
  }

  public JdbcConnector getJdbcConnector() {
    return jdbcConnector;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public String getGeneratorName() {
    return generatorName;
  }

  public SparseSchema getInputSchema() {
    return inputSchema;
  }

  public void printHelpMessage() {
    helpFormatter.printHelp(Oligos.class.getSimpleName() + " <options> <schema>", OPTS);
  }

  private static boolean checkOptions(CommandLine cmd, HelpFormatter formatter) {
    if (cmd.hasOption("help")) {
      formatter.printHelp(USAGE, HEADER, OPTS, "");
      return false;
    }
    if (!cmd.hasOption("username")) {
      System.out
          .println("Please specify a username for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    if (!cmd.hasOption("hostname")) {
      System.out
          .println("Please specify a hostname for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    if (!cmd.hasOption("database")) {
      System.out
          .println("Please specify a database for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    if (!cmd.hasOption("port")) {
      System.out.println("Please specify a port for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }
    if (!cmd.hasOption("generator")) {
      System.out.println("Please specify data generator name");
      formatter.printHelp(USAGE, HEADER, OPTS, "");
      return false;
    }
    if (!cmd.hasOption("output")) {
      System.out.println("Please specify output directory");
      formatter.printHelp(USAGE, HEADER, OPTS, "");
      return false;
    }
    if (!cmd.hasOption("password")) {
      System.out
          .println("Please specify a password for the database connection");
      formatter.printHelp(Oligos.class.getSimpleName(), OPTS);
      return false;
    }

    return true;
  }
}
