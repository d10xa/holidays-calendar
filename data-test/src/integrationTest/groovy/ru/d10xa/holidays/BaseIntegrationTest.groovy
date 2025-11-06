package ru.d10xa.holidays

import groovy.transform.CompileStatic

/**
 * Base class for integration tests that require HTML and JSON files.
 * Provides utility methods for discovering test data files.
 */
@CompileStatic
abstract class BaseIntegrationTest {

    /**
     * Finds the json/ directory, checking both from project root and from data-test subproject.
     */
    protected static File findJsonDir() {
        def dir = new File("json")
        if (!dir.exists()) {
            dir = new File("../json")
        }
        if (!dir.exists()) {
            throw new FileNotFoundException("json/ directory not found")
        }
        return dir
    }

    /**
     * Finds the html/ directory, checking both from project root and from data-test subproject.
     * Returns null if directory doesn't exist (HTML files are optional).
     */
    protected static File findHtmlDir() {
        def dir = new File("html")
        if (!dir.exists()) {
            dir = new File("../html")
        }
        return dir.exists() ? dir : null
    }

    /**
     * Returns a JSON file from the json/ directory.
     */
    protected File findJsonFile(String filename) {
        new File(findJsonDir(), filename)
    }

    /**
     * Returns an HTML file from the html/ directory.
     * File may not exist - caller should check.
     */
    protected File findHtmlFile(String filename) {
        def htmlDir = findHtmlDir()
        if (htmlDir == null) {
            return new File("nonexistent/$filename")
        }
        return new File(htmlDir, filename)
    }

    /**
     * Discovers all years for which JSON files exist with the given prefix.
     * For example, discoverYears("superjob") will find all superjob{year}.json files.
     *
     * @param prefix The filename prefix (e.g., "superjob", "consultant")
     * @return List of years sorted in ascending order
     */
    protected static List<Integer> discoverYears(String prefix) {
        def jsonDir = findJsonDir()
        def pattern = ~/${prefix}(\d{4})\.json/

        return jsonDir.listFiles()
            .findAll { it.isFile() && it.name.matches(pattern) }
            .collect { file ->
                def matcher = file.name =~ pattern
                if (matcher.matches()) {
                    return matcher.group(1).toInteger()
                }
                return null
            }
            .findAll { it != null }
            .sort()
    }

    /**
     * Checks if HTML file exists for the given filename.
     */
    protected boolean hasHtmlFile(String filename) {
        findHtmlFile(filename).exists()
    }
}
