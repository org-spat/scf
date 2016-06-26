package org.spat.scf.serializer.test.entity;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

@SuppressWarnings("serial")
@SCFSerializable
public class SESUser extends User {

	@SCFMember
	private Enterprise enterprise;

	public void setEnterprise(Enterprise enterprise) {
		this.enterprise = enterprise;
	}

	public Enterprise getEnterprise() {
		return enterprise;
	}
	
}
