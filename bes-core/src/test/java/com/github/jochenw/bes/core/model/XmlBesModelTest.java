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
}
