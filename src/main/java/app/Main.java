package app;

import com.opencsv.CSVWriter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private static final Set<String> visitedUrls = new HashSet<>();
    private static int totalItemsScraped = 0;
    
    public static void main(String[] args) {
        System.out.println("=== GreenChef Comprehensive Web Scraper ===");
        System.out.println("Starting comprehensive data extraction...");
        
        // Configure ChromeDriver via WebDriverManager
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,900");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        // options.addArguments("--headless=new"); // Uncomment for headless runs

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        Path outDir = Paths.get("output");
        try {
            if (!Files.exists(outDir)) {
                Files.createDirectories(outDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create output directory", e);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path csvPath = outDir.resolve("comprehensive_scraped_data_" + timestamp + ".csv");
        Path homepagePng = outDir.resolve("homepage_" + timestamp + ".png");
        Path homepageHtml = outDir.resolve("homepage_" + timestamp + ".html");

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath.toFile()))) {
            // Enhanced CSV header with comprehensive fields
            String[] header = new String[]{
                "id", "title", "description", "price", "calories", "servings", 
                "cook_time", "difficulty", "ingredients", "dietary_tags", 
                "category", "url", "image_url", "scraped_at", "source_page"
            };
            writer.writeNext(header);

            // Comprehensive list of GreenChef URLs to scrape
            List<String> targetUrls = List.of(
                "https://www.greenchef.com/",
                "https://www.greenchef.com/menus",
                "https://www.greenchef.com/menu",
                "https://www.greenchef.com/pages/menus-and-plans",
                "https://www.greenchef.com/recipes",
                "https://www.greenchef.com/keto",
                "https://www.greenchef.com/plant-based",
                "https://www.greenchef.com/mediterranean",
                "https://www.greenchef.com/gluten-free",
                "https://www.greenchef.com/high-protein",
                "https://www.greenchef.com/quick-easy",
                "https://www.greenchef.com/calorie-smart"
            );

            for (String url : targetUrls) {
                if (visitedUrls.contains(url)) continue;
                visitedUrls.add(url);
                
                System.out.println("Scraping: " + url);
                try {
                    scrapeUrlComprehensively(driver, wait, writer, url, timestamp);
                } catch (Exception e) {
                    System.err.println("Error scraping " + url + ": " + e.getMessage());
                    // Continue with next URL
                }
            }

            // Save final page source and screenshot
            savePageArtifacts(driver, homepageHtml, homepagePng);

            System.out.println("\n=== SCRAPING COMPLETE ===");
            System.out.println("Total items scraped: " + totalItemsScraped);
            System.out.println("CSV: " + csvPath.toAbsolutePath());
            System.out.println("HTML: " + homepageHtml.toAbsolutePath());
            System.out.println("PNG:  " + homepagePng.toAbsolutePath());
            
        } catch (IOException e) {
            throw new RuntimeException("Error writing CSV or files", e);
        } finally {
            driver.quit();
        }
    }
    
    private static void scrapeUrlComprehensively(WebDriver driver, WebDriverWait wait, 
                                                CSVWriter writer, String url, String timestamp) {
        try {
            driver.get(url);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Handle overlays and popups
            closeOverlayIfPresent(driver, wait);
            Thread.sleep(2000); // Allow page to settle
            
            // Perform comprehensive scrolling to load all content
            performComprehensiveScroll(driver);
            
            // Extract all possible content types
            extractAllContentTypes(driver, writer, url, timestamp);
            
            // Try to find and follow pagination
            handlePagination(driver, wait, writer, url, timestamp);
            
        } catch (Exception e) {
            System.err.println("Error in comprehensive scraping of " + url + ": " + e.getMessage());
        }
    }
    
    private static void performComprehensiveScroll(WebDriver driver) {
        try {
            // Scroll to bottom multiple times to trigger lazy loading
            for (int i = 0; i < 10; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(1500);
                
                // Check if new content loaded
                long currentHeight = (Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
                if (i > 0) {
                    long previousHeight = (Long) ((JavascriptExecutor) driver).executeScript("return arguments[0]", currentHeight);
                    if (currentHeight == previousHeight) break;
                }
            }
            
            // Scroll back to top
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Error during scrolling: " + e.getMessage());
        }
    }
    
    private static void extractAllContentTypes(WebDriver driver, CSVWriter writer, String sourceUrl, String timestamp) {
        // Multiple selector strategies for different content types
        List<By> contentSelectors = List.of(
            // Recipe cards
            By.cssSelector("[data-test*='recipe'], [data-testid*='recipe']"),
            By.cssSelector("article[class*='recipe'], div[class*='recipe']"),
            By.cssSelector(".recipe-card, .meal-card, .menu-item"),
            By.cssSelector("a[href*='/recipes/']"),
            // Menu items
            By.cssSelector(".menu-item, .meal-plan-item"),
            By.cssSelector("[class*='menu'], [class*='meal']"),
            // Product cards
            By.cssSelector(".product-card, .card"),
            By.cssSelector("[class*='product'], [class*='item']"),
            // Generic content
            By.cssSelector("h1, h2, h3, h4"),
            By.cssSelector("a[href*='greenchef']")
        );
        
        for (By selector : contentSelectors) {
            try {
                List<WebElement> elements = driver.findElements(selector);
                for (WebElement element : elements) {
                    try {
                        extractElementData(element, writer, sourceUrl, timestamp);
                    } catch (StaleElementReferenceException e) {
                        // Skip stale elements
                        continue;
                    }
                }
            } catch (Exception e) {
                // Continue with next selector
            }
        }
    }
    
    private static void extractElementData(WebElement element, CSVWriter writer, String sourceUrl, String timestamp) {
        try {
            String id = String.valueOf(totalItemsScraped + 1);
            String title = extractTextWithFallback(element, List.of(
                "h1", "h2", "h3", "h4", ".title", ".name", ".card-title", 
                "[data-test*='title'], [data-testid*='title']"
            ));
            
            String description = extractTextWithFallback(element, List.of(
                "p", ".description", ".desc", ".summary", ".card-description",
                "[data-test*='description'], [data-testid*='description']"
            ));
            
            String price = extractTextWithFallback(element, List.of(
                ".price", ".cost", ".amount", "[class*='price']",
                "[data-test*='price'], [data-testid*='price']"
            ));
            
            String calories = extractTextWithFallback(element, List.of(
                ".calories", ".cal", "[class*='calorie']",
                "[data-test*='calorie'], [data-testid*='calorie']"
            ));
            
            String servings = extractTextWithFallback(element, List.of(
                ".servings", ".serving", "[class*='serving']",
                "[data-test*='serving'], [data-testid*='serving']"
            ));
            
            String cookTime = extractTextWithFallback(element, List.of(
                ".time", ".cook-time", ".duration", "[class*='time']",
                "[data-test*='time'], [data-testid*='time']"
            ));
            
            String difficulty = extractTextWithFallback(element, List.of(
                ".difficulty", ".level", "[class*='difficulty']",
                "[data-test*='difficulty'], [data-testid*='difficulty']"
            ));
            
            String ingredients = extractTextWithFallback(element, List.of(
                ".ingredients", ".ingredient-list", "[class*='ingredient']",
                "[data-test*='ingredient'], [data-testid*='ingredient']"
            ));
            
            String dietaryTags = extractTextWithFallback(element, List.of(
                ".tags", ".dietary", ".badges", "[class*='tag']",
                "[data-test*='tag'], [data-testid*='tag']"
            ));
            
            String category = extractCategoryFromUrl(sourceUrl);
            
            String url = extractHrefWithFallback(element, List.of("a"));
            
            String imageUrl = extractImageUrl(element);
            
            // Only write if we have meaningful data
            if (!title.isEmpty() || !description.isEmpty() || !price.isEmpty()) {
                String[] row = {
                    id, title, description, price, calories, servings, cookTime, difficulty,
                    ingredients, dietaryTags, category, url, imageUrl, timestamp, sourceUrl
                };
                writer.writeNext(row);
                totalItemsScraped++;
                
                if (totalItemsScraped % 10 == 0) {
                    System.out.println("Scraped " + totalItemsScraped + " items so far...");
                }
            }
        } catch (Exception e) {
            // Skip problematic elements
        }
    }
    
    private static String extractTextWithFallback(WebElement base, List<String> selectors) {
        for (String selector : selectors) {
            try {
                WebElement el = base.findElement(By.cssSelector(selector));
                String text = el.getText();
                if (text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            } catch (NoSuchElementException e) {
                // Try next selector
            }
        }
        return "";
    }
    
    private static String extractHrefWithFallback(WebElement base, List<String> selectors) {
        for (String selector : selectors) {
            try {
                WebElement el = base.findElement(By.cssSelector(selector));
                String href = el.getAttribute("href");
                if (href != null && !href.trim().isEmpty()) {
                    return href.trim();
                }
            } catch (NoSuchElementException e) {
                // Try next selector
            }
        }
        return "";
    }
    
    private static String extractImageUrl(WebElement element) {
        try {
            WebElement img = element.findElement(By.cssSelector("img"));
            String src = img.getAttribute("src");
            if (src != null && !src.trim().isEmpty()) {
                return src.trim();
            }
        } catch (NoSuchElementException e) {
            // No image found
        }
        return "";
    }
    
    private static String extractCategoryFromUrl(String url) {
        if (url.contains("keto")) return "Keto";
        if (url.contains("plant-based")) return "Plant-Based";
        if (url.contains("mediterranean")) return "Mediterranean";
        if (url.contains("gluten-free")) return "Gluten-Free";
        if (url.contains("high-protein")) return "High Protein";
        if (url.contains("quick-easy")) return "Quick & Easy";
        if (url.contains("calorie-smart")) return "Calorie Smart";
        if (url.contains("recipes")) return "Recipes";
        if (url.contains("menus")) return "Menus";
        return "General";
    }
    
    private static void handlePagination(WebDriver driver, WebDriverWait wait, CSVWriter writer, String baseUrl, String timestamp) {
        try {
            // Look for pagination elements
            List<By> paginationSelectors = List.of(
                By.cssSelector("button[aria-label*='Next']"),
                By.cssSelector(".pagination-next, .next-page"),
                By.cssSelector("[data-test*='next'], [data-testid*='next']"),
                By.xpath("//button[contains(., 'Next') or contains(., 'More')]"),
                By.xpath("//a[contains(., 'Next') or contains(., 'More')]")
            );
            
            for (int page = 1; page <= 5; page++) { // Limit to 5 pages per URL
                WebElement nextButton = null;
                for (By selector : paginationSelectors) {
                    try {
                        List<WebElement> buttons = driver.findElements(selector);
                        for (WebElement btn : buttons) {
                            if (btn.isDisplayed() && btn.isEnabled()) {
                                nextButton = btn;
                                break;
                            }
                        }
                        if (nextButton != null) break;
                    } catch (Exception e) {
                        // Try next selector
                    }
                }
                
                if (nextButton == null) break;
                
                // Click next page
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);
                    Thread.sleep(3000); // Wait for page load
                    
                    // Extract content from new page
                    extractAllContentTypes(driver, writer, baseUrl + "?page=" + (page + 1), timestamp);
                    
                } catch (Exception e) {
                    System.err.println("Error clicking pagination: " + e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in pagination handling: " + e.getMessage());
        }
    }

    private static void savePageArtifacts(WebDriver driver, Path html, Path png) {
        try {
            Files.writeString(html, driver.getPageSource());
            File shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(shot.toPath(), png);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save page artifacts", e);
        }
    }

    private static void closeOverlayIfPresent(WebDriver driver, WebDriverWait wait) {
        try {
            // Common cookie/marketing modal close buttons
            List<By> candidates = List.of(
                By.cssSelector("button[aria-label='Close']"),
                By.cssSelector(".modal .close, .modal .close-btn"),
                By.cssSelector(".overlay .close, .overlay .close-btn"),
                By.cssSelector("#onetrust-accept-btn-handler"),
                By.cssSelector(".cookie, .cookies, .cookie-banner button"),
                By.cssSelector("[data-test*='close'], [data-testid*='close']"),
                By.cssSelector(".popup-close, .banner-close")
            );
            for (By by : candidates) {
                List<WebElement> els = driver.findElements(by);
                if (!els.isEmpty()) {
                    WebElement el = els.get(0);
                    if (el.isDisplayed() && el.isEnabled()) {
                        try {
                            el.click();
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            // Try JavaScript click
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}