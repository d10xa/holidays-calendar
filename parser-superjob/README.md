# Parser superjob

### build fat jar

```
./gradlew fatJar
```

### run

```
java -jar \
-Dphantomjs.binary.path=./phantomjs/bin/phantomjs \
build/libs/parser-superjob-all.jar 2017 https://www.superjob.ru/proizvodstvennyj_kalendar/2017/ superjob2017.json
```
