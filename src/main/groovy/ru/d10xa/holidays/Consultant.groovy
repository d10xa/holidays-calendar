package ru.d10xa.holidays

import groovy.cli.picocli.CliBuilder
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import java.time.LocalDate
import java.time.Year

class Consultant {

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

    static int extractMonth(Element element) {
        MONTHS.indexOf(element.parent().parent().parent().selectFirst(".month").text()) + 1
    }

    static int extractDay(Element element) {
        Integer.valueOf(element.ownText())
    }

    static int extractYear(Element root) {
        Integer.parseInt(root.selectFirst("title").text().find("\\d{4}"))
    }

    static void main(String[] args) {
        def cli = new CliBuilder()
        cli.input(longOpt: 'input', args: 1, argName: 'input', 'input html file')
        cli.output(longOpt: 'output', args: 1, argName: 'output', 'output json file')
        def options = cli.parse(args)
        def output = options.output
        def input = options.input
        run(new File(input), new File(output))
    }

    static String html2json(String html) {
        Document parsed = Jsoup.parse(html)
        int year = extractYear(parsed)
        int currentYear = Year.now().getValue()
        assert year >= 2013
        assert year <= currentYear + 1

        Element content = parsed.selectFirst("#content")
        Closure<List<LocalDate>> extractDates = { String selector ->
            def days = content.select(selector)
            days.collect { dayElement ->
                LocalDate.of(year, extractMonth(dayElement), extractDay(dayElement))
            }
        }

        // 2020-07-01 Голосование по вопросу одобрения изменений в Конституцию Российской Федерации
        def date20200701 = LocalDate.parse("2020-07-01")
        def listWithOptionalDate20200701 = year == 2020 ? [date20200701] : []

        def holidays = (extractDates("td.weekend") + listWithOptionalDate20200701).sort()
        def preholidays = extractDates("td.preholiday")

        def nowork2020 = year == 2020 ? extractDates("td.nowork") - date20200701 : []
        def nowork2021 = year == 2021 ? extractDates("td.nowork") : []

        assert holidays.size() > 100
        assert holidays.size() < 140
        assert preholidays.size() > 1
        assert preholidays.size() < 20
        assert nowork2020.size() == 28 && year == 2020 || nowork2020.size() == 0 && year != 2020
        assert nowork2021.size() == 4 && year == 2021 || nowork2021.size() == 0 && year != 2021

        def nowork = nowork2020 + nowork2021
        def noworkMap = nowork.empty ? [:] : ["nowork": nowork.collect { it.toString() }]

        String json = JsonOutput.toJson([
            "holidays" : holidays.collect { it.toString() },
            "preholidays": preholidays.collect { it.toString() }
        ] + noworkMap)
        JsonOutput.prettyPrint(json)
    }

    static void run(File input, File output) {
        output.text = html2json(input.text)
    }

}
