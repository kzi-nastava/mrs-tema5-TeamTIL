package rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class RideHistoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By rideCards = By.cssSelector("mat-card.ride-card");
    private By favoriteButtons = By.cssSelector("button.favorite-btn");
    private By favoriteIcon = By.cssSelector("button.favorite-btn mat-icon");
    private By rideHistoryHeader = By.cssSelector("h1");

    public RideHistoryPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void navigateTo() {
        driver.get("http://localhost:4200/user-ride-history");
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(rideHistoryHeader));
        wait.until(ExpectedConditions.presenceOfElementLocated(rideCards));
    }

    public int getRideCount() {
        List<WebElement> rides = driver.findElements(rideCards);
        return rides.size();
    }

    // dodajemo i uklanjamo rutu iz omiljenih
    public void clickFavoriteOnFirstRide() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(favoriteButtons));
        List<WebElement> favButtons = driver.findElements(favoriteButtons);

        if (favButtons.isEmpty()) {
            throw new RuntimeException("No rides found to favorite");
        }

        WebElement firstButton = favButtons.get(0);

        wait.until(ExpectedConditions.elementToBeClickable(firstButton));

        String iconTextBefore = getFirstFavoriteIconText();

        firstButton.click();

        // cekamo da se ikonica promeni
        wait.until(driver -> {
            String iconTextAfter = getFirstFavoriteIconText();
            return !iconTextAfter.equals(iconTextBefore);
        });

        // dodatno cekanje za backend da se azurira u bazi status
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String getFirstFavoriteIconText() {
        try {
            List<WebElement> icons = driver.findElements(favoriteIcon);
            return icons.isEmpty() ? "" : icons.get(0).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isFirstRideFavorited() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(favoriteIcon));
        List<WebElement> icons = driver.findElements(favoriteIcon);

        if (icons.isEmpty()) {
            return false;
        }

        String iconText = icons.get(0).getText();
        return iconText.equals("star");
    }

    public boolean hasRides() {
        return getRideCount() > 0;
    }

    // osiguravamo da prve dve budu omiljene zbog testa 9
    public void ensureFirstTwoRidesAreFavorited() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(favoriteButtons));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(favoriteIcon));

        List<WebElement> favButtons = driver.findElements(favoriteButtons);

        if (favButtons.size() < 2) {
            throw new RuntimeException("Need at least 2 rides for this test");
        }

        // proveri i dodaj prvu voznju u favorite ako nije
        if (!isRideFavoritedByIndex(0)) {
            favButtons.get(0).click();
            wait.until(driver -> isRideFavoritedByIndex(0));
            try {
                Thread.sleep(500); // Daj backendu vreme
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // proveri i dodaj drugu voznju u favorite ako nije
        favButtons = driver.findElements(favoriteButtons);
        if (!isRideFavoritedByIndex(1)) {
            favButtons.get(1).click();
            wait.until(driver -> isRideFavoritedByIndex(1));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // helper proverava da li je voznja na odg poziciji omiljena
    private boolean isRideFavoritedByIndex(int index) {
        try {
            List<WebElement> icons = driver.findElements(favoriteIcon);
            if (index >= icons.size()) {
                return false;
            }
            return "star".equals(icons.get(index).getText());
        } catch (Exception e) {
            return false;
        }
    }
}
