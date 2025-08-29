package com.github.jochenw.bes.core.model;

import java.time.ZonedDateTime;

public class BesExecution extends BesBean<BesExecution.Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	public BesExecution(BesExecution.Id pId) {
		super(pId);
	}


	private BesJob.Id jobId;
	private BesUser.Id startingUserId;
	private ZonedDateTime startTime, endTime;
	private String errorStackTrace;

	public BesJob.Id getJobId() { return jobId; }
	public void setJobId(BesJob.Id pJobId) { jobId = pJobId; }
	public BesUser.Id getStartingUserId() { return startingUserId; }
	public void setStartingUserId(BesUser.Id pStartingUserId) { startingUserId = pStartingUserId; }
	public ZonedDateTime getStartTime() { return startTime; }
	public void setStartTime(ZonedDateTime pStartTime) { startTime = pStartTime; }
	public ZonedDateTime getEndTime() { return endTime; }
	public void setEndTime(ZonedDateTime pEndTime) { endTime = pEndTime; }
	public String getErrorStackTrace() {return errorStackTrace; }
	public void setErrorStackTrace(String pErrorStackTrace) {errorStackTrace = pErrorStackTrace; }
}
