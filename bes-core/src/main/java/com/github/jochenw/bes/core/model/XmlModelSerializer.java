package com.github.jochenw.bes.core.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.github.jochenw.afw.core.util.Sax;
import com.github.jochenw.afw.core.util.Sax.AbstractContentHandler;
import com.github.jochenw.bes.core.api.IBesModel;

public class XmlModelSerializer {
	public static final String NS = "http://namespaces.github.com/jochenw/bes/xmlModel/1.0.0";

	public void write(OutputStream pOut, IBesModel pModel) {
		Sax.creator()
		   .withCharset(StandardCharsets.UTF_8)
		   .withNamespaceUri(NS)
		   .withIndentation()
		   .withoutXmlDeclaration(false)
		   .write(pOut, (sw0) -> {
			   sw0.writeElement("model", (sw1) -> {
				   sw1.writeElement("users", (sw2) -> {
					   pModel.getUserController().readAll((u) -> {
						   sw2.writeElement("user", (String) null, "id", u.getId().getLongValue(),
								                                   "userId", u.getUserId(),
								                                   "email", u.getEmail(),
								                                   "name", u.getName());
					   });
				   });
			   });
		   });
	}

	public static class XmlModelHandler extends AbstractContentHandler {
		private final IBesModel model;
		private final Map<Long,BesUser> userIds = new HashMap<>();
		private final Set<String> userUIds = new HashSet<>();
		private final Set<String> userEmails = new HashSet<>();
		private boolean inUsers;
		
		public XmlModelHandler(IBesModel pModel) {
			model = pModel;
		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void startElement(String pUri, String pLocalName, String pQName, Attributes pAtts) throws SAXException {
			if (!NS.equals(pUri)) {
				final String name = asQName(pUri, pLocalName);
				throw error("Expected namespace " + NS + ", got " + name);
			}
			switch(incLevel()) {
			case 1:
				if (!"model".equals(pLocalName)) {
					throw error("Expected model element at level 0, got " + pLocalName);
				}
				break;
			case 2:
				if (!"users".equals(pLocalName)) {
					throw error("Expected users element at level 1, got " + pLocalName);
				}
				inUsers = true;
				break;
			case 3:
				if (inUsers) {
					if (!"user".equals(pLocalName)) {
						throw error("Expected user element at level 2, got " + pLocalName);
					}
					final String idStr = pAtts.getValue("id");
					if (idStr == null) {
						throw error("Missing attribute user/@id");
					}
					if (idStr.length() == 0) {
						throw error("Empty attribute user/@id");
					}
					final Long l;
					try {
						l = Long.valueOf(idStr);
					} catch (NumberFormatException nfe) {
						throw error("Invalid attribute user/@id: Expected long number value, got " + idStr);
					}
					if (userIds.containsKey(l)) {
						throw error("Duplicate value for attribute user/@id: " + idStr);
					}
					final String userId = pAtts.getValue("userId");
					if (userId == null) {
						throw error("Missing attribute: user/@userId");
					}
					if (userId.length() == 0) {
						throw error("Missing attribute: user/@userId");
					}
					if (!userUIds.add(userId)) {
						throw error("Duplicate value for attribute user/@userId: " + userId);
					}
					final String email = pAtts.getValue("email");
					if (email == null) {
						throw error("Missing attribute: user/@email");
					}
					if (email.length() == 0) {
						throw error("Empty attribute: user/@email");
					}
					if (!userEmails.add(email)) {
						throw error("Duplicate value for attribute user/@email: " + email);
					}
					final String name = pAtts.getValue("name");
					if (name == null) {
						throw error("Missing attribute: user/@name");
					}
					if (name.length() == 0) {
						throw error("Empty attribute: user/@name");
					}
					final BesUser bu = new BesUser(null);
					bu.setEmail(email);
					bu.setUserId(userId);
					bu.setName(name);
					final BesUser insertedUser = model.getUserController().insert(bu);
					userIds.put(l, insertedUser);
				} else {
					throw error("Unexpected element at level 2, outside of users: " + pLocalName);
				}
				break;
			}
		}

		@Override
		public void endElement(String pUri, String pLocalName, String pQName) throws SAXException {
			if (!NS.equals(pUri)) {
				final String name = asQName(pUri, pLocalName);
				throw error("Expected namespace " + NS + ", got /" + name);
			}
			switch (decLevel()) {
			case 1:
				if (!"model".equals(pLocalName)) {
					throw error("Expected /model element at level 0, got /" + pLocalName);
				}
				break;
			case 2:
				if (inUsers) {
					if (!"users".equals(pLocalName)) {
						throw error("Expected /users element at level 1, got /" + pLocalName);
					}
					inUsers = false;
				} else {
					throw error("Unexpected element at level 1: /" + pLocalName);
				}
				break;
			case 3:
				if (inUsers) {
					if (!"user".equals(pLocalName)) {
						throw error("Expected /user element at level 2, got /" + pLocalName);
					}
				}
				break;
			}
		}
	}

	public void read(InputStream pIn, IBesModel pModel) {
		Sax.parse(pIn, new XmlModelHandler(pModel));
	}
}
