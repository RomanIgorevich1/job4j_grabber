package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static  final String PAGE_LINK = "%s/vacancies/java_developer?page=";
    public static void main(String[] args) throws IOException {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        for (int i = 0; i < 5; i++) {
            Connection connection =
                    Jsoup.connect(String.format(PAGE_LINK.concat(String.valueOf(i + 1)), SOURCE_LINK));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element dataElement = row.select(".vacancy-card__date").first().child(0);
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s%n %s", SOURCE_LINK, linkElement.attr("href"),
                        parser.parse(dataElement.attr("datetime")));
                System.out.printf("%s %s%n", vacancyName, link);
            });
        }
    }
}
