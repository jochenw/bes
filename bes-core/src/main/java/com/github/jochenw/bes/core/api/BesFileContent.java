package com.github.jochenw.bes.core.api;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.bes.core.api.IBesModel.BesBean;

public class BesFileContent extends BesBean<BesFileContent.Id> {
	public static class Id extends IBesModel.Id {
		Id(Long pId) {
			super(pId);
		}
	}
	private final BesUser.Id ownerId;
	private final byte[] content;
	private final long size;

	private BesFileContent(BesFileContent.Id pId, BesUser.Id pOwnerId, byte[] pContent) {
		super(pId);
		ownerId = pOwnerId;
		content = pContent;
		size = content.length;
	}

	public BesUser.Id getOwnerId() { return ownerId; }
	public byte[] getContent() { return content; }
	public long getSize() { return size; }

	public static BesFileContent of(Long pId, Long pOwnerId, byte[] pContent) {
		return new BesFileContent(new BesFileContent.Id(pId),
								  new BesUser.Id(Objects.requireNonNull(pOwnerId, "Owner")),
				                  Objects.requireNonNull(pContent, "Content"));
	}

	public static BesFileContent of (Long pOwnerId, byte[] pContent) {
		return of(-1l, pOwnerId, pContent);
	}

	public static BesFileContent of(Long pId, BesFileContent pContent) {
		return of(pId, pContent.getOwnerId().getIdObj(), pContent.getContent());
	}
}
