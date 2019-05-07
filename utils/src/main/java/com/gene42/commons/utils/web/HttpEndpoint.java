/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.web;

import com.gene42.commons.utils.exceptions.ServiceException;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

/**
 * CloseableHttpClient wrapper. It provides useful authentication.
 *
 * @version $Id$
 */
public final class HttpEndpoint implements Closeable
{
    private final BasicHeader authHeader;
    private final String baseURL;
    private final HttpHost httpHost;

    private final CloseableHttpClient httpClient;

    private HttpEndpoint(Builder builder) {
        this.authHeader = builder.authHeader;
        this.baseURL = builder.baseURL;
        this.httpHost = builder.httpHost;
        this.httpClient = builder.httpClient;
    }

    /**
     * Performs a post request with the given relative url against the base url.
     * @param relativeUrl the relative url of the request
     * @param content the content to send in the request
     * @param type the type of the content
     * @return the response body
     * @throws ServiceException if any issue occurs during the request
     */
    public String performPostRequest(String relativeUrl, String content, ContentType type)
        throws ServiceException
    {
        HttpPost httpRequest = this.getHttpPost(this.getURL(relativeUrl), new StringEntity(content, type));

        return this.performRequest(httpRequest, "posting", true);
    }

    /**
     * Performs a put request with the given relative url against the base url.
     * @param relativeUrl the relative url of the request
     * @param content the content to send in the request
     * @param type the type of the content
     * @return the response body
     * @throws ServiceException if any issue occurs during the request
     */
    public String performPutRequest(String relativeUrl, String content, ContentType type)
        throws ServiceException
    {
        HttpPut httpRequest = this.getHttpPut(this.getURL(relativeUrl), new StringEntity(content, type));

        return this.performRequest(httpRequest, "putting", true);
    }

    /**
     * Performs a patch request with the given relative url against the base url.
     * @param relativeUrl the relative url of the request
     * @param content the content to send in the request
     * @param type the type of the content
     * @return the response body
     * @throws ServiceException if any issue occurs during the request
     */
    public String performPatchRequest(String relativeUrl, String content, ContentType type)
        throws ServiceException
    {
        HttpPatch httpRequest = this.getHttpPatch(this.getURL(relativeUrl), new StringEntity(content, type));

        return this.performRequest(httpRequest, "patching", true);
    }

    /**
     * Performs a delete request with the given relative url against the base url.
     * @param relativeUrl the relative url of the request
     * @return the response body
     * @throws ServiceException if any issue occurs during the request
     */
    public String performDeleteRequest(String relativeUrl)
        throws ServiceException
    {
        HttpDelete httpRequest = this.getHttpDelete(this.getURL(relativeUrl));

        return this.performRequest(httpRequest, "deleting", true);
    }

    /**
     * Performs a get request with the given relative url against the base url.
     * @param relativeUrl the relative url of the request
     * @return the response body
     * @throws ServiceException if any issue occurs during the request
     */
    public String performGetRequest(String relativeUrl) throws ServiceException
    {
        HttpGet httpRequest = this.getHttpGet(this.getURL(relativeUrl));

        return this.performRequest(httpRequest, "getting", false);
    }

