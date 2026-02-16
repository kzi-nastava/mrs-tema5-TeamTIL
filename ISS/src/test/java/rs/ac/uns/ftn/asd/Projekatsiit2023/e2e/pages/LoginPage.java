package rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By emailInput = By.cssSelector("input[type='email']");
    private By passwordInput = By.cssSelector("input[type='password']");
    private By loginButton = By.cssSelector("button.login-btn");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void navigateTo() {
        driver.get("http://localhost:4200/login");
    }

    public void login(String email, String password) {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        passwordField.clear();
        passwordField.sendKeys(password);

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginBtn.click();

        // cekamo da url vise ne sadrzi login
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));

        // cekamo da se stranica potpuno ucita
        wait.until(driver -> ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
    }

    // ako url vise ne sadrzi login znaci da je uspesno logovanje
    public boolean isLoggedIn() {
        return !driver.getCurrentUrl().contains("/login");
    }
}