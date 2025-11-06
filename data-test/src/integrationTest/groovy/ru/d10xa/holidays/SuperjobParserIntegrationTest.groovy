package ru.d10xa.holidays

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

import static org.junit.jupiter.api.Assertions.*

/**
 * Integration tests for the legacy Superjob parser.
 * Dynamically creates tests for all superjob{year}.json files found in json/ directory.
 * Tests are skipped (ignored) if corresponding HTML files are not available.
 */
class SuperjobParserIntegrationTest extends BaseIntegrationTest {

    /**
     * Dynamically creates a test for each year found in json/ directory.
     * Tests that have no corresponding HTML file will be marked as ignored.
     */
    @TestFactory
    Collection<DynamicTest> testLegacyParserAgainstAllYears() {
        def years = discoverYears("superjob")

        println "Discovered ${years.size()} superjob JSON files for legacy parser: ${years}"

        return years.collect { year ->
            DynamicTest.dynamicTest("superjob${year} (legacy)") {
                testLegacyParserForYear(year)
            }
        }
    }

    /**
     * Tests the legacy parser for a specific year.
     */
    private void testLegacyParserForYear(int year) {
        def htmlFile = findHtmlFile("superjob${year}.html")
        def jsonFile = findJsonFile("superjob${year}.json")

        // Skip test if HTML file doesn't exist (e.g., in CI or for old years)
        Assumptions.assumeTrue(
            htmlFile.exists(),
            "HTML file not available for year ${year} (test skipped)"
        )

        // Verify JSON file exists
        assertTrue(jsonFile.exists(), "JSON file must exist: superjob${year}.json")

        // Read expected results from JSON
        def expectedJson = new JsonSlurper().parseText(jsonFile.text)

        // Parse HTML with legacy Superjob parser
        def actualJsonStr = Superjob.html2json(htmlFile.text)
        def actualJson = new JsonSlurper().parseText(actualJsonStr)

        // Validate structure
        assertTrue(actualJson.containsKey('holidays'), "Result must contain 'holidays' key")
        assertTrue(actualJson.containsKey('preholidays'), "Result must contain 'preholidays' key")

        // Compare holidays
        def expectedHolidays = (expectedJson['holidays'] ?: []).sort()
        def actualHolidays = (actualJson['holidays'] ?: []).sort()

        assertEquals(
            expectedHolidays.size(),
            actualHolidays.size(),
            "Holidays count mismatch for ${year}"
        )

        assertEquals(
            expectedHolidays,
            actualHolidays,
            "Holidays content mismatch for ${year}"
        )

        // Compare preholidays
        def expectedPreholidays = (expectedJson['preholidays'] ?: []).sort()
        def actualPreholidays = (actualJson['preholidays'] ?: []).sort()

        assertEquals(
            expectedPreholidays.size(),
            actualPreholidays.size(),
            "Preholidays count mismatch for ${year}"
        )

        assertEquals(
            expectedPreholidays,
            actualPreholidays,
            "Preholidays content mismatch for ${year}"
        )

        // Sanity checks
        assertTrue(actualHolidays.size() > 50, "Should have reasonable number of holidays (>50)")
        assertTrue(actualHolidays.size() < 200, "Should not have too many holidays (<200)")

        println "✓ Year ${year} (legacy): ${actualHolidays.size()} holidays, ${actualPreholidays.size()} preholidays"
    }
}
