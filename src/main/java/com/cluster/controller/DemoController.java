package com.cluster.controller;

import com.cluster.job.HelloJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@RestController
public class DemoController {

    private static final Logger LOG = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private SchedulerFactoryBean schedulerFactory;

    @RequestMapping(value = "/api/stop/{jobName}/{groupName}",
            method = RequestMethod.GET, produces = "application/json")
    public Boolean removeJob(@PathVariable("jobName") String jobName,
                          @PathVariable("groupName") String groupName) {
        Scheduler scheduler = schedulerFactory.getScheduler();
        try {
            scheduler.deleteJob(new JobKey(jobName, groupName));
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping(value = "/api/start/{jobName}/{groupName}/{quantity}/{interval}",
            method = RequestMethod.GET, produces = "application/json")
    public Boolean addJob(@PathVariable("jobName") String jobName,
                          @PathVariable("groupName") String groupName,
                          @PathVariable("quantity") int quantity,
                          @PathVariable("interval") int interval) {
        Scheduler scheduler = schedulerFactory.getScheduler();
        JobDetail job = newJob(HelloJob.class)
              .withIdentity(jobName, groupName)
              .build();

        // Trigger the job to run now
        Trigger trigger = newTrigger()
              .withIdentity(jobName, groupName)
              .startNow()
              .withSchedule(
                  simpleSchedule()
                  .withIntervalInSeconds(interval)
                  .withRepeatCount(quantity)
               )
              .build();

        // Tell quartz to schedule the job using our trigger
        try {
            scheduler.scheduleJob(job, trigger);
            return true;
        } catch (SchedulerException e) {
            LOG.info(e.getMessage());
            return false;
        }
    }
}