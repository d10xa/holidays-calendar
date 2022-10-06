package ru.d10xa.holidays

import groovy.cli.picocli.CliBuilder
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import java.time.LocalDate

class Superjob {

    final static ArrayList<String> MONTHS = [
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

    static <A> A headAssert(Collection<A> c) {
        assert c.size() == 1
        c.head()
    }

    static List<List<LocalDate>> daysMap(Element el, int monthIndex, int year) {
        el.select("span")
            .findAll { it.text() == MONTHS[monthIndex] }
            .head().parent().nextElementSibling()
            .select("span")
            .findAll { it.html().isNumber() }
            .dropWhile { it.html().toInteger() != 1 }
            .reverse()
            .dropWhile { it.html().toInteger() < 10 }
            .reverse()
            .groupBy { it.parent().parent().parent().attr("class") }
            .values()
            .collect { it.collect(e -> e.text().toInteger()) }
            .sort { it.size() }
            .collect { lists ->
                lists.collect { day ->
                    LocalDate.of(year, monthIndex + 1, day)
                }
            }
            .reverse()
    }

    static String html2json(String html) {
        Document parsed = Jsoup.parse(html)
        def year = extractYear(parsed)
        List<List<List<LocalDate>>> groupedByMonth =
            MONTHS.indices.collect { daysMap(parsed, it, year) }
        List<Tuple2<List<LocalDate>, List<LocalDate>>> groupedAsTuples =
            groupedByMonth.collect {
                it ->
                    switch (it.size()) {
                        case 2:
                            return new Tuple2(it[1], [] as List<LocalDate>)
                        case 3:
                            return new Tuple2(it[1], it[2])
                        default:
                            throw new IllegalArgumentException(it.toString())
                    }
            }

        List<LocalDate> holidays = groupedAsTuples
            .collect { it[0] }.flatten()
        List<LocalDate> preholidays = groupedAsTuples
            .collect { it[1] }.flatten()
        assert holidays.size() > 100
        assert preholidays.size() < 8
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
