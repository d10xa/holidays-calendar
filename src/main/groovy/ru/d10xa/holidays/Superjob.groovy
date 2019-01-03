package ru.d10xa.holidays

import groovy.cli.picocli.CliBuilder
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.time.LocalDate

class Superjob {

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
        MONTHS.indexOf(element.parent().parent().previousElementSibling().text()) + 1
    }

    static int extractDay(Element element) {
        Integer.valueOf(element.select("div.MonthsList_day").text())
    }

    static int extractYear(Element root) {
        Integer.parseInt(root.select("title").text().find("\\d{4}"))
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
        Elements holidaysElements =
            parsed.select("div.MonthsList_holiday:not(.m_outholiday):not(.m_outshortday):not(.h_color_gray)")
        assert holidaysElements.size() > 100
        assert holidaysElements.size() < 140

        Elements preholidaysElements =
            parsed.select("div.MonthsList_preholiday:not(.m_outholiday):not(.m_outshortday):not(.h_color_gray)")
        assert preholidaysElements.size() > 1
        assert preholidaysElements.size() < 20

        int year = extractYear(parsed)

        def holidays = holidaysElements.collect { e -> LocalDate.of(year, extractMonth(e), extractDay(e)) }
        def preholidays = preholidaysElements.collect { e -> LocalDate.of(year, extractMonth(e), extractDay(e)) }

        String json = JsonOutput.toJson([
            "holidays"   : holidays.collect { it.toString() },
            "preholidays": preholidays.collect { it.toString() }
        ])
        JsonOutput.prettyPrint(json)
    }

    static void run(File input, File output) {
        output.text = html2json(input.text)
    }

}
