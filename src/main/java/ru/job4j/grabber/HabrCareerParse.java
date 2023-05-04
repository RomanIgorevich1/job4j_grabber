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
    private static final String PAGE_LINK = "%s/vacancies/java_developer?page=%d";

    public static void main(String[] args) throws IOException {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        HabrCareerParse careerParse = new HabrCareerParse();
        for (int i = 1; i < 2; i++) {
            Connection connection =
                    Jsoup.connect(PAGE_LINK.formatted(SOURCE_LINK, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element dataElement = row.select(".vacancy-card__date").first().child(0);
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s%n %s", SOURCE_LINK, linkElement.attr("href"),
                        parser.parse(dataElement.attr("datetime")));
                System.out.printf("%s %s%n %s%n", vacancyName, link,
                        careerParse.retrieveDescription(linkElement.attr("href")));
            });

        }
    }
    /**
     * Загрузка описания объявления
     * connection.get().select(".vacancy-description__text").text()
     * @param link ссылка вакансии
     * @return само описание
     */
    private String retrieveDescription(String link) {
        String description = "";
        try {
            Connection connection = Jsoup.connect(String.format("%s%s", SOURCE_LINK, link));
            Document document = connection.get();
            Elements row = document.select(".vacancy-description__text");
            description = row.text();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return description;
    }
}