    private String performRequest(HttpRequestBase request, String requestErrorStr, boolean require200)
        throws ServiceException
    {
        try (CloseableHttpResponse response = this.httpClient.execute(this.httpHost, request)) {

            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == 404) {
                return null;
            } else if (responseCode >= 400 || (require200 && (responseCode >= 300))) {
                throw new ServiceException(String.format("Error occurred while %s resource [%s][%s]", requestErrorStr,
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity == null) {
                return "";
            } else {
                return IOUtils.toString(responseEntity.getContent(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    private HttpPost getHttpPost(String path, HttpEntity content)
    {
        HttpPost httpRequest = new HttpPost(path);
        httpRequest.setEntity(content);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    private HttpGet getHttpGet(String path)
    {
        HttpGet httpRequest = new HttpGet(path);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    private HttpPut getHttpPut(String path, HttpEntity content)
    {
        HttpPut httpRequest = new HttpPut(path);
        httpRequest.setEntity(content);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    private HttpDelete getHttpDelete(String path)
    {
        HttpDelete httpRequest = new HttpDelete(path);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    private HttpPatch getHttpPatch(String path, HttpEntity content)
    {
        HttpPatch httpRequest = new HttpPatch(path);
        httpRequest.setEntity(content);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    private String getURL(String path)
    {
        if (StringUtils.isNotBlank(path)) {
            return this.baseURL + ((StringUtils.startsWith(path, "/")) ? path : "/" + path);
        } else {
            return this.baseURL;
        }
    }

    /**
     * Get new builder object.
     * @return a Builder
     */
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Getter for authHeader.
     *
     * @return authHeader
     */
    public BasicHeader getAuthHeader()
    {
        return this.authHeader;
    }

    /**
     * Getter for baseURL.
     *
     * @return baseURL
     */
    public String getBaseURL()
    {
        return this.baseURL;
    }

    /**
     * Getter for httpHost.
     *
     * @return httpHost
     */
    public HttpHost getHttpHost() {
        return this.httpHost;
    }

    @Override
    public void close() throws IOException {
        if (this.httpClient != null) {
            this.httpClient.close();
        }
    }

    /**
     * Builder class.
     */
    public static class Builder
    {
        private static final String HTTP = "http://";
        private static final String HTTPS = "https://";

        private BasicHeader authHeader;
        private BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        private CloseableHttpClient httpClient;
        private HttpHost httpHost;
        private String baseURL;
        private String username;
        private String password;

        private String host;
        private int port;
        private boolean https;

        /**
         * Build a new a HttpEndpoint.
         * @return a new HttpEndpoint instance filled with the options set up
         */
        public HttpEndpoint build() {

            this.baseURL = this.host;

            if (this.https) {
                if (!StringUtils.startsWith(this.baseURL, HTTPS)) {
                    if (StringUtils.startsWith(this.baseURL, HTTP)) {
                        this.baseURL = StringUtils.removeStart(this.baseURL, HTTP);
                    }
                    this.baseURL = HTTPS + this.baseURL;
                }
            } else {
                if (!StringUtils.startsWith(this.baseURL, HTTP)) {
                    this.baseURL = HTTP + this.baseURL;
                }
            }

            if (this.port > 0) {
                this.baseURL = this.baseURL + ":" + this.port;
            }

            this.httpHost = new HttpHost(this.host, this.port);

            this.httpClient = this.getHttpClient(true);
            return new HttpEndpoint(this);
        }

        /**
         * Setter for host.
         * @param host the host name
         * @return this object
         */
        public Builder setHost(String host) {
            return this.setHostAndPort(host, -1);
        }

        /**
         * Setter for host and port.
         * @param host the host name
         * @param port the port
         * @return this object
         */
        public Builder setHostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
            return this;
        }

        /**
         * Setter for https.
         *
         * @param https https to set
         * @return this object
         */
        public Builder setHttps(boolean https) {
            this.https = https;
            return this;
        }

        /**
         * Sets the authentication parameters.
         * @param username the user name
         * @param password the password
         * @return this object
         */
        public Builder setUsernameAndPassword(String username, String password) {
            this.username = username;
            this.password = password;

            this.credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(this.username, this.password));

            String auth = Base64.getEncoder().encodeToString((this.username + ":" + this.password).getBytes());

            this.authHeader = new BasicHeader("Authorization", "Basic " + auth);
            return this;
        }

        private CloseableHttpClient getHttpClient(boolean redirectsEnabled) {
            return HttpClientBuilder
                .create()
                .setDefaultRequestConfig(
                    RequestConfig.custom()
                                 .setAuthenticationEnabled(true)
                                 .setRedirectsEnabled(redirectsEnabled)
                                 .setRelativeRedirectsAllowed(redirectsEnabled)
                                 .build())
                .setDefaultCredentialsProvider(this.credentialsProvider)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        }

        /**
         * Getter for authHeader.
         *
         * @return authHeader
         */
        public BasicHeader getAuthHeader() {
            return this.authHeader;
        }

        /**
         * Getter for credentialsProvider.
         *
         * @return credentialsProvider
         */
        public BasicCredentialsProvider getCredentialsProvider() {
            return this.credentialsProvider;
        }

        /**
         * Getter for httpHost.
         *
         * @return httpHost
         */
        public HttpHost getHttpHost() {
            return this.httpHost;
        }

        /**
         * Getter for baseURL.
         *
         * @return baseURL
         */
        public String getBaseURL() {
            return this.baseURL;
        }

        /**
         * Getter for username.
         *
         * @return username
         */
        public String getUsername() {
            return this.username;
        }

        /**
         * Getter for password.
         *
         * @return password
         */
        public String getPassword() {
            return this.password;
        }

        /**
         * Getter for httpClient.
         *
         * @return httpClient
         */
        public CloseableHttpClient getHttpClient() {
            return this.httpClient;
        }

        /**
         * Getter for https.
         *
         * @return https
         */
        public boolean isHttps() {
            return this.https;
        }
    }
}
