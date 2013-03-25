Oligios
=======

Prerequisites
-------------

1.  Download and extract [IBM DB2 Driver for JDBC and SQLJ](http://www-01.ibm.com/support/docview.wss?uid=swg21363866)

1.  Maven 3

1.  Oracle-JDK 6


Building Oligos
---------------

1.  Checkout the code
    
    `git pull git@bitbucket.org:carabolic/oligos.git`

1.  Change to the Oligos directory

    `cd oligos`

1.  Create a "fat-jar" including Oligos and all dependencies (excluding JDBC)

    `mvn assembly:assembly`


Running Oligos
--------------

1.  Configure the run.sh file in the root folder

1.  Execute run.sh with the path to your JDBC driver and the schema

    `./run.sh PATH/TO/JDBC-DRIVER.jar 'SCHEMA (TABLE (COLUMN))'`

Developing Oligos
-----------------

In order to ease the development and debugging of Oligos, the jdbc drivers are
included in the maven package description (pom.xml). To use them just type the
following:

1.  Change to the path were "db2jcc4.jar" and "db2jcc_license_cu.jar" are stored

1.  Install the jdbc driver with

	`mvn install:install-file -Dfile=db2jcc4.jar -DgroupId=com.ibm.db2 \
		-DartifactId=jdbc -Dversion=4.0 -Dpackaging=jar`

1.  Install the jdbc license with

	`mvn install:install-file -Dfile=db2jcc_license_cu.jar -DgroupId=com.ibm.db2 \
		-DartifactId=jdbc_license -Dversion=4.0 -Dpackaging=jar`

