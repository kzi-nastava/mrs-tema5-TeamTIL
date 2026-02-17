package rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RideHistoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By rideCards = By.cssSelector("mat-card.ride-card");
    private By favoriteButtons = By.cssSelector("button.favorite-btn");
    private By favoriteIcon = By.cssSelector("button.favorite-btn mat-icon");
    private By rideHistoryHeader = By.cssSelector("h1");

    private By filtersSection = By.cssSelector("section.filters-section");
    private By dateFromInput = By.xpath("//mat-form-field//input[@matInput][1]");
    private By dateToInput = By.xpath("//mat-form-field//input[@matInput][2]");
    private By statusSelect = By.xpath("//mat-select");
    private By applyFiltersButton = By.cssSelector("button.btn-apply");
    private By quickFilterChips = By.cssSelector("mat-chip-option");
    private By rideTime = By.cssSelector(".ride-info .time");
    private By ridePrice = By.cssSelector(".ride-meta .price");
    private By rideStatus = By.cssSelector(".ride-meta .status");

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

    // ===== FILTERING AND SORTING =====

    public void waitForFiltersSection() {
        // Prvo cekamo da je sekcija vidljiva
        wait.until(ExpectedConditions.visibilityOfElementLocated(filtersSection));
        // Zatim cekamo da vidimo da li su svi elementi ucitani
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void setDateFrom(String date) {
        // Pokusavamo prvo sa XPath pristupom
        try {
            WebElement dateFrom = wait.until(ExpectedConditions.visibilityOfElementLocated(dateFromInput));
            dateFrom.clear();
            dateFrom.sendKeys(date);
        } catch (Exception e) {
            // Ako ne uspe, pokusavamo sa JavaScript
            WebDriver driver = this.driver;
            JavascriptExecutor js = (JavascriptExecutor) driver;
            List<WebElement> inputs = driver.findElements(By.cssSelector("input[matInput]"));
            if (!inputs.isEmpty()) {
                WebElement firstInput = inputs.get(0);
                js.executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", firstInput, date);
            }
        }
    }

    public void setDateTo(String date) {
        // Pokusavamo prvo sa XPath pristupom
        try {
            WebElement dateTo = wait.until(ExpectedConditions.visibilityOfElementLocated(dateToInput));
            dateTo.clear();
            dateTo.sendKeys(date);
        } catch (Exception e) {
            // Ako ne uspe, pokusavamo sa JavaScript
            WebDriver driver = this.driver;
            JavascriptExecutor js = (JavascriptExecutor) driver;
            List<WebElement> inputs = driver.findElements(By.cssSelector("input[matInput]"));
            if (inputs.size() >= 2) {
                WebElement secondInput = inputs.get(1);
                js.executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", secondInput, date);
            }
        }
    }

    public void selectStatus(String status) {
        try {
            // Pronalazimo svi mat-select elementi
            List<WebElement> selects = driver.findElements(By.xpath("//mat-select"));
            if (selects.isEmpty()) {
                throw new RuntimeException("Mat-select not found");
            }

            WebElement statusDropdown = selects.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(statusDropdown));
            statusDropdown.click();

            // Cekamo da se mat-option pojavi i biramo opciju po tekstu
            By optionLocator = By.xpath("//mat-option//span[text()='" + status + "']");
            WebElement optionElement = wait.until(ExpectedConditions.elementToBeClickable(optionLocator));
            optionElement.click();

            // Malo vremena da se select zatvori
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to select status: " + status, e);
        }
    }

    public void clickApplyFilters() {
        WebElement applyBtn = wait.until(ExpectedConditions.elementToBeClickable(applyFiltersButton));
        applyBtn.click();

        // Cekamo da se podaci ucitaju
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void clickQuickFilter(String filterName) {
        List<WebElement> chips = driver.findElements(quickFilterChips);
        for (WebElement chip : chips) {
            if (chip.getText().equals(filterName)) {
                chip.click();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return;
            }
        }
        throw new RuntimeException("Quick filter '" + filterName + "' not found");
    }

    public List<String> getRideTimes() {
        List<WebElement> timeElements = driver.findElements(rideTime);
        return timeElements.stream()
                .map(WebElement::getText)
                .toList();
    }

    public List<String> getRidePrices() {
        List<WebElement> priceElements = driver.findElements(ridePrice);
        return priceElements.stream()
                .map(WebElement::getText)
                .toList();
    }

    public List<String> getRideStatuses() {
        List<WebElement> statusElements = driver.findElements(rideStatus);
        return statusElements.stream()
                .map(WebElement::getText)
                .toList();
    }

    public boolean areRidesSortedByDate(boolean ascending) {
        List<String> times = getRideTimes();
        if (times.size() < 2) return true;

        for (int i = 0; i < times.size() - 1; i++) {
            String current = times.get(i);
            String next = times.get(i + 1);

            int comparison = current.compareTo(next);
            if (ascending && comparison > 0) return false;
            if (!ascending && comparison < 0) return false;
        }
        return true;
    }

    public boolean areRidesSortedByPrice(boolean ascending) {
        List<String> prices = getRidePrices();
        if (prices.size() < 2) return true;

        for (int i = 0; i < prices.size() - 1; i++) {
            double currentPrice = extractPrice(prices.get(i));
            double nextPrice = extractPrice(prices.get(i + 1));

            if (ascending && currentPrice > nextPrice) return false;
            if (!ascending && currentPrice < nextPrice) return false;
        }
        return true;
    }

    public boolean allRidesHaveStatus(String expectedStatus) {
        List<String> statuses = getRideStatuses();
        if (statuses.isEmpty()) return false;

        // Normalizuj očekivani status
        String normalizedExpected = expectedStatus.toLowerCase().trim();

        return statuses.stream()
                .map(status -> status.toLowerCase().trim())
                .allMatch(status -> status.equals(normalizedExpected) || status.contains(normalizedExpected));
    }

    private double extractPrice(String priceText) {
        // Izvlacimo numericki deo iz teksta (npr. "1500 RSD" -> 1500)
        String numericPart = priceText.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(numericPart);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public boolean isFiltersSectionVisible() {
        try {
            return driver.findElement(filtersSection).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ===== DATE VALIDATION METHODS =====

    /**
     * Gets ride dates from displayed ride cards
     * @return List of LocalDate objects for each ride
     */
    public List<LocalDate> getRideDates() {
        List<WebElement> timeElements = driver.findElements(rideTime);
        List<LocalDate> dates = new ArrayList<>();

        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("MMM d, yyyy"),
            DateTimeFormatter.ofPattern("MMM dd, yyyy"),
            DateTimeFormatter.ofPattern("MMMM d, yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy"),
            DateTimeFormatter.ofPattern("dd MMM yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        };

        for (WebElement element : timeElements) {
            String text = element.getText();
            LocalDate date = parseDateFromRideText(text, formatters);
            if (date != null) {
                dates.add(date);
            }
        }

        return dates;
    }

    private LocalDate parseDateFromRideText(String text, DateTimeFormatter[] formatters) {
        try {
            String datePart = text;
            if (text.contains(",")) {
                int lastComma = text.lastIndexOf(",");
                if (lastComma > 0) {
                    datePart = text.substring(0, lastComma).trim();
                }
            }

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(datePart, formatter);
                } catch (DateTimeParseException e) {
                    continue;
                }
            }

            return extractDateFromText(datePart);

        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate extractDateFromText(String text) {
        String[] parts = text.split("[,\\s]+");
        if (parts.length >= 3) {
            try {
                int year = -1;
                int day = -1;
                String monthStr = null;

                for (String part : parts) {
                    if (part.matches("\\d{4}")) {
                        year = Integer.parseInt(part);
                    } else if (part.matches("\\d{1,2}")) {
                        day = Integer.parseInt(part);
                    } else if (part.matches("[A-Za-z]+")) {
                        monthStr = part;
                    }
                }

                if (year != -1 && day != -1 && monthStr != null) {
                    int month = getMonthNumber(monthStr);
                    if (month != -1) {
                        return LocalDate.of(year, month, day);
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return null;
    }

    private int getMonthNumber(String monthStr) {
        monthStr = monthStr.toLowerCase();
        if (monthStr.startsWith("jan")) return 1;
        if (monthStr.startsWith("feb")) return 2;
        if (monthStr.startsWith("mar")) return 3;
        if (monthStr.startsWith("apr")) return 4;
        if (monthStr.startsWith("may")) return 5;
        if (monthStr.startsWith("jun")) return 6;
        if (monthStr.startsWith("jul")) return 7;
        if (monthStr.startsWith("aug")) return 8;
        if (monthStr.startsWith("sep")) return 9;
        if (monthStr.startsWith("oct")) return 10;
        if (monthStr.startsWith("nov")) return 11;
        if (monthStr.startsWith("dec")) return 12;
        return -1;
    }

    public boolean allRidesInDateRange(LocalDate from, LocalDate to) {
        List<LocalDate> dates = getRideDates();
        if (dates.isEmpty()) {
            return true;
        }

        return dates.stream()
                .allMatch(date ->
                    (date.isEqual(from) || date.isAfter(from)) &&
                    (date.isEqual(to) || date.isBefore(to))
                );
    }

    public boolean allRidesWithinLastDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days);
        return allRidesInDateRange(startDate, today);
    }

    public boolean allRidesToday() {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = getRideDates();
        return dates.stream().allMatch(date -> date.isEqual(today));
    }
}
