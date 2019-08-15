package com.ibm.cics.bundle.deploy;

/*-
 * #%L
 * CICS Bundle Maven Plugin
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

public class BundleDeployer {
	
	public static void main(String[] args) {
		
		String endpointURL = args[0];
		String bundle = args[1];
		String bunddef = args[2];
		String csdgroup = args[3];
		String cicsplex = args[4];
		String region = args[5];
		String username = args[6];
		String password = args[7];
		
		try {
			URI uri = new URI(endpointURL);
			BundleDeployHelper.deployBundle(uri, new File(bundle), bunddef, csdgroup, cicsplex, region, username, password);
			System.out.println("Bundle deployed");
		} catch (URISyntaxException e) {
			System.out.println("Invalid URL: " + endpointURL);
		} catch (BundleDeployException e) {
			System.out.println("Bundle deploy exception: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO Exception: " + e.getMessage());
		}
		
		
	}
	
}
