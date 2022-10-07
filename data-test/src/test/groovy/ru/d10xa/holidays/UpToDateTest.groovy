package ru.d10xa.holidays

import groovy.json.JsonSlurper
import org.junit.Test

import static org.junit.Assert.assertEquals

class UpToDateTest {

    @Test
    void extractMaxYearTest() {

        def json = new JsonSlurper().parseText("""
{
    "holidays": [
        "2011-01-01",
        "2011-01-02",
        "2011-01-03",
        "2019-12-28",
        "2019-12-29"
    ]
}
""".trim())
        assertEquals(extractMaxYear(json), 2019)
    }

    @Test
    void isUpToDateWithConsultantRu() {
        def f = getCalendarFile()
        def json = new JsonSlurper().parseText(f.text)
        def maxYear = extractMaxYear(json)

        def holidays = json['holidays'].findAll { it.startsWith("$maxYear-") }
        def preholidays = json['preholidays'].findAll { it.startsWith("$maxYear-") }
        def nowork = (json['nowork'] ?: []).findAll { it.startsWith("$maxYear-") }

        def html = "https://www.consultant.ru/law/ref/calendar/proizvodstvennye/$maxYear/".toURL().text

        def newJsonStr = Consultant.html2json(html)
        def newJson = new JsonSlurper().parseText(newJsonStr)

        assertEquals(holidays, newJson['holidays'])
        assertEquals(preholidays, newJson['preholidays'])
        assertEquals(nowork, newJson['nowork'] ?: [])
    }

    Integer extractMaxYear(Object json) {
        json['holidays']
            .collect { it.takeWhile { it.isNumber() } }
            .collect { Integer.valueOf(it) }
            .max()
    }

    File getCalendarFile() {
        def f = new File("json/calendar.json")

        if (!f.exists()) { // For IDE execution
            f = new File("../json/calendar.json")
        }
        f
    }
}
