Oligios
=======

Prerequisites
-------------

1.	Download and extract [IBM DB2 Driver for JDBC and SQLJ](https://www14.software.ibm.com/webapp/iwm/web/reg/download.do?source=swg-dm-db2jdbcdriver&S_PKG=dl&lang=en_US&cp=UTF-8)

1.	Change to the path were "db2jcc.jar" and "db2jcc_license_cu.jar" are stored

1.	Install the jdbc driver with

	mvn install:install-file -Dfile=db2jcc.jar -DgroupId=com.ibm.db2 \
		-DartifactId=jdbc -Dversion=4.0 -Dpackaging=jar

1.	Install the jdbc license with

	mvn install:install-file -Dfile=db2jcc_license_cu.jar -DgroupId=com.ibm.db2 \
		-DartifactId=jdbc_license -Dversion=4.0 -Dpackaging=jar

