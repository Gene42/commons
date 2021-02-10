/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.web;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.TextUtils;

import com.gene42.commons.utils.exceptions.ServiceException;

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
        HttpPost httpRequest = this.getHttpPost(relativeUrl, new StringEntity(content, type));

        return this.performRequest(httpRequest, "posting", true);
    }

    /**
     * Performs a post request with the given relative url against the base url.
     * @param relativeUrl the relative url of the request
     * @param content the content to send in the request
     * @return the response body
     * @throws ServiceException if any issue occurs during the request
     */
    public String performPostRequest(String relativeUrl, HttpEntity content)
        throws ServiceException
    {
        HttpPost httpRequest = this.getHttpPost(relativeUrl, content);

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
        HttpPut httpRequest = this.getHttpPut(relativeUrl, new StringEntity(content, type));

        return this.performRequest(httpRequest, "putting", true);
    }

    /**
     * Performs a put request with the given relative url against the base url.
     * @param relativeUrl the relative url of the request
     * @param content the content to send in the request
     * @return the response body
     * @throws ServiceException if any issue occurs during the request
     */
    public String performPutRequest(String relativeUrl, HttpEntity content) throws ServiceException
    {
        HttpPut httpRequest = this.getHttpPut(relativeUrl, content);

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
        HttpPatch httpRequest = this.getHttpPatch(relativeUrl, new StringEntity(content, type));

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
        HttpDelete httpRequest = this.getHttpDelete(relativeUrl);

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
        HttpGet httpRequest = this.getHttpGet(relativeUrl);

        return this.performRequest(httpRequest, "getting", false);
    }

    /**
     * Performs the given request.
     * @param request the request to execute on this endpoint's client.
     * @param requestErrorStr the error string to use if request fails
     * @param require200 if set to true this method will throw an exception if anything but a 200 or a 404 is returned
     * @return the body of the response
     * @throws ServiceException if require200 is true and request response is not a 200 or a 404.
     *                          if the body content cannot be read into a string
     */
    public String performRequest(HttpRequestBase request, String requestErrorStr, boolean require200)
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

    /**
     * Returns an {@link HttpPost} request set up with the given content entity and an authentication header
     * using the credentials set up in this object.
     * @param path the full url in string form of the request (use {@link HttpEndpoint#getURL} to generate this url
     *             easily against a relative path
     * @param content the content entity of the request
     * @return the {@link HttpPost} request
     */
    public HttpPost getHttpPost(String path, HttpEntity content)
    {
        HttpPost httpRequest = new HttpPost(this.getRequestURL(path));
        httpRequest.setEntity(content);
        httpRequest.setHeader(content.getContentType());
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    /**
     * Returns an {@link HttpGet} request set up with an authentication header
     * using the credentials set up in this object.
     * @param path the full url in string form of the request (use {@link HttpEndpoint#getURL} to generate this url
     *             easily against a relative path
     * @return the {@link HttpGet} request
     */
    public HttpGet getHttpGet(String path)
    {
        HttpGet httpRequest = new HttpGet(this.getRequestURL(path));
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    /**
     * Returns an {@link HttpPut} request set up with the given content entity and an authentication header
     * using the credentials set up in this object.
     * @param path the full url in string form of the request (use {@link HttpEndpoint#getURL} to generate this url
     *             easily against a relative path
     * @param content the content entity of the request
     * @return the {@link HttpPut} request
     */
    public HttpPut getHttpPut(String path, HttpEntity content)
    {
        HttpPut httpRequest = new HttpPut(this.getRequestURL(path));
        httpRequest.setEntity(content);
        httpRequest.setHeader(content.getContentType());
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    /**
     * Returns an {@link HttpDelete} request set up with an authentication header
     * using the credentials set up in this object.
     * @param path the full url in string form of the request (use {@link HttpEndpoint#getURL} to generate this url
     *             easily against a relative path
     * @return the {@link HttpDelete} request
     */
    public HttpDelete getHttpDelete(String path)
    {
        HttpDelete httpRequest = new HttpDelete(this.getRequestURL(path));
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    /**
     * Returns an {@link HttpPatch} request set up with the given content entity and an authentication header
     * using the credentials set up in this object.
     * @param path the full url in string form of the request (use {@link HttpEndpoint#getURL} to generate this url
     *             easily against a relative path
     * @param content the content entity of the request
     * @return the {@link HttpPatch} request
     */
    public HttpPatch getHttpPatch(String path, HttpEntity content)
    {
        HttpPatch httpRequest = new HttpPatch(this.getRequestURL(path));
        httpRequest.setEntity(content);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    /**
     * Get a full URL starting with the base URL of this endpoint appended with the relative path given.
     * @param path a relative path to use for generating the final URL
     * @return a URL in string form
     */
    public String getURL(String path)
    {
        if (StringUtils.isNotBlank(path)) {
            return this.baseURL + ((StringUtils.startsWith(path, "/")) ? path : "/" + path);
        } else {
            return this.baseURL;
        }
    }

    /**
     * Get a full request URL. If already starting with http, return as is, else call the {@link HttpEndpoint#getURL}
     * method.
     * @param path a path to use for generating the final URL, could be either an absolute url, or a relative path.
     * @return a URL in string form
     */
    private String getRequestURL(String path)
    {
        if (StringUtils.startsWith(path, "http")) {
            return path;
        } else {
            return this.getURL(path);
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
        private final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        private CloseableHttpClient httpClient;
        private HttpHost httpHost;
        private String baseURL;
        private String username;
        private String password;

        private String host;
        private int port;
        private boolean https;
        private boolean verifySSL = true;
        private boolean redirectsEnabled = true;

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

            this.httpHost = HttpHost.create(this.baseURL);// new HttpHost(this.host, this.port);

            this.httpClient = this.createHttpClient();
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

            String auth =
                Base64.getEncoder().encodeToString((this.username + ":" + this.password)
                    .getBytes(StandardCharsets.UTF_8));

            this.authHeader = new BasicHeader("Authorization", "Basic " + auth);
            return this;
        }

        private CloseableHttpClient createHttpClient() {
            HttpClientBuilder builder = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(
                    RequestConfig.custom()
                                 .setAuthenticationEnabled(true)
                                 .setRedirectsEnabled(this.redirectsEnabled)
                                 .setRelativeRedirectsAllowed(this.redirectsEnabled)
                                 .build())
                .setDefaultCredentialsProvider(this.credentialsProvider);


            if (!this.verifySSL) {
                builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

                    sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs,
                            String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs,
                            String authType) {
                        }
                    } }, new SecureRandom());

                    HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                        RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", new SSLConnectionSocketFactory(
                                sslContext,
                                split(System.getProperty("https.protocols")),
                                split(System.getProperty("https.cipherSuites")),
                                NoopHostnameVerifier.INSTANCE
                            ))
                            .build());

                    builder.setConnectionManager(connectionManager);
                    builder.setSSLContext(sslContext);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    //
                }
            }

            return builder.build();
        }

        private static String[] split(final String s) {
            if (TextUtils.isBlank(s)) {
                return null;
            }
            return s.split(" *, *");
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

        /**
         * Getter for verifySSL.
         *
         * @return verifySSL
         */
        public boolean isVerifySSL() {
            return this.verifySSL;
        }

        /**
         * Setter for verifySSL.
         *
         * @param verifySSL verifySSL to set
         * @return this object
         */
        public Builder setVerifySSL(boolean verifySSL) {
            this.verifySSL = verifySSL;
            return this;
        }

        /**
         * Getter for redirectsEnabled.
         *
         * @return redirectsEnabled
         */
        public boolean isRedirectsEnabled() {
            return this.redirectsEnabled;
        }

        /**
         * Setter for redirectsEnabled.
         *
         * @param redirectsEnabled redirectsEnabled to set
         * @return this object
         */
        public Builder setRedirectsEnabled(boolean redirectsEnabled) {
            this.redirectsEnabled = redirectsEnabled;
            return this;
        }

        private void disableVerifySSL() {

        }
    }
}
