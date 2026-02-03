package de.kseek.core.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class SchedulerEvent<T> extends TimerEvent<T> implements Job {
    private String cronExpression;

    private JobExecutionContext jobExecutionContext;

    private JobKey jobKey;

    public SchedulerEvent(TimerListener listener, T parameter, String cronExpression) {
        super(listener, parameter);
        this.cronExpression = cronExpression;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        listener.onTimer(this);
    }

    public JobExecutionContext getJobExecutionContext() {
        return jobExecutionContext;
    }

    public void setJobExecutionContext(JobExecutionContext jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
    }

    public JobKey getJobKey() {
        return jobKey;
    }

    public void setJobKey(JobKey jobKey) {
        this.jobKey = jobKey;
    }

    public String getCronExpression() {
        return cronExpression;
    }
}
