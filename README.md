# Производственный Календарь .json

[ ![Travis](https://img.shields.io/travis/d10xa/holidays-calendar.svg)](https://travis-ci.com/d10xa/holidays-calendar)

[CRON](https://travis-ci.com/d10xa/holidays-calendar/builds). Календарь ежедневно сверяется с consultant.ru.

Для отслеживания изменений, можно нажать кнопку Watch. Кнопка Star даст мне знать, что репозиторий кому-то интересен и 
есть смысл его обновлять каждый год.

## 2020 год :mask: & :fireworks:

Будние дни с 2020-03-30 по 2020-05-08 - нерабочиие (карантин)

2020-06-24 - День празднования 75-й годовщины Победы в Великой Отечественной войне

2020-07-01 — День голосования по поправкам в Конституцию РФ

Все эти дни вынесены в json поле "nowork2020"

Подробно про изменения календаря в 2020 в [описании к пулл реквесту #5](https://github.com/d10xa/holidays-calendar/pull/5)


## single json

[calendar.json](json/calendar.json)

## superjob

[2013](json/superjob2013.json) 
[2014](json/superjob2014.json) 
[2015](json/superjob2015.json) 
[2016](json/superjob2016.json) 
[2017](json/superjob2017.json) 
[2018](json/superjob2018.json) 
[2019](json/superjob2019.json) 
[2020](json/superjob2020.json) 
[2021](json/superjob2021.json) 

## consultant

[2011](json/consultant2011.json) 
[2012](json/consultant2012.json) 
[2013](json/consultant2013.json) 
[2014](json/consultant2014.json) 
[2015](json/consultant2015.json) 
[2016](json/consultant2016.json) 
[2017](json/consultant2017.json)
[2018](json/consultant2018.json)
[2019](json/consultant2019.json)
[2020](json/consultant2020.json)
[2021](json/consultant2021.json)

## json structure

```json
{
    "holidays": [
        "2017-01-01",
        "2017-12-31"
    ],
    "preholidays": [
        "2017-02-22",
        "2017-03-07",
        "2017-11-03"
    ],
    "nowork2020": [
        "2020-03-30",
        "2020-03-31",
    
        "2020-06-24"
    ]
}
```

## curl

```
curl -o src/main/resources/calendar.json https://raw.githubusercontent.com/d10xa/holidays-calendar/master/json/calendar.json
```

## Gradle task

```gradle
task downloadCalendar
downloadCalendar.doLast {
    def f = file("$projectDir/src/main/resources/calendar.json")
    new URL('https://raw.githubusercontent.com/d10xa/holidays-calendar/master/json/calendar.json')
        .withInputStream { i -> f.withOutputStream { it << i } }
}

```

## Использование парсеров

Исходный код страниц можно взять из браузера. Пример прямых ссылок для chrome:

    view-source:https://www.superjob.ru/proizvodstvennyj_kalendar/2019/
    view-source:https://www.consultant.ru/law/ref/calendar/proizvodstvennye/2019/

Как альтернативный вариант, можно использовать headless chrome:

    mkdir html
    export YEAR=2019
    
    chromium --headless --disable-gpu --dump-dom "https://www.superjob.ru/proizvodstvennyj_kalendar/$YEAR" > "html/superjob$YEAR.html"
    chromium --headless --disable-gpu --dump-dom "https://www.consultant.ru/law/ref/calendar/proizvodstvennye/$YEAR/" > "html/consultant$YEAR.html"


Запуск парсера superjob:

    export YEAR=2019
    ./gradlew runSuperjob --args="--input $PWD/html/superjob$YEAR.html --output $PWD/json/superjob$YEAR.json"

Запуск парсера consultant:

    export YEAR=2019
    ./gradlew runConsultant --args="--input $PWD/html/consultant$YEAR.html --output $PWD/json/consultant$YEAR.json"


Объединение в общий файл calendar.json

    ./gradlew mergeJson

