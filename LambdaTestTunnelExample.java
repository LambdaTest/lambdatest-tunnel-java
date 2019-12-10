package com.lambdatest;

import java.net.URL;
import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class LambdaTestTunnelExample {
  private WebDriver driver;
  Tunnel t;

  public static void main() throws Exception {
    String username = System.getenv("LT_USERNAME");
    String access_key = System.getenv("LT_ACCESS_KEY");

    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("browser", "chrome");
    caps.setCapability("version","79.0");
    caps.setCapability("platform", "Windows 10");
    caps.setCapability("tunnel", true);

    t = new Tunnel();
    HashMap<String, String> options = new HashMap<String, String>();
    options.put("key", access_key);
    t.start(options);

    System.out.println("Starting session");
    driver = new RemoteWebDriver(new URL("http://" + username + ":" + access_key + "@hub.lambdatest.com/wd/hub"), caps);
    System.out.println("Started session");

    driver.get("http://localhost");
    System.out.println("Process is running : " + t.isRunning());
    System.out.println("Page title is: " + driver.getTitle());

    driver.quit();
    t.stop();
  }
}