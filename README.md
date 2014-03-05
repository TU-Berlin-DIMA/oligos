# Summary

Oligos is a data profiler. It reads meta information from database management
systems and creates a data generator specification. Since it is part of the
[Myriad Toolkit]() the generated data generator specification can be used by
_Myriad_ to create data generators that generate data similar to the original
data.

In order to aid query execution all of the current database management systems
(e.g., Oracle DB, IBM DB2) do some kind of bookkeeping, that is storing data
about the data (meta data) in a catalog. This meta data consists of structural
information, such as column types or relations (key - foreign key), and
statistical information, such as one dimensional histograms to capture the
distribution of columns. _Oligos_ uses the catalog data to create a data
generator specification. For the data generator specification _Oligos_ uses the
XML language specified by the [_Myriad Toolkit_](https://github.com/TU-Berlin-
DIMA/myriad-toolkit/wiki/XML-Specification-Reference-Manual).

After successfully executing _Oligos_ the data generator specification can be
used to create a data generator. Please see the guide on how to use _Myriad_
with _Oligos_ in the [_Myriad Wiki_](https://github.com/TU-Berlin-DIMA/myriad-
toolkit/wiki/Using-Oligos-Guide).

# Organisation

_Oligos_ is an application written in Java that uses Maven as a build tool. Thus
the typical Maven project structure is used. The specific project structure is
as follows:

    .
    ├── AUTHORS
    ├── CONTRIBUTE.md
    ├── INSTALL.md
    ├── LICENSE
    ├── NOTICE
    ├── README.md

The `AUTHORS` and the `NOTICE` file lists all the _Oligos_ contributors and
other used open source projects respectively. The `CONTRIBUTE` file describes
how to contribute to the _Oligos_ project and  `INSTALL` explains how to install
_Oligos_. `LICENSE` contains detailed license information and `README` is the
current document.

    ├── pom.xml
    ├── run.sh

The `pom.xml` file is the maven build file. The `run.sh` is a helper script for
using the compiled binaries.

    ├── src
    │   ├── main
    │   │   └── java
    │   └── test
    │       ├── java
    │       └── resources

The `src/` folder contains all the source files needed to build and test
_Oligos_. The `src/main/java` folder contains the actual _Oligos_ source code.
Whereas `src/test/java` folder contains the test code for _Oligos_. All test
files are based on [JUnit 4]() and `src/test/resources` folder contains the
resources needed to run the unit tests.

# Prerequisites

_Oligos_ is written in Java and uses
[JDBC](http://www.oracle.com/technetwork/java/javase/jdbc/index.html) to connect
to the database. To run or build _Oligos_ the following things are needed:

* Apache Maven 3 (building only)
* Java Runtime >= 1.6
* An appropriate JDBC driver for database

# Installation and Usage

1. Download the latest stable version from [GitHub](https://github.com/TU-
Berlin-DIMA/oligos/releases) and unpack it or checkout the latest version.

2. Change to the Oligos directory

     ```Shell
     carabolic:~% cd oligos
     ```

3.  Create a "fat-jar" including Oligos and all dependencies (excluding the jdbc
driver)

     ```Shell
     carabolic:~/oligos% mvn assembly:assembly
     ```

4. Configure the `run.sh` file in the root folder

5. Execute `run.sh` with the path to your JDBC driver and the schema

    ```Shell
    carabolic:~/oligos% ./run.sh <PATH/TO/JDBC-DRIVER.jar> '<SCHEMA> (<TABLE> (<COLUMN>))'
    ```

# Example

Assuming your the jdbc driver for your database is located under `/tmp/my-
jdbc.jar` and your database has a `ORG` schema with a `STUDENT` and a `COURSE`
table, the command to profile the `ID`, `FIRSTNAME` and `LASTNAME` column from
the `STUDENT` table and the whole `COURSE` table is:

`./run.sh /tmp/my-jdbc.jar 'ORG (STUDENT (ID, FIRSTNAME, LASTNAME, AGE), COURSE)'`

# License

_Oligos_ is licensed under the Apache Software License Version 2.0. For more
information please consult the LICENSE file.

# Contact

* [Mailing list](mailto:dima-myriad.toolkit@lists.tu-berlin.de)
* [Project Wiki](https://github.com/TU-Berlin-DIMA/oligos/wiki)
