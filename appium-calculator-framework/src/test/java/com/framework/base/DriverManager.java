package com.framework.base;

import com.framework.utils.ConfigReader;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Owns AndroidDriver creation/teardown. Uses a ThreadLocal so the framework
 * is safe for parallel execution (e.g. TestNG parallel="methods") without
 * tests stepping on each other's driver session.
 */
public class DriverManager {

    private static final ThreadLocal<AndroidDriver> DRIVER = new ThreadLocal<>();

    public static AndroidDriver getDriver() {
        return DRIVER.get();
    }

    public static void initDriver() {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName(ConfigReader.get("platformName"));
        options.setDeviceName(ConfigReader.get("deviceName"));
        options.setPlatformVersion(ConfigReader.get("platformVersion"));
        options.setAutomationName(ConfigReader.get("automationName"));
        options.setAppPackage(ConfigReader.get("appPackage"));
        options.setAppActivity(ConfigReader.get("appActivity"));
        options.setNoReset(ConfigReader.getBoolean("noReset"));

        try {
            URL serverUrl = new URL(ConfigReader.get("appium.server.url"));
            AndroidDriver driver = new AndroidDriver(serverUrl, options);
            DRIVER.set(driver);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL in config.properties", e);
        }
    }

    public static void quitDriver() {
        if (DRIVER.get() != null) {
            DRIVER.get().quit();
            DRIVER.remove();
        }
    }
}
