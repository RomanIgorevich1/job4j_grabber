package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private Connection connection;
    private static final String SOURCE_LINK = "https://career.habr.com";

    public static void main(String[] args) throws IOException, SQLException {
        HabrCareerParse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        try (PsqlStore store = new PsqlStore(new Properties())) {
            parse.list(SOURCE_LINK);
            for (Post vacancy : parse.list(SOURCE_LINK)) {
                store.save(vacancy);
            }
            System.out.println(store.getAll());
            System.out.println(store.findById(4));
        }
    }

    public PsqlStore(Properties properties) {
        try {
            InputStream stream = new FileInputStream("src/main/resources/post.properties");
            properties.load(stream);
            Class.forName(properties.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    properties.getProperty("jdbc.url"),
                    properties.getProperty("jdbc.username"),
                    properties.getProperty("jdbc.password")
            );
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into post(name, text, link, created) values (?, ?, ?, ?) "
                        + "on conflict on constraint post_link_key do nothing")) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "select * from post")) {
            statement.execute();
            try (ResultSet resultSet = statement.getResultSet()) {
                while (resultSet.next()) {
                    Post vacancy = new Post();
                    vacancy.setId(resultSet.getInt("id"));
                    vacancy.setTitle(resultSet.getString("name"));
                    vacancy.setLink(resultSet.getString("link"));
                    vacancy.setDescription(resultSet.getString("text"));
                    vacancy.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
                    postList.add(vacancy);
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return postList;
    }

    @Override
    public Post findById(int id) {
        Post post = new Post();
        try (PreparedStatement statement = connection.prepareStatement(
                "select * from post where id = ?")) {
            statement.setInt(1, id);
            statement.execute();
            try (ResultSet resultSet = statement.getResultSet()) {
                while (resultSet.next()) {
                    post.setId(resultSet.getInt("id"));
                    post.setTitle(resultSet.getString("name"));
                    post.setLink(resultSet.getString("link"));
                    post.setDescription(resultSet.getString("text"));
                    post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
