package com.github.jochenw.bes.core.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.jochenw.bes.core.api.IBesModel;

class XmlBesModelTest {
	@Test
	void testReadModelUsersSample() {
		final XmlBesModel model = getSampleUsersModel();
		validateSampleUsers(model);
	}

	private XmlBesModel getSampleUsersModel() {
		final XmlBesModel model = new XmlBesModel();
		final Path p = Paths.get("src/test/resources/com/github/jochenw/bes/core/model/model-users-sample.xml");
		assertTrue(Files.isRegularFile(p));
		try (InputStream in = Files.newInputStream(p)) {
			new XmlModelSerializer().read(in, model);
		} catch (IOException ioe) {
			throw Exceptions.show(ioe);
		}
		return model;
	}

	@Test
	void testReadWriteReadModelUsersSample() throws IOException {
		final XmlBesModel model = getSampleUsersModel();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final XmlModelSerializer xms = new XmlModelSerializer();
		xms.write(baos, model);
		final byte[] bytes = baos.toByteArray();
		final XmlBesModel model2 = new XmlBesModel();
		xms.read(new ByteArrayInputStream(bytes), model2);
		validateSampleUsers(model2);
	}
	
	private void validateSampleUsers(final IBesModel pModel) {
		final List<BesUser> list = new ArrayList<>();
		pModel.getUserController().readAll(list::add);
		assertEquals(3, list.size());
		list.sort((bu1, bu2) -> {
			final String userId1 = bu1.getUserId();
			final Long id1 = Long.valueOf(userId1.substring(4));
			final String userId2 = bu2.getUserId();
			final Long id2 = Long.valueOf(userId2.substring(4));
			return Long.compare(id1.longValue(), id2.longValue());
		});
		for (int i = 0;  i < 3;  i++) {
			final BesUser bu = list.get(i);
			assertEquals("user" + i, bu.getUserId());
			assertEquals("user" + i + "@some-employer.com", bu.getEmail());
			assertEquals("User " + i, bu.getName());
		}
	}

	@Test
	void testReadModelJobsSample() {
		final XmlBesModel model = getSampleJobsModel();
		validateSampleJobs(model);
	}

	private XmlBesModel getSampleJobsModel() {
		final XmlBesModel model = new XmlBesModel();
		final Path p = Paths.get("src/test/resources/com/github/jochenw/bes/core/model/model-jobs-sample.xml");
		assertTrue(Files.isRegularFile(p));
		try (InputStream in = Files.newInputStream(p)) {
			new XmlModelSerializer().read(in, model);
		} catch (IOException ioe) {
			throw Exceptions.show(ioe);
		}
		return model;
	}

	private void validateSampleJobs(IBesModel pModel) {
		validateSampleUsers(pModel);
		final List<BesJob> jobs = new ArrayList<>();
		pModel.getJobController().readAll(jobs::add);
		jobs.sort((j1,j2) -> {
			return Long.compare(j1.getId().longValue(), j2.getId().longValue());
		});
		assertEquals(3, jobs.size());
		validateJob(jobs.get(0), 0, 0, "Restart Server");
		validateJob(jobs.get(1), 1, 0, "Validate Db");
		validateJob(jobs.get(2), 2, 2, "Validate Db");
	}

	private void validateJob(BesJob pJob, long pId, long pOwnerId, String pName) {
		assertNotNull(pJob);
		assertEquals(pId, pJob.getId().longValue());
		assertEquals(pOwnerId, pJob.getOwner().longValue());
		assertEquals(pName, pJob.getName());
	}

	@Test
	void testReadWriteReadModelJobsSample() throws IOException {
		final XmlBesModel model = getSampleJobsModel();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final XmlModelSerializer xms = new XmlModelSerializer();
		xms.write(baos, model);
		final byte[] bytes = baos.toByteArray();
		System.out.write(bytes);
		System.out.println();
		final XmlBesModel model2 = new XmlBesModel();
		xms.read(new ByteArrayInputStream(bytes), model2);
		validateSampleJobs(model2);
	}
}
