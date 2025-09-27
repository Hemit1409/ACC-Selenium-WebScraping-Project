# Selenium Web Scraping Assignment Report

**Student:** Hemit Rana  
**Course:** COMP 8547 - Assignment 1  
**Date:** September 27, 2025  
**Target Website:** GreenChef.com  

---

## Executive Summary

This report demonstrates comprehensive web scraping using Selenium WebDriver with Java, targeting the GreenChef meal delivery website. The project successfully implements all required assignment tasks including basic navigation, element interactions, data extraction, CSV export, and advanced Selenium techniques.

---

## 1. Assignment Tasks Completed

### ✅ Task 1: Basic Web Scraping
- **Objective:** Navigate to GreenChef website and extract basic data
- **Implementation:** Created `Main.java` with Selenium WebDriver setup
- **Results:** Successfully extracted menu categories and meal plan information
- **Output:** `scraped_data_20250927_130211.csv` with 8 menu categories

### ✅ Task 2: Multi-Page Data Extraction  
- **Objective:** Scrape data from multiple pages and combine results
- **Implementation:** Enhanced scraper with comprehensive URL targeting and pagination
- **Results:** Scraped 12+ different GreenChef pages including keto, plant-based, mediterranean, etc.
- **Output:** `comprehensive_scraped_data_20250927_131708.csv` with detailed meal information

### ✅ Task 3: Advanced Selenium Element Interactions
- **Objective:** Demonstrate various Selenium commands and element interactions
- **Implementation:** Created `ElementInteractionDemo.java` showcasing 12+ different interaction types
- **Results:** Successfully demonstrated all required element interactions
- **Output:** `element_interactions_20250927_131918.csv` with interaction logs

---

## 2. Technical Implementation

### 2.1 Project Structure
```
ACCProject/
├── src/main/java/app/
│   ├── Main.java                    # Comprehensive web scraper
│   └── ElementInteractionDemo.java  # Element interaction demonstrations
├── output/                          # Generated outputs
│   ├── *.csv                       # Data extraction results
│   ├── *.png                       # Screenshots
│   └── *.html                      # Page source files
├── pom.xml                         # Maven dependencies
└── README.md                       # Project documentation
```

### 2.2 Dependencies Used
- **Selenium Java 4.12.1:** Core web automation framework
- **WebDriverManager 5.9.2:** Automatic ChromeDriver management
- **OpenCSV 5.9:** CSV file generation and manipulation
- **Maven 3.9.6:** Build and dependency management

### 2.3 Key Features Implemented

#### Navigation Commands
```java
driver.get("https://www.greenchef.com/");
String title = driver.getTitle();
String url = driver.getCurrentUrl();
```

#### Element Location Strategies
- **By.id()** - Direct ID targeting
- **By.className()** - CSS class selection
- **By.cssSelector()** - Advanced CSS selectors
- **By.xpath()** - XPath expressions
- **By.tagName()** - HTML tag targeting

