package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static  Connection connection;

    private static LocalDateTime dateTime = LocalDateTime.now();

    public static void main(String[] args) {
        int interval = Integer.parseInt(readProperties().getProperty("rabbit.interval"));
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("connection", connection);
            dataMap.put("date", dateTime);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(dataMap)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInMinutes(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
        }

        @Override
        public void execute(JobExecutionContext context) {
            Timestamp timestamp = Timestamp.valueOf(dateTime);
           try {
               PreparedStatement statement = connection.prepareStatement(
                       "insert into rabbit(created_date) values (?)");
               statement.setTimestamp(1, timestamp);
               statement.execute();
           } catch (SQLException e) {
               throw new RuntimeException(e);
           }
            System.out.println("Rabbit runs here...");
            LocalDateTime time = (LocalDateTime) context.getJobDetail().getJobDataMap().get("date");
            System.out.println(time);
        }
    }

    private static Properties readProperties() {
        Properties config = new Properties();
        try (InputStream inputStream = AlertRabbit.class.getClassLoader().getResourceAsStream(
                "rabbit.properties")) {
            config.load(inputStream);
            Class.forName(config.getProperty("driver-class-name"));
             connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));
        } catch (IOException | SQLException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return config;
    }
}