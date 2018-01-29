/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.cloud.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

public class HttpsUtils {
    public static CloseableHttpClient createHttpClient() {
        final SSLConnectionSocketFactory sslsf = getSSLConnectionSocketFactory();
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }

    public static CloseableHttpClient createHttpClient(CredentialsProvider credentialsProvider) {
        final SSLConnectionSocketFactory sslsf = getSSLConnectionSocketFactory();
        return HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCredentialsProvider(credentialsProvider).build();
    }

    public static String readResponseContent(CloseableHttpResponse response) {
        try (InputStream inputStream = response.getEntity().getContent()) {
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Error reading HTTP response", e);
        }

    }

    public static CredentialsProvider createCredentialsProvider(String username, String password) {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        final CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);

        return provider;
    }

    public static SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
        try {
            final SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustAllStrategy());
            return new SSLConnectionSocketFactory(
                    builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            throw new RuntimeException("Error in SSL setup", e);
        }
    }

    public static class TrustAllStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            return true;
        }
    }
}
