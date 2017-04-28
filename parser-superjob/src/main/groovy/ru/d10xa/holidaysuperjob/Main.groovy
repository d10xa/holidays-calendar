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

        def outputFile = new File(outputJson)

        assert outputFile.parentFile.isDirectory()
        WebDriver driver = new PhantomJSDriver()
        def browser = new Browser(driver: driver)

        browser.go url

        browser.waitFor {browser.$("div.MonthsList_holiday").size() > 100}

        def daysCountInYear = browser.$(".MonthsList_quarter")
            .$("div.MonthsList_date:not(.h_color_gray):not(.m_outholiday)")
            .size()
        if(daysCountInYear != 365  && daysCountInYear != 364) {
            throw new RuntimeException("days count in year $daysCountInYear")
        }

        def holidays = dates(browser
            .$("div.MonthsList_date.MonthsList_holiday:not(.h_color_gray):not(.m_outholiday)"), year)
        def preholidays = dates(browser
            .$("div.MonthsList_date.MonthsList_preholiday:not(.h_color_gray):not(.m_outholiday)"), year)

        browser.quit()

        println("holidays:")
        println("${holidays.collect{it.toString()}.join('\n')}")
        println("preholidays:")
        println("${preholidays.collect{it.toString()}.join('\n')}")

        String json = JsonOutput.toJson([
            "holidays": holidays.collect{it.toString()},
            "preholidays": preholidays.collect{it.toString()}
        ])

        outputFile.text = JsonOutput.prettyPrint(json)
    }

    static List<LocalDate> dates(Navigator navigator, int year) {
        navigator.collect { div ->
            def mStr = div.parent().parent().previous().getAttribute("innerText")
            def m = Integer.valueOf(MONTHS.indexOf(mStr)) + 1
            def d = Integer.valueOf(div.getAttribute("innerText").find( /\d+/ ).toInteger())
            LocalDate.of(year, m, d)
        }
    }
}
