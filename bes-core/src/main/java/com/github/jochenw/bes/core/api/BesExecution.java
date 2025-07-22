package com.github.jochenw.bes.core.api;

import java.time.ZonedDateTime;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.bes.core.api.IBesModel.BesBean;

public class BesExecution extends BesBean<BesExecution.Id> {
	public static class Id extends IBesModel.Id {
		Id(Long pId) {
			super(pId);
		}
	}
	private final ZonedDateTime startTime, endTime;
	private final BesJob.Id jobId;

	private BesExecution(long pId, ZonedDateTime pStartTime, ZonedDateTime pEndTime, BesJob.Id pJobId) {
		super(new BesExecution.Id(pId));
		startTime = pStartTime;
		endTime = pEndTime;
		jobId = pJobId;
	}

	public ZonedDateTime getStartTime() { return startTime; }
	public ZonedDateTime getEndTime() { return endTime; }
	public BesJob.Id getJobId() { return jobId; }

	public static BesExecution of(long pId, ZonedDateTime pStartTime, ZonedDateTime pEndTime, Long pJobId) {
		return new BesExecution(pId, Objects.requireNonNull(pStartTime, "StartTime"), pEndTime, new BesJob.Id(pJobId));
	}

	public static BesExecution of(ZonedDateTime pStartTime, ZonedDateTime pEndTime, Long pJobId) {
		return of(pStartTime, pEndTime, pJobId);
	}

	public static BesExecution of(long pId, BesExecution pExecution) {
		return of(pId, pExecution.getStartTime(), pExecution.getEndTime(), pExecution.getJobId().getIdObj());
	}
}
