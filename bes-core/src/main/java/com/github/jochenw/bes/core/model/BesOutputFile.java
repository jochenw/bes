package com.github.jochenw.bes.core.model;

import com.github.jochenw.bes.core.model.BesBean.Id;

public class BesOutputFile extends BesBean<Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	private BesFile.Id fileId;
	private BesExecution.Id executionId;

	public BesOutputFile(com.github.jochenw.bes.core.model.BesOutputFile.Id pId) {
		super(pId);
	}

	public BesFile.Id getFileId() { return fileId; }
	public void setFileId(BesFile.Id pFileId) { fileId = pFileId; }
	public BesExecution.Id getExecutionId() { return executionId; }
	public void setExecutionId(BesExecution.Id pExecutionId) { executionId = pExecutionId; }
}
