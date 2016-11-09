# Parser consultant

### build fat jar

```
./gradlew fatJar
```

### run

```
java -jar \
-Dphantomjs.binary.path=./phantomjs/bin/phantomjs \
2017 \
http://www.consultant.ru/law/ref/calendar/proizvodstvennye/2017/ \
json/consultant2017.json
```
