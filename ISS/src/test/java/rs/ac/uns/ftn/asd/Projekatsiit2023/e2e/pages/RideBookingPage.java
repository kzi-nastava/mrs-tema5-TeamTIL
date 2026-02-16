package rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class RideBookingPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By startLocationInput = By.cssSelector("input[placeholder='Add starting location']");
    private By endLocationInput = By.cssSelector("input[placeholder='Add ending location']");
    private By favoritesAccordion = By.cssSelector(".accordion-header");
    private By favoriteRouteCards = By.cssSelector(".fav-route-card");
    private By chooseButtons = By.cssSelector("button.yellow-btn-small");
    private By noFavoritesMessage = By.cssSelector("p.no-favorites");
    private By requestRideButton = By.cssSelector("button.main-request-btn");

    public RideBookingPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void navigateTo() {
        driver.get("http://localhost:4200/book");
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(startLocationInput));
        wait.until(ExpectedConditions.presenceOfElementLocated(endLocationInput));
    }

    public void clickFavoritesAccordion() {
        WebElement accordion = wait.until(ExpectedConditions.elementToBeClickable(favoritesAccordion));
        accordion.click();

        // cekamo da se pojave omiljene rute ili poruka da ih nema
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(favoriteRouteCards),
                ExpectedConditions.presenceOfElementLocated(noFavoritesMessage)
        ));
    }

    public boolean hasFavoriteRoutes() {
        try {
            return !driver.findElements(favoriteRouteCards).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public int getFavoriteRoutesCount() {
        if (!hasFavoriteRoutes()) {
            return 0;
        }
        return driver.findElements(favoriteRouteCards).size();
    }

    public void selectFirstFavoriteRoute() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(chooseButtons));
        List<WebElement> buttons = driver.findElements(chooseButtons);

        if (buttons.isEmpty()) {
            throw new RuntimeException("No favorite routes to select");
        }

        WebElement firstButton = buttons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(firstButton));

        firstButton.click();

        // cekamo da se popune start i end lokacija iz omiljene rute
        wait.until(driver -> {
            String start = getStartLocationValue();
            String end = getEndLocationValue();
            return start != null && !start.isEmpty() && end != null && !end.isEmpty();
        });
    }

    public String getStartLocationValue() {
        WebElement input = driver.findElement(startLocationInput);
        return input.getAttribute("value");
    }

    public String getEndLocationValue() {
        WebElement input = driver.findElement(endLocationInput);
        return input.getAttribute("value");
    }

    public boolean isFormPopulated() {
        String start = getStartLocationValue();
        String end = getEndLocationValue();

        return start != null && !start.isEmpty() &&
                end != null && !end.isEmpty();
    }

    public void setStartLocation(String location) {
        WebElement input = driver.findElement(startLocationInput);
        input.clear();
        input.sendKeys(location);
    }

    public void setEndLocation(String location) {
        WebElement input = driver.findElement(endLocationInput);
        input.clear();
        input.sendKeys(location);
    }

    // koristimo kad imamo vse omiljenih ruta
    public void selectFavoriteRouteByIndex(int index) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(chooseButtons));
        List<WebElement> buttons = driver.findElements(chooseButtons);

        if (index >= buttons.size()) {
            throw new RuntimeException("Index " + index + " out of bounds. Only " + buttons.size() + " favorites available");
        }

        WebElement button = buttons.get(index);
        wait.until(ExpectedConditions.elementToBeClickable(button));

        button.click();

        // cekamo da se forma popuni novim podacima
        wait.until(driver -> {
            String start = getStartLocationValue();
            String end = getEndLocationValue();
            return start != null && !start.isEmpty() && end != null && !end.isEmpty();
        });
    }
}
