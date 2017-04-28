#!/bin/bash
for i in 2011 2012 2013 2014 2015 2016 2017 2018
do
  java -jar \
  -Dphantomjs.binary.path=phantomjs/bin/phantomjs \
  parser-consultant/build/libs/parser-consultant-all.jar \
  ${i} \
  http://www.consultant.ru/law/ref/calendar/proizvodstvennye/${i}/ \
  json/consultant${i}.json
done
