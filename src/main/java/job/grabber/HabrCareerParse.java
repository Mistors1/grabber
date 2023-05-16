package job.grabber;

import job.grabber.util.DateTimeParser;
import job.grabber.util.HabrCareerDateTimeParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse{

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private final List<Post> list = new ArrayList<>();
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }
    public static void main(String[] args) throws IOException {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse habrCareerParse = new HabrCareerParse(dateTimeParser);
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(String.format("%s%d", PAGE_LINK, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first();
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateTimeRow = dateElement.child(0);
                String vacancyDateTime = dateTimeRow.attr("datetime");
                LocalDateTime date = new HabrCareerDateTimeParser().parse(vacancyDateTime);
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", date, vacancyName, link);
                try {
                    String description = retrieveDescription(link);
                    habrCareerParse.list(vacancyName, link, description, date);
                    System.out.println(description);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element  descriptionElement = document.select(".vacancy-description__text").first();
            return descriptionElement.text();
    }

    @Override
    public  List<Post> list(String title, String link, String description, LocalDateTime createdDate) {
        Post post = new Post(title, link, description, createdDate);
        list.add(post);
        return list;
    }
}
