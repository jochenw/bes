package com.github.jochenw.bes.core.api;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.bes.core.api.IBesModel.BesBean;

public class BesInputFile extends BesBean<BesInputFile.Id> {
	public static class Id extends IBesModel.Id {
		Id(Long pId) {
			super(pId);
		}
	}
	private final BesJob.Id jobId;
	private final BesFileContent.Id contentId;
	private final String name;

	private BesInputFile(BesInputFile.Id pId, BesJob.Id pJobId, BesFileContent.Id pContentId, String pName) {
		super(pId);
		jobId = pJobId;
		contentId = pContentId;
		name = pName;
	}

	public BesJob.Id getJobId() { return jobId; }
	public BesFileContent.Id getContentId() { return contentId; }
	public String getName() { return name; }

	public static BesInputFile of(Long pId, Long pOwnerId, Long pJobId, Long pContentId, String pName) {
		return new BesInputFile(new BesInputFile.Id(pId),
				                new BesJob.Id(Objects.requireNonNull(pJobId, "JobId")),
				                new BesFileContent.Id(Objects.requireNonNull(pContentId, "ContentId")),
				                Strings.requireNonEmpty(pName, "Name"));
	}

	public static BesInputFile of(Long pOwnerId, Long pJobId, Long pContentId, String pName) {
		return of(pOwnerId, pJobId, pContentId, pName);
	}

	public static BesInputFile of(long pId, BesInputFile pInputFile) {
		return new BesInputFile(new BesInputFile.Id(pId),
                                Objects.requireNonNull(pInputFile.getJobId(), "JobId"),
		                        Objects.requireNonNull(pInputFile.getContentId(), "ContentId"),
		                        Strings.requireNonEmpty(pInputFile.getName(), "Name"));
	}
}
