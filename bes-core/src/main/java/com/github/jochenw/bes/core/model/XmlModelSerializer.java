package com.github.jochenw.bes.core.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
					   final List<BesUser> users = new ArrayList<>();
					   pModel.getUserController().readAll(users::add);
					   users.sort((bu1,bu2) -> {
						   return Long.compare(bu1.getId().longValue(), bu2.getId().longValue());
					   });
					   users.forEach((u) -> {
						   sw2.writeElement("user", (String) null, "id", u.getId().getLongValue(),
								                                   "userId", u.getUserId(),
								                                   "email", u.getEmail(),
								                                   "name", u.getName());
					   });
				   });
				   sw1.writeElement("jobs", (sw2) -> {
					   final List<BesJob> jobs = new ArrayList<>();
					   pModel.getJobController().readAll(jobs::add);
					   jobs.sort((bj1, bj2) -> bj1.getId().getLongValue().compareTo(bj2.getId().getLongValue()));
					   jobs.forEach((j) -> {
						   sw2.writeElement("job", (String) null, "id", j.getId().getLongValue(),
								                                  "owner", j.getOwner().getLongValue(),
								                                  "name", j.getName());
					   });
				   });
			   });
		   });
	}

	public static class XmlModelHandler extends AbstractContentHandler {
		private final IBesModel model;
		private final Map<Long,BesUser> userIds = new HashMap<>();
		private final Map<Long,BesJob> jobIds = new HashMap<>();
		private final Set<String> userUIds = new HashSet<>();
		private final Set<String> userEmails = new HashSet<>();
		private boolean inUsers, inJobs;
		
		public XmlModelHandler(IBesModel pModel) {
			model = pModel;
		}

		@Override
		public void startDocument() throws SAXException {
			System.out.println("startDocument:");
		}

		@Override
		public void endDocument() throws SAXException {
			System.out.println("endDocument:");
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
				if (inJobs) {
					throw error("Unexpected " + pLocalName + " element, while state=inJobs)");
				}
				if (inUsers) {
					throw error("Unexpected " + pLocalName + " element, while state=inUsers)");
				}
				if ("users".equals(pLocalName)) {
					inUsers = true;
				} else if ("jobs".equals(pLocalName)) {
					inJobs = true;
				} else {
					throw error("Expected jobs|users element at level 1, got " + pLocalName);
				} 
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
				} else if (inJobs) {
					System.out.println("Job: id="
							+ pAtts.getValue("id") + ", owner=" + pAtts.getValue("owner")
							+ ", name=" + pAtts.getValue("name") + ", this=" + this);
					if (!"job".equals(pLocalName)) {
						throw error("Expected job element at level 2, got " + pLocalName);
					}
					final String idStr = pAtts.getValue("id");
					if (idStr == null) {
						throw error("Missing attribute job/@id");
					}
					if (idStr.length() == 0) {
						throw error("Empty attribute job/@id");
					}
					final Long l;
					try {
						l = Long.valueOf(idStr);
					} catch (NumberFormatException nfe) {
						throw error("Invalid attribute job/@id: Expected long number value, got " + idStr);
					}
					if (jobIds.containsKey(l)) {
						throw error("Duplicate value for attribute job/@id: " + idStr);
					}
					final String ownerIdStr = pAtts.getValue("owner");
					if (ownerIdStr == null) {
						throw error("Missing attribute job/@owner");
					}
					if (ownerIdStr.length() == 0) {
						throw error("Empty attribute job/@owner");
					}
					final Long ol;
					try {
						ol = Long.valueOf(ownerIdStr);
					} catch (NumberFormatException nfe) {
						throw error("Invalid attribute job/@owner: Expected long number value, got " + ownerIdStr);
					}
					final BesUser.Id ownerId = new BesUser.Id(ol);
					final BesUser owner = userIds.get(ol);
					if (owner == null) {
						throw error("Invalid attribute job/@owner: No user found with id " + ol);
					}
					final String name = pAtts.getValue("name");
					if (name == null) {
						throw error("Missing attribute job/@name");
					}
					if (name.length() == 0) {
						throw error("Empty attribute job/@name");
					}
					if (model.getJobController().getJob(owner.getId(), name) != null) {
						throw error("Duplicate job name for owner id " + ownerId + ": " + name);
					}
					final BesJob bj = new BesJob(null);
					bj.setOwner(owner.getId());
					bj.setName(name);
					final BesJob newBj = model.getJobController().insert(bj);
					jobIds.put(ol, newBj);
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
					throw error("Expected /model element at level 1, got /" + pLocalName);
				}
				break;
			case 2:
				if (inUsers) {
					if (!"users".equals(pLocalName)) {
						throw error("Expected /users element at level 2, got /" + pLocalName);
					}
					inUsers = false;
				} else if (inJobs) {
					if (!"jobs".equals(pLocalName)) {
						throw error("Expected /jobs element at level 2, got /" + pLocalName);
					}
					inUsers = false;
				} else {
					throw error("Unexpected element at level 2: /" + pLocalName);
				}
				break;
			case 3:
				if (inUsers) {
					if (!"user".equals(pLocalName)) {
						throw error("Expected /user element at level 3, got /" + pLocalName);
					}
				} else if (inJobs) {
					if (!"job".equals(pLocalName)) {
						throw error("Expected /job element at level 3, got /" + pLocalName);
					}
				} else {
					throw error("Expected /job element, or /user element at level 3, got /" + pLocalName);
				}
				break;
			}
		}
	}

	public void read(InputStream pIn, IBesModel pModel) {
		Sax.parse(pIn, new XmlModelHandler(pModel));
	}
}
