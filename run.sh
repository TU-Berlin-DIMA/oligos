#!/bin/bash
# Path to oligos jar
OLIGOS=target/oligos-0.3.1-jar-with-dependencies.jar
# Host where the database is running (default=localhost)
HOST="localhost"
# database port (default=60000)
PORT=60000
# database username (default=db2inst1)
USER="db2inst1"
# Name of the database (default=sampledb)
DB="sampledb"
# Output directory for the Myriad files (default=/tmp/oligos)
OUT="/tmp/oligos"
# Name of the Myriad generator (default=example-gen)
NAME="example-gen"

if [ "$#" -ne 3 ]; then
    echo "Usage:"
    echo "./run.sh <oracle|db2> <PATH TO JDBC DRIVER> <SCHEMA>"
    exit 1
fi

VENDOR=$1
JDBC=$2
SCHEMA=$3
read -s -p "Password: " PASS
echo ""
java -cp $OLIGOS:$JDBC de.tu_berlin.dima.oligos.Oligos -j $VENDOR -h $HOST -P $PORT -u $USER -p $PASS -D $DB -o $OUT -g $NAME $SCHEMA
