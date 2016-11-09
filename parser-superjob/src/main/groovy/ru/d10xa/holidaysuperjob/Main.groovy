package ru.d10xa.holidaysuperjob

import geb.navigator.Navigator
import groovy.json.JsonOutput
import org.openqa.selenium.WebDriver
import org.openqa.selenium.phantomjs.PhantomJSDriver
import geb.Browser

import java.time.LocalDate

class Main {

    final static def MONTHS = [
        "Январь",
        "Февраль",
        "Март",
        "Апрель",
        "Май",
        "Июнь",
        "Июль",
        "Август",
        "Сентябрь",
        "Октябрь",
        "Ноябрь",
        "Декабрь"
    ]

    static void main(String[] args) {
        int year = Integer.valueOf(args[-3])
        def url = args[-2]
        def outputJson = args[-1]

        println "year: $year"
        println "url: $url"
        println "output json: $outputJson"

        WebDriver driver = new PhantomJSDriver()
        def browser = new Browser(driver: driver)

        browser.go url

        def holidays = dates(browser.$("div.MonthsList_holiday"), year)
        def preholidays = dates(browser.$("div.MonthsList_preholiday"), year)

        browser.quit()

        println("holidays:")
        println("${holidays.collect{it.toString()}.join('\n')}")
        println("preholidays:")
        println("${preholidays.collect{it.toString()}.join('\n')}")

        String json = JsonOutput.toJson([
            "holidays": holidays.collect{it.toString()},
            "preholidays": preholidays.collect{it.toString()}
        ])

        new File(outputJson).text = JsonOutput.prettyPrint(json)
    }

    static List<LocalDate> dates(Navigator navigator, int year) {
        navigator.collect { div ->
            def mStr = div.parent().parent().previous().text()
            def m = Integer.valueOf(MONTHS.indexOf(mStr)) + 1
            def d = Integer.valueOf(div.text())
            LocalDate.of(year, m, d)
        }
    }
}
