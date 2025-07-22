package com.github.jochenw.bes.core.api;

import java.util.Objects;

import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.bes.core.api.IBesModel.BesBean;

public class BesOutputFile extends BesBean<BesOutputFile.Id> {
	public static class Id extends IBesModel.Id {
		Id(Long pId) {
			super(pId);
		}
	}

	private final BesExecution.Id executionId;
	private final BesFileContent.Id contentId;
	private final String name;
	private BesOutputFile(Id pId, BesExecution.Id pExecutionId, BesFileContent.Id pContentId, String pName) {
		super(pId);
		executionId = pExecutionId;
		contentId = pContentId;
		name = pName;
	}

	public BesExecution.Id getExecutionId() { return executionId; }
	public BesFileContent.Id getContentId() { return contentId; }
	public String getName() { return name; }


	public static BesOutputFile of(long pId, Long pExecutionId, Long pContentId, String pName) {
		final BesOutputFile.Id id = new BesOutputFile.Id(Long.valueOf(pId));
		final BesExecution.Id executionId = new BesExecution.Id(Long.valueOf(Objects.requireNonNull(pExecutionId, "ExecutionId")));
		final BesFileContent.Id contentId = new BesFileContent.Id(Objects.requireNonNull(pContentId, "ContentId"));
		final String name = Strings.requireNonEmpty(pName, "Name");
		return new BesOutputFile(id, executionId, contentId, name);
	}

	public static BesOutputFile of(Long pExecutionId, Long pContentId, String pName) {
		return of(-1l, pExecutionId, pContentId, pName);
	}

	public static BesOutputFile of(long pId, BesOutputFile pOutputFile) {
		final BesOutputFile of = Objects.requireNonNull(pOutputFile, "OutputFile");
		final BesOutputFile.Id id = new BesOutputFile.Id(Long.valueOf(pId));
		final BesExecution.Id executionId = Objects.requireNonNull(of.getExecutionId(), "ExecutionId");
		final BesFileContent.Id contentId = Objects.requireNonNull(of.getContentId(), "ContentId");
		final String name = Strings.requireNonEmpty(of.getName(), "Name");
		return new BesOutputFile(id, executionId, contentId, name);
	}
}
