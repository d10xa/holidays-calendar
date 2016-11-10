#!/bin/bash
for i in 2013 2014 2015 2016 2017
do
  echo "year: $i"
  java -jar \
  -Dphantomjs.binary.path=./phantomjs/bin/phantomjs \
  parser-superjob/build/libs/parser-superjob-all.jar \
  ${i} \
  https://www.superjob.ru/proizvodstvennyj_kalendar/${i}/ \
  json/superjob${i}.json
done
