package rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class RateRidePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By rideCards = By.cssSelector("mat-card.ride-card");
    private final By rideStatusBadges = By.cssSelector(".ride-meta .status");

    private final By sidebar = By.cssSelector(".details-sidebar.open");
    private final By sidebarStatus = By.cssSelector(".details-sidebar.open .status");
    private final By rateRideButton = By.cssSelector("[data-testid='rate-btn']");

    private final By ratingDialog = By.cssSelector("mat-dialog-container");
    private final By dialogTitle = By.cssSelector("h2.modal-title");

    private final By driverStarsContainer = By.cssSelector("[data-testid='driver-stars']");
    private final By vehicleStarsContainer = By.cssSelector("[data-testid='vehicle-stars']");

    private final By commentTextarea = By.cssSelector("[data-testid='comment-input']");
    private final By submitButton = By.cssSelector("[data-testid='submit-rating-btn']");

    private final By driverRatingWarning = By.xpath(
            "//mat-dialog-content//div[@class='entity-section'][1]//*[contains(@class,'rating-warning')]"
    );
    private final By vehicleRatingWarning = By.xpath(
            "//mat-dialog-content//div[@class='entity-section'][2]//*[contains(@class,'rating-warning')]"
    );

    public RateRidePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void clickFirstCompletedRide() {
        List<WebElement> cards = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(rideCards)
        );
        List<WebElement> statusBadges = driver.findElements(rideStatusBadges);

        for (int i = 0; i < statusBadges.size(); i++) {
            if (statusBadges.get(i).getText().equalsIgnoreCase("Completed")) {
                wait.until(ExpectedConditions.elementToBeClickable(cards.get(i)));
                cards.get(i).click();
                waitForSidebarToOpen();

                if (isRateButtonVisible()) {
                    return;
                }
            }
        }
        throw new RuntimeException("Nema 'Completed' vožnji u listi.");
    }

    public void clickFirstCanceledRide() {
        List<WebElement> cards = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(rideCards)
        );
        List<WebElement> statusBadges = driver.findElements(rideStatusBadges);

        for (int i = 0; i < statusBadges.size(); i++) {
            if (statusBadges.get(i).getText().equalsIgnoreCase("Canceled")) {
                wait.until(ExpectedConditions.elementToBeClickable(cards.get(i)));
                cards.get(i).click();
                waitForSidebarToOpen();
                return;
            }
        }
        throw new RuntimeException("Nema 'Canceled' vožnji u listi.");
    }

    public void clickFirstRatedRide() {
        By ratedCard = By.cssSelector("mat-card.ride-card[data-rated='true']");
        WebElement card = wait.until(ExpectedConditions.elementToBeClickable(ratedCard));
        card.click();
        waitForSidebarToOpen();
    }

    public void waitForSidebarToOpen() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(sidebar));
        // Čeka da se sidebar animacija završi
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String getSelectedRideStatus() {
        try {
            return driver.findElement(sidebarStatus).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isRateButtonVisible() {
        try {
            List<WebElement> buttons = driver.findElements(rateRideButton);
            return !buttons.isEmpty() && buttons.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickRateRideButton() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(rateRideButton));
        btn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(ratingDialog));
        // Čeka da se dialog u potpunosti inicijalizuje
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialogTitle));
    }

    public boolean isRatingDialogOpen() {
        try {
            return !driver.findElements(ratingDialog).isEmpty()
                    && driver.findElement(ratingDialog).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void setDriverRating(int stars) {
        validateStarValue(stars);
        WebElement container = wait.until(
                ExpectedConditions.visibilityOfElementLocated(driverStarsContainer)
        );
        List<WebElement> starIcons = container.findElements(By.cssSelector("mat-icon"));
        starIcons.get(stars - 1).click();

        // Čeka da se klasa 'active' postavi na zvezdici
        final int starsToCheck = stars;
        wait.until(driver -> {
            WebElement c = driver.findElement(driverStarsContainer);
            List<WebElement> icons = c.findElements(By.cssSelector("mat-icon"));
            return icons.get(starsToCheck - 1).getAttribute("class").contains("active");
        });
    }

    public void setVehicleRating(int stars) {
        validateStarValue(stars);
        WebElement container = wait.until(
                ExpectedConditions.visibilityOfElementLocated(vehicleStarsContainer)
        );
        List<WebElement> starIcons = container.findElements(By.cssSelector("mat-icon"));
        starIcons.get(stars - 1).click();

        final int starsToCheck = stars;
        wait.until(driver -> {
            WebElement c = driver.findElement(vehicleStarsContainer);
            List<WebElement> icons = c.findElements(By.cssSelector("mat-icon"));
            return icons.get(starsToCheck - 1).getAttribute("class").contains("active");
        });
    }

    public void setComment(String text) {
        WebElement textarea = wait.until(
                ExpectedConditions.visibilityOfElementLocated(commentTextarea)
        );
        textarea.clear();
        if (text != null && !text.isEmpty()) {
            textarea.sendKeys(text);
        }
    }

    public void clickSubmit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        btn.click();
    }

    public void closeDialog() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(ratingDialog));
    }

    public void waitForDialogToClose() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(ratingDialog));
    }

    public boolean isDialogClosed() {
        try {
            List<WebElement> dialogs = driver.findElements(ratingDialog);
            return dialogs.isEmpty() || !dialogs.get(0).isDisplayed();
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isSubmitButtonDisabled() {
        try {
            WebElement btn = wait.until(
                    ExpectedConditions.presenceOfElementLocated(submitButton)
            );
            return !btn.isEnabled();
        } catch (Exception e) {
            return true;
        }
    }

    public int getActiveDriverStarCount() {
        WebElement container = driver.findElement(driverStarsContainer);
        List<WebElement> icons = container.findElements(By.cssSelector("mat-icon"));
        return (int) icons.stream()
                .filter(i -> i.getAttribute("class").contains("active"))
                .count();
    }

    public int getActiveVehicleStarCount() {
        WebElement container = driver.findElement(vehicleStarsContainer);
        List<WebElement> icons = container.findElements(By.cssSelector("mat-icon"));
        return (int) icons.stream()
                .filter(i -> i.getAttribute("class").contains("active"))
                .count();
    }

    public boolean isDriverRatingWarningVisible() {
        try {
            return driver.findElement(driverRatingWarning).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isVehicleRatingWarningVisible() {
        try {
            return driver.findElement(vehicleRatingWarning).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private void validateStarValue(int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Ocena mora biti između 1 i 5, prosleđeno: " + stars);
        }
    }
}

