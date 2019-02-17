import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;


public class SoundDownloader {

    public static final String BASE_URL = "http://bbcsfx.acropolis.org.uk/?page=%s";
    public static final String FILE_EXTENSION = "wav";
    public static final int TOTAL_PAGES = 641;

    public static final int ITEMS_PER_PAGE = 25;
    public static final int WAIT_TIME = 10;

    //Connection and read timeout values for downloading files
    public static final int CONNECT_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;

    public static void main(String[] args) {
        WebDriver webDriver = new ChromeDriver();
        try {

            int currentPage = 1;

            if(args.length > 0){
                currentPage = Integer.parseInt(args[0]);
            }

            if(currentPage <= 0 || currentPage > TOTAL_PAGES){
                throw new Exception("Start page must be above zero and below " + (TOTAL_PAGES + 1));
            }

            int endPage = (args.length > 1) ? Integer.parseInt(args[1]) : TOTAL_PAGES;

            if(endPage <= 0 || endPage > TOTAL_PAGES){
                throw new Exception("End page must be above zero and below " + (TOTAL_PAGES + 1));
            } else if (endPage < currentPage){
                throw new Exception("End page must be less than start page");
            }

            String pageUrl = String.format(BASE_URL, new String[]{Integer.toString(currentPage)});
            webDriver.get(pageUrl);

            String fileDescription;
            String category;
            String fileUrl;

            while(currentPage <= endPage){
                pageUrl = String.format(BASE_URL, new String[]{Integer.toString(currentPage)});
                System.out.println("Downloading page : " + pageUrl);
                webDriver.manage().timeouts().implicitlyWait(WAIT_TIME, TimeUnit.SECONDS);

                for(int i = 1 ; i <= ITEMS_PER_PAGE; i++){
                    fileDescription = ((ChromeDriver) webDriver).findElementsByXPath("//*[@id=\"example\"]/tbody/tr[" + i + "]/td[1]").get(0).getText();
                    category = ((ChromeDriver) webDriver).findElementsByXPath("//*[@id=\"example\"]/tbody/tr[" + i + "]/td[2]").get(0).getText();
                    fileUrl = ((ChromeDriver) webDriver).findElementByXPath("//*[@id=\"example\"]/tbody/tr[" + i + "]/td[5]/a").getAttribute("href");

                    //This is strange - need to think of something smarter here
                    fileDescription = fileDescription.replace("/","~");

                    if(fileDescription.contains(".") && fileDescription.lastIndexOf(".") == fileDescription.length() - 1){
                        downloadFile(fileUrl, category + " - " + fileDescription + FILE_EXTENSION);
                    } else {
                        downloadFile(fileUrl, category + " - " + fileDescription + "." + FILE_EXTENSION);
                    }
                }

                ((ChromeDriver) webDriver).findElementById("example_next").click();
                currentPage++;
            }
        } catch( NumberFormatException e){
            System.out.println("Input error. Please provide arguments larger above zero and below " + (TOTAL_PAGES + 1));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void downloadFile(String url, String filename) throws Exception{
        FileUtils.copyURLToFile(
                new URL(url),
                new File(filename),
                CONNECT_TIMEOUT,
                READ_TIMEOUT);
        System.out.println("Downloaded file: " + filename);
    }

}
