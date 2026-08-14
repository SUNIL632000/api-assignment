package com.framework.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtils {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /**
     * Captures a screenshot and saves it under the configured screenshot
     * directory, returning the absolute path of the saved file (useful for
     * attaching to the ExtentReports report).
     */
    public static String capture(WebDriver driver, String testName) {
        try {
            String dirName = ConfigReader.get("screenshotDir");
            Path dir = Paths.get(dirName);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String fileName = testName + "_" + LocalDateTime.now().format(TS_FORMAT) + ".png";
            Path destination = dir.resolve(fileName);

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(srcFile.toPath(), destination);

            return destination.toAbsolutePath().toString();
        } catch (IOException e) {
            System.err.println("Failed to capture screenshot for " + testName + ": " + e.getMessage());
            return null;
        }
    }
}
