# ACC Selenium Web Scraping Project

**COMP 8547 Assignment 1 - Web Scraping with Selenium**

This project demonstrates comprehensive web scraping techniques using Java and Selenium WebDriver, specifically targeting GreenChef.com for data extraction and analysis.

## 🎯 Project Overview

This assignment fulfills all requirements for COMP 8547 Assignment 1, including:
- **Task 1**: Basic web scraping with element extraction
- **Task 2**: Multi-page crawling and data aggregation  
- **Task 3**: Advanced Selenium techniques (waits, popup handling, screenshots)

## 🚀 Features

- **Multiple Scraper Implementations**:
  - `Main.java` - Comprehensive multi-page scraper
  - `ElementInteractionDemo.java` - Demonstrates various Selenium interactions
  - `ComprehensiveLinkButtonTester.java` - Systematic link and button testing
  - `SystematicNavigationTester.java` - Targeted section navigation and data scraping

- **Advanced Selenium Techniques**:
  - Explicit waits with WebDriverWait
  - Popup and overlay handling
  - Stale element reference exception handling
  - JavaScript execution for dynamic content
  - Screenshot capture and HTML source saving

- **Data Export**:
  - CSV files with comprehensive scraped data
  - Screenshots (PNG files) for each page
  - HTML page sources for analysis
  - Detailed logging and error reporting

## 🛠️ Prerequisites

- **Java 11+** (JDK 11 or higher)
- **Maven 3.6+** (for dependency management)
- **Chrome Browser** (latest version recommended)
- **Git** (for version control)

## 📦 Dependencies

- **Selenium WebDriver 4.12.1** - Web automation framework
- **WebDriverManager 5.4.1** - Automatic ChromeDriver management
- **OpenCSV 5.7.1** - CSV file operations
- **ChromeDriver** - Browser automation driver

## 🏃‍♂️ Running the Project

### Quick Start
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/ACC-Selenium-WebScraping-Project.git
cd ACC-Selenium-WebScraping-Project

# Compile and run the main scraper
mvn compile exec:java
```

### Running Specific Scrapers
```bash
# Run comprehensive multi-page scraper
mvn exec:java -Dexec.mainClass="app.Main"

# Run element interaction demonstration
mvn exec:java -Dexec.mainClass="app.ElementInteractionDemo"

# Run comprehensive link/button tester
mvn exec:java -Dexec.mainClass="app.ComprehensiveLinkButtonTester"

# Run systematic navigation tester
mvn exec:java -Dexec.mainClass="app.SystematicNavigationTester"
```

## 📁 Project Structure

```
ACC-Selenium-WebScraping-Project/
├── src/main/java/app/
│   ├── Main.java                           # Main comprehensive scraper
│   ├── ElementInteractionDemo.java        # Selenium interaction demos
│   ├── ComprehensiveLinkButtonTester.java  # Link/button testing
│   └── SystematicNavigationTester.java     # Targeted navigation scraper
├── pom.xml                                # Maven configuration
├── README.md                              # This file
├── Assignment_Report.md                   # Detailed assignment report
├── .gitignore                             # Git ignore rules
└── output/                                # Generated output files
    ├── *.csv                              # Scraped data files
    ├── *.png                              # Screenshots
    └── *.html                             # Page source files
```

## 📊 Output Files

The project generates several types of output files in the `output/` directory:

- **CSV Files**: Structured data with columns for title, description, price, URL, etc.
- **PNG Screenshots**: Visual captures of each scraped page
- **HTML Files**: Complete page source code for analysis
- **Log Files**: Detailed execution logs and error reports

## 🎯 Target Website: GreenChef.com

**Why GreenChef was chosen:**
- Simpler structure compared to Blue Apron and Home Chef
- More stable selectors and less dynamic content
- Easier pagination and link following
- Better suited for educational scraping purposes

## 🔧 Technical Implementation

### Advanced Selenium Techniques Demonstrated:

1. **Explicit Waits**:
   ```java
   WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
   wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".button")));
   ```

2. **Popup Handling**:
   ```java
   // Close overlays and modals
   closeOverlayIfPresent(driver, wait);
   ```

3. **Stale Element Handling**:
   ```java
   try {
       element.click();
   } catch (StaleElementReferenceException e) {
       // Re-locate element and retry
   }
   ```

4. **Multi-page Navigation**:
   ```java
   // Handle pagination and infinite scroll
   performComprehensiveScroll(driver);
   handlePagination(driver, wait, writer, url, timestamp);
   ```

## 📈 Results Summary

- **Total Elements Tested**: 482+ interactions
- **Unique URLs Visited**: Multiple pages across GreenChef.com
- **Data Points Extracted**: Titles, descriptions, prices, URLs, images
- **Screenshots Captured**: One per page/section tested
- **Error Handling**: Robust exception management for production-like scenarios

## 🚨 Important Notes

- **Ethical Scraping**: This project implements polite scraping with delays and respects robots.txt
- **Rate Limiting**: Built-in throttling to avoid overwhelming the target server
- **Error Handling**: Comprehensive exception handling for real-world scenarios
- **Academic Use**: This project is for educational purposes only

## 📝 Assignment Compliance

This project fully addresses all assignment requirements:

✅ **Task 1**: Basic web scraping with element extraction  
✅ **Task 2**: Multi-page crawling and data aggregation  
✅ **Task 3**: Advanced Selenium techniques demonstration  
✅ **Screenshots**: Numbered figures with explanations  
✅ **CSV Export**: Structured data output  
✅ **Code Comments**: Comprehensive documentation  
✅ **Error Handling**: Robust exception management  

## 🤝 Contributing

This is an academic assignment project. For questions or issues, please contact the author.

## 📄 License

This project is created for educational purposes as part of COMP 8547 Assignment 1.

## 👨‍💻 Author

**Hemit Rana**  
COMP 8547 - Web Technologies  
Assignment 1 - Selenium Web Scraping Project

---

*Last Updated: September 2025*