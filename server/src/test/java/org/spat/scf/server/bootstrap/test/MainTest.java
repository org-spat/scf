package org.spat.scf.server.bootstrap.test;

import org.spat.scf.server.bootstrap.Main;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Main.main(new String[] { "-Dscf.service.name=demo" });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
