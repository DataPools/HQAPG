package hqapg;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeThread implements Runnable {
	private String query;
	private int point;
	WebDriver driver;
	public ChromeThread(String queryin, int pointin) {
		query = queryin;
		point = pointin;
	}
  //Splits all answers into 3 different windows that are automatically positioned side by side.
	@Override
	public void run() {
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--log-level=3");
		chromeOptions.addArguments("--silent");
		chromeOptions.addArguments("--automatic-tab-discarding=disabled");
		driver = new ChromeDriver(chromeOptions);
		HQAPG.drivers.add(driver);
		driver.manage().window().setSize(new Dimension(500,768));
		driver.manage().window().setPosition(new Point(point, 0));    
		 String url = "https://google.com/xhtml";
		 driver.get("https://google.com/xhtml");
		  WebElement searchBox = driver.findElement(By.name("q"));
		  ((JavascriptExecutor) driver).executeScript("document.getElementsByName(\"q\")[0].setAttribute(\"value\", \""+query+"\");", url);
		  searchBox.submit();
		  
		
	}
	public void switchWebsite(String newSearch) {
		query = newSearch;
		String url = "https://google.com/xhtml";
		 driver.get("https://google.com/");
		  WebElement searchBox = driver.findElement(By.name("q"));
		  ((JavascriptExecutor) driver).executeScript("document.getElementsByName(\"q\")[0].setAttribute(\"value\", \""+query+"\");", url);
		  searchBox.submit();
	}

}
