package com.github.jochenw.bes.core.model;


public class BesFile extends BesBean<BesFile.Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	public BesFile(Id pId) {
		super (pId);
	}

	private BesMediumBlob.Id mediumBlobId;
	private BesLargeBlob.Id largeBlobId;
	private String name;
	private Long size;

	public BesMediumBlob.Id getMediumBlobId() { return mediumBlobId; }
	public void setMediumBlobId(BesMediumBlob.Id pMediumBlobId) { mediumBlobId = pMediumBlobId; }
	public BesLargeBlob.Id getLargeBlobId() { return largeBlobId; }
	public void setLargeBlobId(BesLargeBlob.Id pLargeBlobId) { largeBlobId = pLargeBlobId; }
	public String getName() { return name; }
	public void setName(String pName) { name = pName; }
	public Long getSize() { return size; }
	public void setSize(Long pSize) { size = pSize; }
}
