package org.spat.scf.client;

/**
 * Provides the version information of SCF.
 * 
 * @author Service Platform Architecture Team 
 */
public final class Version {
	/** The version identifier. */
	public static final String ID = SCFConst.VERSION_FLAG;

	/** Prints out the version identifier to stdout. */
	public static void main(String[] args) {
		System.out.println(ID);
	}

	private Version() {
		super();
	}
}
