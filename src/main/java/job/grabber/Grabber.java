package job.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import job.grabber.util.HabrCareerDateTimeParser;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Parse parse;
    private final Store store;
    private final Scheduler scheduler;
    private final int time;

    public Grabber(Parse parse, Store store, Scheduler scheduler, int time) {
        this.parse = parse;
        this.store = store;
        this.scheduler = scheduler;
        this.time = time;
    }

    @Override
    public void init() throws SchedulerException, InterruptedException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(time)
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
        Thread.sleep(150000);
        scheduler.shutdown();
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            List<Post> list;
            try {
                list = parse.list("https://career.habr.com/vacancies/java_developer?page=");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            list.forEach(store::save);
        }
    }



    public static void main(String[] args) throws Exception {
        var cfg = new Properties();
        try (InputStream in = Grabber.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            cfg.load(in);
        }
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        var parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        var store = new PsqlStore(cfg);
        var time = Integer.parseInt(cfg.getProperty("time"));
        new Grabber(parse, store, scheduler, time).init();
    }
}