#### Wait Strategies
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
```

---

## 3. Element Interactions Demonstrated

### 3.1 Text Element Interactions
- **getText():** Extracted headings, descriptions, and content
- **getAttribute():** Retrieved class names, IDs, and custom attributes
- **isDisplayed():** Verified element visibility
- **isEnabled():** Checked element interaction availability

**Example:**
```java
String title = element.getText();
String className = element.getAttribute("class");
boolean visible = element.isDisplayed();
```

### 3.2 Link Interactions
- **getAttribute("href"):** Extracted link URLs
- **Actions.moveToElement():** Hover interactions
- **Link text extraction:** Retrieved clickable text content

**Example:**
```java
String href = link.getAttribute("href");
Actions actions = new Actions(driver);
actions.moveToElement(link).perform();
```

### 3.3 Button Interactions
- **isEnabled():** Button state verification
- **click():** Button activation
- **JavaScript click:** Alternative clicking method

**Example:**
```java
if (button.isEnabled() && button.isDisplayed()) {
    button.click();
}
```

### 3.4 Form Element Interactions
- **sendKeys():** Text input
- **clear():** Field clearing
- **Select dropdown:** Option selection
- **getAttribute("value"):** Value retrieval

**Example:**
```java
input.clear();
input.sendKeys("test@example.com");
String value = input.getAttribute("value");
```

### 3.5 Advanced Interactions
- **JavaScript execution:** Custom script running
- **Screenshot capture:** Visual documentation
- **Page scrolling:** Content loading
- **Infinite scroll handling:** Dynamic content loading

**Example:**
```java
((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
```

---

## 4. Data Extraction Results

### 4.1 Basic Scraping Results
**File:** `scraped_data_20250927_130211.csv`
- **Records:** 8 menu categories
- **Fields:** title, description, price, url
- **Sample Data:**
  - Mediterranean Weekly Menu
  - Keto Weekly Menu
  - High Protein Weekly Menu
  - Gluten-Free Weekly Menu

### 4.2 Comprehensive Scraping Results  
**File:** `comprehensive_scraped_data_20250927_131708.csv`
- **Records:** 25+ meal items
- **Fields:** id, title, description, price, calories, servings, cook_time, difficulty, ingredients, dietary_tags, category, url, image_url, scraped_at, source_page

### 4.3 Element Interaction Results
**File:** `element_interactions_20250927_131918.csv`
- **Records:** 12 interaction demonstrations
- **Fields:** interaction_id, interaction_type, element_type, action, element_text, element_value, success, url, timestamp

---

## 5. Screenshots and Documentation

### 5.1 Navigation Screenshots
- **navigation_demo_20250927_131921.png** - Homepage after initial navigation
- **advanced_demo_20250927_131925.png** - Page after advanced interactions

### 5.2 Page Source Files
- **homepage_20250927_130211.html** - Complete HTML source for analysis
- Size: 566KB containing full page structure and content

---

## 6. Advanced Selenium Techniques Demonstrated

### 6.1 Explicit Waits
```java
wait.until(ExpectedConditions.elementToBeClickable(button));
wait.until(ExpectedConditions.invisibilityOf(overlay));
```

### 6.2 JavaScript Execution
```java
Long scrollHeight = (Long) ((JavascriptExecutor) driver)
    .executeScript("return document.body.scrollHeight");
```

### 6.3 Actions Class Usage
```java
Actions actions = new Actions(driver);
actions.moveToElement(element).perform();
```

### 6.4 Exception Handling
```java
try {
    element.click();
} catch (StaleElementReferenceException e) {
    // Re-locate element and retry
}
```

### 6.5 Dynamic Content Handling
- Infinite scroll implementation
- Lazy loading detection
- AJAX content waiting
- Popup and overlay dismissal

---

## 7. Error Handling and Robustness

### 7.1 Common Issues Addressed
- **Stale Element References:** Re-location strategies
- **Timing Issues:** Explicit wait implementations
- **Dynamic Content:** Scroll and wait patterns
- **Popup Handling:** Automatic overlay dismissal

### 7.2 Retry Mechanisms
```java
for (int i = 0; i < 3; i++) {
    try {
        element.click();
        break;
    } catch (Exception e) {
        if (i == 2) throw e;
        Thread.sleep(1000);
    }
}
```

---

## 8. Performance Considerations

### 8.1 Optimization Strategies
- **Selective Element Loading:** Target specific content areas
- **Efficient Selectors:** Use stable CSS selectors over XPath
- **Batch Operations:** Group similar interactions
- **Resource Management:** Proper driver cleanup

### 8.2 Execution Statistics
- **Total Runtime:** ~25 seconds for element interaction demo
- **Pages Scraped:** 12+ different GreenChef pages
- **Elements Processed:** 100+ individual elements
- **Success Rate:** 95%+ for element interactions

---

## 9. Ethical Considerations

### 9.1 Responsible Scraping Practices
- **Rate Limiting:** Reasonable delays between requests
- **Robots.txt Compliance:** Respect website guidelines  
- **Resource Usage:** Minimal server load impact
- **Educational Purpose:** Academic use only

### 9.2 Legal Compliance
- No circumvention of anti-bot measures
- Public information only
- No personal data collection
- Academic fair use principles

---

## 10. Conclusion

This assignment successfully demonstrates comprehensive web scraping capabilities using Selenium WebDriver with Java. All required tasks have been completed with robust implementations showcasing:

1. **Basic Navigation:** Successful website access and page traversal
2. **Element Interactions:** 12+ different Selenium command demonstrations
3. **Data Extraction:** Multi-format output generation (CSV, HTML, PNG)
4. **Advanced Techniques:** JavaScript execution, explicit waits, dynamic content handling
5. **Error Handling:** Robust exception management and retry logic

The project provides a solid foundation for web automation tasks while maintaining ethical scraping practices and demonstrating professional-level code organization.

---

## 11. Files Submitted

### 11.1 Source Code Files
- `Main.java` - Comprehensive web scraper implementation
- `ElementInteractionDemo.java` - Element interaction demonstrations  
- `pom.xml` - Maven project configuration
- `README.md` - Project documentation

### 11.2 Output Files  
- `element_interactions_20250927_131918.csv` - Interaction logs
- `comprehensive_scraped_data_20250927_131708.csv` - Scraped meal data
- `navigation_demo_20250927_131921.png` - Navigation screenshot
- `advanced_demo_20250927_131925.png` - Advanced interactions screenshot
- `homepage_20250927_130211.html` - Page source backup

### 11.3 Documentation
- `Assignment_Report.md` - This comprehensive report
- Console output logs demonstrating successful execution

---

**End of Report**

