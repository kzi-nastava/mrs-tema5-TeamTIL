package rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.tests;

import org.junit.jupiter.api.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.base.BaseE2ETest;
import rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages.LoginPage;
import rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages.RideHistoryPage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E tests for functionality 2.9.3: Filtering and sorting ride history overview
 * This suite tests:
 * - Filtering by date range
 * - Filtering by status (Completed/Canceled)
 * - Quick filters (All, Last 7 days, Last month, Completed only, Canceled only)
 * - Combination of filters
 * - Display of empty list after filtering
 * - Date range validation
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RideHistoryFilterSortE2ETest extends BaseE2ETest {

    private LoginPage loginPage;
    private RideHistoryPage rideHistoryPage;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        loginPage = new LoginPage(driver, wait);
        rideHistoryPage = new RideHistoryPage(driver, wait);
    }

    private void loginAsTestUser() {
        loginPage.navigateTo();
        loginPage.login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
    }

    @Test
    @Order(1)
    @DisplayName("1. Filters section is visible on ride history page")
    public void testFiltersSectionVisible() {
        loginAsTestUser();
        assertTrue(loginPage.isLoggedIn(), "User should be logged in");

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        assertTrue(rideHistoryPage.hasRides(), "User should have ride history");
        assertTrue(rideHistoryPage.isFiltersSectionVisible(),
                "Filters section should be visible");
    }

    @Test
    @Order(2)
    @DisplayName("2. Filtering by date range - Last 7 days")
    public void testFilterByDateRange() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        int initialCount = rideHistoryPage.getRideCount();

        rideHistoryPage.waitForFiltersSection();

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        rideHistoryPage.setDateFrom(weekAgo.format(formatter));
        rideHistoryPage.setDateTo(today.format(formatter));

        rideHistoryPage.clickApplyFilters();

        int resultCount = rideHistoryPage.getRideCount();
        assertTrue(resultCount <= initialCount,
                "Filtered results should be less than or equal to initial count");

        if (resultCount > 0) {
            assertTrue(rideHistoryPage.allRidesInDateRange(weekAgo, today),
                    "All displayed rides should be within the specified date range (last 7 days)");
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. Filtering by status - Completed")
    public void testFilterByCompletedStatus() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        rideHistoryPage.waitForFiltersSection();

        rideHistoryPage.selectStatus("Completed");
        rideHistoryPage.clickApplyFilters();

        if (rideHistoryPage.getRideCount() > 0) {
            assertTrue(rideHistoryPage.allRidesHaveStatus("Completed") ||
                      rideHistoryPage.allRidesHaveStatus("COMPLETED") ||
                      rideHistoryPage.allRidesHaveStatus("completed"),
                    "All displayed rides should have Completed status");
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. Filtering by status - Canceled")
    public void testFilterByCanceledStatus() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        rideHistoryPage.waitForFiltersSection();

        rideHistoryPage.selectStatus("Canceled");
        rideHistoryPage.clickApplyFilters();

        if (rideHistoryPage.getRideCount() > 0) {
            assertTrue(rideHistoryPage.allRidesHaveStatus("Canceled") ||
                      rideHistoryPage.allRidesHaveStatus("CANCELED") ||
                      rideHistoryPage.allRidesHaveStatus("canceled"),
                    "All displayed rides should have Canceled status");
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. Quick filter - Last 7 days")
    public void testQuickFilterLast7Days() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        int initialCount = rideHistoryPage.getRideCount();

        rideHistoryPage.clickQuickFilter("Last 7 days");

        int filteredCount = rideHistoryPage.getRideCount();
        assertTrue(filteredCount <= initialCount,
                "Last 7 days filter should show same or fewer rides");

        if (filteredCount > 0) {
            assertTrue(rideHistoryPage.allRidesWithinLastDays(7),
                    "All displayed rides should be from the last 7 days");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Quick filter - Last month")
    public void testQuickFilterLastMonth() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        int initialCount = rideHistoryPage.getRideCount();

        rideHistoryPage.clickQuickFilter("Last month");

        int filteredCount = rideHistoryPage.getRideCount();
        assertTrue(filteredCount <= initialCount,
                "Last month filter should show same or fewer rides");

        if (filteredCount > 0) {
            assertTrue(rideHistoryPage.allRidesWithinLastDays(30),
                    "All displayed rides should be from the last 30 days (month)");
        }
    }

    @Test
    @Order(7)
    @DisplayName("7. Quick filter - All (reset)")
    public void testQuickFilterAll() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        int initialCount = rideHistoryPage.getRideCount();

        rideHistoryPage.clickQuickFilter("Completed only");

        rideHistoryPage.clickQuickFilter("All");
        int allCount = rideHistoryPage.getRideCount();

        assertEquals(initialCount, allCount,
                "All filter should restore all rides");
    }

    @Test
    @Order(8)
    @DisplayName("8. Combination of date range and status filters")
    public void testCombinedDateAndStatusFilter() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        rideHistoryPage.waitForFiltersSection();

        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        rideHistoryPage.setDateFrom(monthAgo.format(formatter));
        rideHistoryPage.setDateTo(today.format(formatter));
        rideHistoryPage.selectStatus("Completed");

        rideHistoryPage.clickApplyFilters();

        if (rideHistoryPage.getRideCount() > 0) {
            assertTrue(rideHistoryPage.allRidesInDateRange(monthAgo, today),
                    "All rides should be within the specified date range");
            assertTrue(rideHistoryPage.allRidesHaveStatus("Completed") ||
                      rideHistoryPage.allRidesHaveStatus("completed"),
                    "Combined filter should show only completed rides in date range");
        }
    }

    @Test
    @Order(9)
    @DisplayName("9. Empty results after filtering with future dates")
    public void testEmptyResultsAfterFiltering() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        rideHistoryPage.waitForFiltersSection();

        LocalDate futureDate = LocalDate.now().plusYears(1);
        LocalDate furtherFutureDate = futureDate.plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        rideHistoryPage.setDateFrom(futureDate.format(formatter));
        rideHistoryPage.setDateTo(furtherFutureDate.format(formatter));

        rideHistoryPage.clickApplyFilters();

        int count = rideHistoryPage.getRideCount();
        assertEquals(0, count,
                "Should have no rides when filtering by future dates");
    }

    @Test
    @Order(10)
    @DisplayName("10. Validation of invalid date range (from date after to date)")
    public void testInvalidDateRangeValidation() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        int initialCount = rideHistoryPage.getRideCount();

        rideHistoryPage.waitForFiltersSection();

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        rideHistoryPage.setDateFrom(today.format(formatter));
        rideHistoryPage.setDateTo(weekAgo.format(formatter));

        rideHistoryPage.clickApplyFilters();

        int resultCount = rideHistoryPage.getRideCount();
        assertTrue(resultCount == 0 || resultCount == initialCount,
                "Invalid date range should either show no results or show all results");
    }

    @Test
    @Order(11)
    @DisplayName("11. Filter results update correctly when applying filters")
    public void testFilterResultsUpdate() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        int initialCount = rideHistoryPage.getRideCount();
        assertTrue(initialCount > 0, "Should have rides to test filtering");

        rideHistoryPage.waitForFiltersSection();
        rideHistoryPage.selectStatus("Completed");
        rideHistoryPage.clickApplyFilters();

        int completedCount = rideHistoryPage.getRideCount();
        assertTrue(completedCount <= initialCount,
                "Completed filter should show same or fewer rides than total");

        rideHistoryPage.selectStatus("Canceled");
        rideHistoryPage.clickApplyFilters();

        int canceledCount = rideHistoryPage.getRideCount();
        assertTrue(canceledCount <= initialCount,
                "Canceled filter should show same or fewer rides than total");
    }

    @Test
    @Order(12)
    @DisplayName("12. Multiple filters applied in sequence affect ride count")
    public void testSequentialFilterApplication() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        int initialCount = rideHistoryPage.getRideCount();
        assertTrue(initialCount > 0, "Should have rides to test filtering");

        rideHistoryPage.clickQuickFilter("Last 7 days");
        int last7DaysCount = rideHistoryPage.getRideCount();
        assertTrue(last7DaysCount <= initialCount,
                "Last 7 days filter should show same or fewer rides");

        rideHistoryPage.clickQuickFilter("Last month");
        int lastMonthCount = rideHistoryPage.getRideCount();
        assertTrue(lastMonthCount >= last7DaysCount,
                "Last month should show more or equal rides than last 7 days");

        rideHistoryPage.waitForFiltersSection();
        rideHistoryPage.selectStatus("Completed");
        rideHistoryPage.clickApplyFilters();

        int completedInMonthCount = rideHistoryPage.getRideCount();
        assertTrue(completedInMonthCount <= lastMonthCount,
                "Adding status filter should reduce results");
    }

    @Test
    @Order(13)
    @DisplayName("13. Combination of quick filter (Last 7 days) with status filter")
    public void testQuickFilterWithStatusFilter() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        rideHistoryPage.clickQuickFilter("Last 7 days");
        int quickFilterCount = rideHistoryPage.getRideCount();

        rideHistoryPage.waitForFiltersSection();
        rideHistoryPage.selectStatus("Completed");
        rideHistoryPage.clickApplyFilters();

        int combinedCount = rideHistoryPage.getRideCount();

        assertTrue(combinedCount <= quickFilterCount,
                "Combined quick filter + status filter should show same or fewer rides");

        if (combinedCount > 0) {
            assertTrue(rideHistoryPage.allRidesWithinLastDays(7),
                    "All rides should be from last 7 days");
            assertTrue(rideHistoryPage.allRidesHaveStatus("Completed") ||
                      rideHistoryPage.allRidesHaveStatus("completed"),
                    "All rides should be Completed");
        }
    }

    @Test
    @Order(14)
    @DisplayName("14. Filtering with today-only date range")
    public void testFilterWithTodayOnly() {
        loginAsTestUser();

        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();

        rideHistoryPage.waitForFiltersSection();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String todayStr = today.format(formatter);

        rideHistoryPage.setDateFrom(todayStr);
        rideHistoryPage.setDateTo(todayStr);
        rideHistoryPage.clickApplyFilters();

        int count = rideHistoryPage.getRideCount();

        if (count > 0) {
            assertTrue(rideHistoryPage.allRidesToday(),
                    "All rides should be from today only");
        }
    }
}

