package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private final DateTimeParser dateTimeParser;
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = "%s/vacancies/java_developer?page=%d";

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse careerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        System.out.println(careerParse.list(SOURCE_LINK));
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> postList = new ArrayList<>();
        HabrCareerParse careerParse = new HabrCareerParse(dateTimeParser);
        for (int i = 1; i < 5; i++) {
            Connection connection =
                    Jsoup.connect(PAGE_LINK.formatted(SOURCE_LINK, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                String dataElement = row.select(".vacancy-card__date").first().child(0)
                        .attr("datetime");
                String linkElement = titleElement.child(0).attr("href");
                Post vacancy = new Post();
                vacancy.setTitle(titleElement.text());
                vacancy.setLink(String.format("%s%s%n", SOURCE_LINK, linkElement));
                vacancy.setDescription(careerParse.retrieveDescription(linkElement));
                vacancy.setCreated(dateTimeParser.parse(dataElement));
                postList.add(vacancy);
            });
        }
        return postList;
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
