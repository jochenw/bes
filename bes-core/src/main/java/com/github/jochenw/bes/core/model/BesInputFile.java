package com.github.jochenw.bes.core.model;

import com.github.jochenw.bes.core.model.BesBean.Id;

public class BesInputFile extends BesBean<Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	private BesFile.Id fileId;
	private BesJob.Id jobId;

	public BesInputFile(com.github.jochenw.bes.core.model.BesInputFile.Id pId) {
		super(pId);
	}

	public BesFile.Id getFileId() { return fileId; }
	public void setFileId(BesFile.Id pFileId) { fileId = pFileId; }
	public BesJob.Id getJobId() { return jobId; }
	public void setJobId(BesJob.Id pJobId) { jobId = pJobId; }
}
