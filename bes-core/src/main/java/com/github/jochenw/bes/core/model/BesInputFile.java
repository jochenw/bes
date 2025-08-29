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
	public static enum Type {
		binary, text, text_interpolated;
	}

	private BesFile.Id fileId;
	private BesJob.Id jobId;
	private Type type;
	private String charset;
	

	public BesInputFile(com.github.jochenw.bes.core.model.BesInputFile.Id pId) {
		super(pId);
	}

	public BesFile.Id getFileId() { return fileId; }
	public void setFileId(BesFile.Id pFileId) { fileId = pFileId; }
	public BesJob.Id getJobId() { return jobId; }
	public void setJobId(BesJob.Id pJobId) { jobId = pJobId; }
	public Type getType() { return type; }
	public void setType(Type pType) { type = pType; }
	public String getCharset() { return charset; }
	public void setCharset(String pCharset) { charset = pCharset; }
}
