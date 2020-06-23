package com.ibm.cics.bundle.deploy;

/*-
 * #%L
 * CICS Bundle Common Parent
 * %%
 * Copyright (C) 2019 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.ibm.websphere.crypto.PasswordUtil;

public class BundleDeployer {
	
	public static void main(String[] args) {
		
		String endpointURL = args[0];
		String bundle = args[1];
		String bunddef = args[2];
		String csdgroup = args[3];
		String cicsplex = args[4];
		String region = args[5];
		String username = args[6];
		char[] password = getPasswordAsChars(args[7]);
		boolean allowSelfSignedCertificate = Boolean.parseBoolean(args[8]);
		
		try {
			URI uri = new URI(endpointURL);
			BundleDeployHelper.deployBundle(uri, new File(bundle), bunddef, csdgroup, cicsplex, region, username, password, allowSelfSignedCertificate);
			System.out.println("Bundle deployed");
		} catch (URISyntaxException e) {
			System.err.println("Invalid URL: " + endpointURL);
		} catch (BundleDeployException e) {
			System.err.println("Bundle deploy exception: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IO Exception: " + e.getMessage());
		}
		
	}
	
	/*
	 * Current best practice is to avoid putting strings containing passwords onto
	 *  the heap or interning the strings containing them, hence using char[] where possible
	 */
	public static char[] getPasswordAsChars(String encryptedPassword) {
		char[] password = new char[0];
		
		if (encryptedPassword != null && !encryptedPassword.isEmpty()) {
			if (isEncrypted(encryptedPassword)) {
				try {
					password = PasswordUtil.passwordDecode(encryptedPassword).toCharArray();
				} catch (Exception e) {
					System.err.println("Failed to decode encrypted password: " + e.getMessage());
				}
			} else {
				password = encryptedPassword.toCharArray();
			}
		}
		return password;
	}
	
	public static boolean isEncrypted(String password) {
		return '{' == password.charAt(0) && password.contains("}");
	}
	
}
