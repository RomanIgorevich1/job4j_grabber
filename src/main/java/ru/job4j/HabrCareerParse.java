package ru.job4j;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";

    public static void main(String[] args) throws IOException {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        List<String> listPages = new ArrayList<>();
        listPages.add(String.format("%s/vacancies/java_developer?page=1", SOURCE_LINK));
        listPages.add(String.format("%s/vacancies/java_developer?page=2", SOURCE_LINK));
        listPages.add(String.format("%s/vacancies/java_developer?page=3", SOURCE_LINK));
        listPages.add(String.format("%s/vacancies/java_developer?page=4", SOURCE_LINK));
        listPages.add(String.format("%s/vacancies/java_developer?page=5", SOURCE_LINK));
        for (String page : listPages) {
            Connection connection = Jsoup.connect(page);
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