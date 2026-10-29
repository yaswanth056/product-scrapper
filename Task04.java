import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.PrintWriter;

public class Task04 {

    public static void main(String[] args) {

        String url = "https://webscraper.io/test-sites/e-commerce/allinone";

        try {

            // Get current USD to INR exchange rate
            Document exchangeDoc = Jsoup.connect(
                    "https://api.frankfurter.app/latest?from=USD&to=INR")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .get();

            String json = exchangeDoc.text();

            String rateText = json.split("\"INR\":")[1]
                    .split("}")[0];

            double exchangeRate = Double.parseDouble(rateText);

            PrintWriter writer =
                    new PrintWriter(new FileWriter("products.csv"));

            writer.println("Product Name,Price INR,Description,Rating");

            int count = 0;
            int pageNumber = 1;
            int maxPages = 25;

            System.out.println("Connecting to website...\n");

            while (url != null && pageNumber <= maxPages) {

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(15000)
                        .get();

                System.out.println("Reading page " + pageNumber + "...");

                Elements products = doc.select(".thumbnail");

                for (Element product : products) {

                    String name = product.select(".title").text();

                    String priceText =
                            product.select(".price").text();

                    double priceUSD =
                            Double.parseDouble(
                                    priceText.replace("$", ""));

                    // Convert USD to INR
                    double priceINR =
                            priceUSD * exchangeRate;

                    String description =
                            product.select(".description").text();

                    String rating =
                            product.select(".ratings p")
                                    .attr("data-rating");

                    if (rating.isEmpty()) {
                        rating = "Not Available";
                    }

                    System.out.println(
                            "Product: " + name +
                            " | Price: ₹" +
                            String.format("%.2f", priceINR) +
                            " | Rating: " + rating);

                    writer.println(
                            "\"" + name.replace("\"", "\"\"") +
                            "\",₹" +
                            String.format("%.2f", priceINR) +
                            ",\"" +
                            description.replace("\"", "\"\"") +
                            "\"," +
                            rating);

                    count++;
                }

                // Find next page
                Element nextButton =
                        doc.select("li.next a").first();

                if (nextButton != null) {

                    url = nextButton.absUrl("href");

                    if (url.isEmpty()) {
                        url = null;
                    }

                } else {
                    url = null;
                }

                pageNumber++;
            }

            writer.close();

            System.out.println("\n================================");
            System.out.println("SCRAPING COMPLETED");
            System.out.println("================================");
            System.out.println("Total products: " + count);
            System.out.println("Data saved in products.csv");

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
}