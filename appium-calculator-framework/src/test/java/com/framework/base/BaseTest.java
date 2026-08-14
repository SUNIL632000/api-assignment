package com.framework.base;

import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * All test classes extend this so every @Test method gets a fresh Appium
 * session started before it runs and cleanly closed afterwards. Keeping
 * this here (rather than in each test class) is what makes the individual
 * test classes read as pure business logic.
 */
public class BaseTest {

    protected AndroidDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverManager.initDriver();
        driver = DriverManager.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
