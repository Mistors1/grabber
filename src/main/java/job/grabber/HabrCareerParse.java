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

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final List<Post> list = new ArrayList<>();
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) throws IOException {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse habrCareerParse = new HabrCareerParse(dateTimeParser);
        for (int i = 1; i <= 5; i++) {
            String linkPage = "%s%s%d".formatted(link, "?page=", i);
            habrCareerParse.parse(linkPage);
        }
        return list;
    }


    private  String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descriptionElement = document.select(".vacancy-description__text").first();
        return descriptionElement.text();
    }

    public void parse(String pageLink) throws IOException {
        Connection connection = Jsoup.connect(pageLink);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        for (Element row : rows) {
            Element dateElement = row.select(".vacancy-card__date").first();
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            Element dateTimeRow = dateElement.child(0);
            String vacancyDateTime = dateTimeRow.attr("datetime");
            LocalDateTime date = new HabrCareerDateTimeParser().parse(vacancyDateTime);
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String description = retrieveDescription(link);
            Post post = new Post(vacancyName, link, description,date);
            list.add(post);
        }
    }

    public static void main(String[] args) throws IOException {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse habrCareerParse = new HabrCareerParse(dateTimeParser);
        System.out.println(habrCareerParse.list(PAGE_LINK).get(2));
    }
}
