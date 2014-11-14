#!/bin/bash
mkdir -p $2
echo "$2 is output"
java -jar $KB_TOP/lib/jars/inferelator/inferelator.jar $@ 2> $2/error.log
tar cvfz $2.tgz $2
cp $2.tgz /var/tmp/inferelator
rm -rf $2
