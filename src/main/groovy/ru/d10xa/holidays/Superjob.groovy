package ru.d10xa.holidays

import groovy.cli.picocli.CliBuilder
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import java.time.LocalDate
import java.time.Year

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

    static int extractYear(Document document) {
        Integer.parseInt(document.selectFirst("title").text().find("\\d{4}"))
    }

    static Integer extractMonth(Element element) {
        // Поднимаемся вверх по DOM до заголовка месяца
        Element current = element
        for (int i = 0; i < 15 && current != null; i++) {
            current = current.parent()
            if (current) {
                def monthLink = current.selectFirst("a[href*='/proizvodstvennyj_kalendar/']")
                if (monthLink) {
                    def monthText = monthLink.text().trim()
                    def monthIndex = MONTHS.indexOf(monthText)
                    if (monthIndex >= 0) {
                        return monthIndex + 1
                    }
                }
            }
        }
        return null
    }

    static Integer extractDayNumber(Element element) {
        // Число дня находится в самом глубоком span внутри элемента
        def spans = element.select("span")
        if (spans.isEmpty()) {
            return null
        }

        // Ищем с конца - самый глубокий span обычно содержит число
        for (int i = spans.size() - 1; i >= 0; i--) {
            def text = spans[i].text().trim()
            if (text.matches("\\d+")) {
                def num = text.toInteger()
                if (num >= 1 && num <= 31) {
                    return num
                }
            }
        }
        return null
    }

    static String classifyElement(Element element) {
        def classes = element.classNames().join(' ')

        // Предпраздничный день (сокращенный)
        if (classes.contains('Vremya_raboty_sokrascheno')) {
            return 'preholiday'
        }

        // Дни из другого месяца (игнорируем)
        if (classes.contains('Drugoj_mesyac')) {
            return 'other_month'
        }

        // Праздничный день или выходной
        if (classes.contains('Prazdnichnyj_den') || classes.contains('Vyhodnoj')) {
            return 'holiday'
        }

        return 'workday'
    }

    static void assertYear(int year) {
        int currentYear = Year.now().getValue()
        assert year >= 2013
        assert year <= currentYear + 1
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
        Document document = Jsoup.parse(html)
        int year = extractYear(document)
        assertYear(year)

        def holidays = [] as Set<LocalDate>
        def preholidays = [] as Set<LocalDate>

        // 2020-07-01 Голосование по вопросу одобрения изменений в Конституцию Российской Федерации
        if (year == 2020) {
            holidays << LocalDate.parse("2020-07-01")
        }

        // Находим все элементы с f-test-tooltip атрибутами
        document.select("[class*='f-test-tooltip-']").each { element ->
            def type = classifyElement(element)

            // Пропускаем рабочие дни и дни из других месяцев
            if (type == 'workday' || type == 'other_month') {
                return
            }

            def dayNumber = extractDayNumber(element)
            def month = extractMonth(element)

            if (dayNumber && month) {
                try {
                    def date = LocalDate.of(year, month, dayNumber)

                    if (type == 'preholiday') {
                        preholidays << date
                    } else if (type == 'holiday') {
                        holidays << date
                    }
                } catch (Exception e) {
                    // Игнорируем невалидные даты
                }
            }
        }

        // Валидация результатов
        assert holidays.size() > 100, "Too few holidays: ${holidays.size()}"
        assert holidays.size() < 140, "Too many holidays: ${holidays.size()}"
        assert preholidays.size() >= 0, "Invalid preholidays count: ${preholidays.size()}"
        assert preholidays.size() < 20, "Too many preholidays: ${preholidays.size()}"

        String json = JsonOutput.toJson([
            "holidays"   : holidays.sort().collect { it.toString() },
            "preholidays": preholidays.sort().collect { it.toString() }
        ])
        JsonOutput.prettyPrint(json)
    }

    static void run(File input, File output) {
        output.text = html2json(input.text)
    }
}
