#!/bin/bash
# Path to oligos jar
OLIGOS=target/oligos-0.3-jar-with-dependencies.jar
# Host where DB2 is running
HOST="localhost"
# DB2 port (default=60000)
PORT=60004
# DB2 username (default=db2inst1)
USER="db2inst2"
# Name of the database (default=sampledb)
DB="TPCH"
# Output directory for the Myriad files (default=/tmp/oligos)
OUT="/tmp/oligos"
# Name of the Myriad generator (default=example-gen)
NAME="example-gen"

if [ "$#" -ne 2 ]; then
    echo "Usage:"
    echo "./run.sh <PATH TO JDBC DRIVER> <SCHEMA>"
    exit 1
fi

JDBC=$1
SCHEMA=$2
read -s -p "Password: " PASS
java -cp $OLIGOS:$JDBC de.tu_berlin.dima.oligos.Oligos -h $HOST -P $PORT -u $USER -p $PASS -D $DB -o $OUT -g $NAME $SCHEMA
