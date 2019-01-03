package ru.d10xa.holidays.testutil

import org.openqa.selenium.firefox.FirefoxBinary
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions

class BrowserWrapper {
    FirefoxBinary firefoxBinary
    FirefoxOptions firefoxOptions
    FirefoxDriver driver
    String geckodriverPath = System.getenv("GECKODRIVER_PATH") ?:
        "${System.getProperty("user.home")}/geckodriver/geckodriver"

    BrowserWrapper() {
        firefoxBinary = new FirefoxBinary()
        firefoxOptions = new FirefoxOptions()
        System.setProperty("webdriver.gecko.driver", geckodriverPath)
        firefoxBinary.addCommandLineOptions("--headless")
        firefoxOptions.setBinary(firefoxBinary)
        driver = new FirefoxDriver(firefoxOptions)
        def f = new File(geckodriverPath)
        if (!f.exists() || f.isDirectory()) {
            throw new RuntimeException("geckodriver not found at $geckodriverPath")
        }
    }

    String getAndQuit(String url) {
        def pageSource = null
        try {
            driver.get(url)
            pageSource = driver.getPageSource()
        } finally {
            driver.quit()
        }
        pageSource
    }

}
