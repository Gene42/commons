package com.gene42.commons.utils.web;

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
import org.apache.http.client.methods.HttpGet;
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

import com.gene42.commons.utils.exceptions.ServiceException;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public final class HttpEndpoint implements Closeable {

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

    public String performPostRequest(String relativeUrl, String content, ContentType type)
        throws ServiceException {
        HttpPost httpRequest = this.getHttpPost(this.getURL(relativeUrl), new StringEntity(content, type));

        return this.performRequest(httpRequest, "posting", true);

       /* try (CloseableHttpResponse response = this.httpClient.execute(this.httpHost, httpRequest)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new ServiceException(String.format("Error occurred while performing post request [%s][%s]",
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
        }*/
    }

    public String performPutRequest(String relativeUrl, String content, ContentType type)
        throws ServiceException {
        HttpPut httpRequest = this.getHttpPut(this.getURL(relativeUrl), new StringEntity(content, type));

        return this.performRequest(httpRequest, "putting", true);
        /*try (CloseableHttpResponse response = this.httpClient.execute(this.httpHost, httpRequest)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new ServiceException(String.format("Error occurred while performing post request [%s][%s]",
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
        }*/
    }

    public String performGetRequest(String relativeUrl) throws ServiceException {
        HttpGet httpRequest = this.getHttpGet(this.getURL(relativeUrl));

        return this.performRequest(httpRequest, "getting", false);
       /* try (CloseableHttpResponse response = this.httpClient.execute(this.httpHost, httpRequest)) {
            if (response.getStatusLine().getStatusCode() == 404) {
                return null;
            } else if (response.getStatusLine().getStatusCode() >= 400) {
                throw new ServiceException(String.format("Error occurred while getting resource [%s][%s]",
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }
            return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ServiceException(e);
        }*/
    }

    private String performRequest(HttpRequestBase request, String requestErrorStr, boolean require200)
        throws ServiceException {
        try (CloseableHttpResponse response = this.httpClient.execute(this.httpHost, request)) {

            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == 404) {
                return null;
            } else if (responseCode >= 400 || (require200 && response.getStatusLine().getStatusCode() != 200)) {
                throw new ServiceException(String.format("Error occurred while %s resource [%s][%s]", requestErrorStr,
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }

            /*if (require200 && response.getStatusLine().getStatusCode() != 200) {
                throw new ServiceException(String.format("Error occurred while performing post request [%s][%s]",
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }*/

            System.out.println("hmmmmm=" + responseCode + ", msg=" + response.getStatusLine().getReasonPhrase());

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

    private HttpPost getHttpPost(String path, HttpEntity content) {
        HttpPost httpRequest = new HttpPost(path);
        httpRequest.setEntity(content);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    private HttpGet getHttpGet(String path) {
        HttpGet httpRequest = new HttpGet(path);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    private HttpPut getHttpPut(String path, HttpEntity content) {
        HttpPut httpRequest = new HttpPut(path);
        httpRequest.setEntity(content);
        httpRequest.addHeader(this.authHeader);
        return httpRequest;
    }

    private String getURL(String path) {
        if (StringUtils.isNotBlank(path)) {
            return this.baseURL + ((StringUtils.startsWith(path,"/")) ? path : "/" + path);
        } else {
            return this.baseURL;
        }
    }

    /**
     * Get new builder object.
     * @return a Builder
     */
    public static Builder builder() {
        return new Builder();
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
     * Getter for baseURL.
     *
     * @return baseURL
     */
    public String getBaseURL() {
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
    public static class Builder {
        private BasicHeader authHeader;
        private BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        private CloseableHttpClient httpClient;
        private HttpHost httpHost;
        private String baseURL;
        private String username;
        private String password;

        public HttpEndpoint build() {
            this.httpClient = this.getHttpClient(true);
            return new HttpEndpoint(this);
        }

        public Builder setHost(String host) {
            return this.setHostAndPort(host, -1);
        }

        public Builder setHostAndPort(String host, int port) {

            this.baseURL = host;

            // TODO: handle https
            if (!StringUtils.startsWith(this.baseURL, "http")) {
                this.baseURL = "http://" + this.baseURL;
            }

            if (port > 0) {
                this.baseURL = this.baseURL + ":" + port;
            }

            this.httpHost = new HttpHost(host, port);
            return this;
        }

        public Builder setUsernameAndPassword(String username, String password) {
            this.username = username;
            this.password = password;

            this.credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(this.username, this.password));

            String auth = new String(Base64.getEncoder().encode((this.username + ":" + this.password)
                .getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

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
    }
}
