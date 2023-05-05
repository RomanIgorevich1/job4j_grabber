package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private Properties config() {
        var config = new Properties();
        try (InputStream stream = Grabber.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            config.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    private Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
       return scheduler;
    }

    private Store store() {
        return new PsqlStore(config());
    }

    @Override
    public void start(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        int interval = Integer.parseInt(config().getProperty("time"));
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(interval)
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            try {
                for (Post vacancy : parse.list("https://career.habr.com")) {
                    store.save(vacancy);
                }
                System.out.println(store.getAll());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(config().getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream outputStream = socket.getOutputStream()) {
                        outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            outputStream.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            outputStream.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws SchedulerException {
        Grabber grab = new Grabber();
        grab.config();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.start(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
        grab.web(store);
    }
}
