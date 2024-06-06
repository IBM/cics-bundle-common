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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BundleDeployHelper {

	private static final String AUTHORIZATION_HEADER = "Authorization";

	public static void deployBundle(URI endpointURL, File bundle, String bunddef, String csdgroup, String cicsplex, String region, String username, char[] password, boolean allowSelfSignedCertificate) throws BundleDeployException, IOException {
		MultipartEntityBuilder mpeb = MultipartEntityBuilder.create();
		mpeb.addPart("bundle", new FileBody(bundle, ContentType.create("application/zip")));
		mpeb.addPart("bunddef", new StringBody(bunddef, ContentType.TEXT_PLAIN));
		mpeb.addPart("csdgroup", new StringBody(csdgroup, ContentType.TEXT_PLAIN));
		if (cicsplex != null && !cicsplex.isEmpty()) {
			mpeb.addPart("cicsplex", new StringBody(cicsplex, ContentType.TEXT_PLAIN));
		}
		if (region != null && !region.isEmpty()) {
			mpeb.addPart("region", new StringBody(region, ContentType.TEXT_PLAIN));
		}
		
		
		String path = endpointURL.getPath();
		if (path == null) {
			path = "";
		} else if (!path.endsWith("/")) {
			path = path + "/";
		}
		
		path = path + "managedcicsbundles";
		
		URI target;
		try {
			target = new URI(
				endpointURL.getScheme(),
				endpointURL.getUserInfo(),
				endpointURL.getHost(),
				endpointURL.getPort(),
				path,
				endpointURL.getQuery(),
				endpointURL.getFragment()
			);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		
		HttpPost httpPost = new HttpPost(target);
		HttpEntity httpEntity = mpeb.build();
		httpPost.setEntity(httpEntity);
		
		CloseableHttpClient httpClient;
		if (!allowSelfSignedCertificate) {
			httpClient = HttpClientBuilder.create().useSystemProperties().build();
		} else {
			try {
                SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(new TrustAllStrategy());
                SSLConnectionSocketFactoryBuilder sslSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(sslContextBuilder.build())
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                
                PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactoryBuilder.build());

                httpClient = HttpClients.custom()
                        .setConnectionManager(connectionManagerBuilder.build())
                        .useSystemProperties()
                        .build();
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
				throw new BundleDeployException("Error instantiating secure connection", e);
			}
		}
		
		String stringPassword = (password == null || password.length == 0) ? null : String.valueOf(password);
		String credentials = username + ":" + stringPassword;
		String encoding = Base64.getEncoder().encodeToString(credentials.getBytes("ISO-8859-1"));
		httpPost.setHeader(AUTHORIZATION_HEADER, "Basic " + encoding);
		
		if (!bundle.exists()) {
			throw new BundleDeployException("Bundle does not exist: '" + bundle + "'");
		}
		
		
		
		CloseableHttpResponse response = httpClient.execute(httpPost);
		int responseStatusCode = response.getCode();
		Header[] contentTypeHeaders = response.getHeaders("Content-Type");
		String contentType;
		if (contentTypeHeaders.length != 1) {
			contentType = null;
		} else {
			contentType = contentTypeHeaders[0].getValue();
		}

		if (responseStatusCode != 200) {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			try {
				String responseContent = bufferedReader.lines().collect(Collectors.joining());
				if (contentType == null) {
					throw new BundleDeployException("Http response: " + responseStatusCode);
				} else if (contentType.equals("application/xml")) {
					//liberty level error
					throw new BundleDeployException(responseContent);
				} else if (contentType.equals("application/json")) {
					//error from deploy endpoint
					ObjectMapper objectMapper = new ObjectMapper();
					String responseMessage = objectMapper.readTree(responseContent).get("message").asText();
					String responseErrors = "";
					
					if (responseMessage.contains("Some of the supplied parameters were invalid")) {
						Iterator<Entry<String, JsonNode>> errorFields = objectMapper.readTree(responseContent).get("requestErrors").fields();
						StringBuffer sb = new StringBuffer();
						while (errorFields.hasNext()) {
							Entry<String, JsonNode> errorField = errorFields.next();
							sb.append(errorField.getKey());
							sb.append(": ");
							sb.append(errorField.getValue().asText());
							sb.append('\n');
						}
						responseErrors = sb.toString();
					} else if (responseMessage.contains("Bundle deployment failure")) {
						responseErrors = objectMapper.readTree(responseContent).get("deployments").findValue("message").asText();
					}
					throw new BundleDeployException(responseMessage + ":\n - " + responseErrors);
				} else {
					//CICS level error
					throw new BundleDeployException(responseContent);
				}
			} finally {
				bufferedReader.close();
			}
		}
	}

}
