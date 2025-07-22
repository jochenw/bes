package com.github.jochenw.bes.core.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SqlBesModelTest {
	@Test
	public void testAsInsertStatement() {
		assertEquals("INSERT INTO besUsers (id, userId, userName, userEmail) VALUES (?, ?, ?, ?)", SqlBesModel.INSERT_USERS);
	}

	@Test
	public void testAsUpdateStatement() {
		assertEquals("UPDATE besUsers SET userId=?, userName=?, userEmail=? WHERE id=?", SqlBesModel.UPDATE_USERS);
	}

	@Test
	public void testAsNewIdStatement() {
		assertEquals("SELECT NEXT VALUE FOR besUsersSeq", SqlBesModel.SELECT_NEW_ID_USERS);
	}
}
