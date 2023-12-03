package io.kyligence.benchmark.utils;

import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import io.kyligence.benchmark.HttpRequestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {

    private static final int MAX_TOTAL = 600;
    private static final int MAX_PER_ROUTE = 300;
    private static final int CONNECT_TIMEOUT = 6000;
    private static final int SOCKET_TIMEOUT = 60000;

    // 连接池管理器
    private static PoolingHttpClientConnectionManager connectionManager = null;

    // 连接实例
    private static CloseableHttpClient httpClient;

    static {
        try {
            log.info("httpClient连接池初始化，连接池大小：{}，每个路由最大连接数：{}", MAX_TOTAL, MAX_PER_ROUTE);
            //跳过SSL证书认证策略
            SSLConnectionSocketFactory sslFactory = createMySSLConnectionSocketFactory();
            // 配置同时支持 HTTP 和 HTTPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslFactory)
                    .build();
            // 创建连接池管理器对象
            connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // 最大连接数
            connectionManager.setMaxTotal(MAX_TOTAL);
            // 每个路由最大连接数
            connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
            // 初始化httpClient
            httpClient = createHttpClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建HttpClient对象
     *
     * @return CloseableHttpClient
     */
    private static CloseableHttpClient createHttpClient() {
        if (httpClient == null) {
            log.info("初始化httpClient, 连接服务器超时时间:{}, 获取数据的超时时间:{}", CONNECT_TIMEOUT, SOCKET_TIMEOUT);
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT) // 设置服务器超时时间
                    .setSocketTimeout(SOCKET_TIMEOUT) // 设定获取数据的超时时间
                    .build();

            httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig)
                    .setConnectionManager(connectionManager).setConnectionManagerShared(true)
                    //                .evictExpiredConnections()
                    .build();
        }
        return httpClient;
    }

    // 设置header
    private static <T extends HttpRequestBase> void setFullHeaders(T httpRequest, Map<String, String> addHeaders) {
        httpRequest.setHeader("Content-Type", "application/json");
        if (addHeaders.size() > 0) {
            for (String key : addHeaders.keySet()) {
                httpRequest.addHeader(key, addHeaders.get(key));
            }
        }
    }

    // Http协议Get请求
    public static String httpsGet(String url, Map<String, String> headers) throws Exception, HttpRequestException {
        CloseableHttpResponse response = null;
        String result = "";
        try {
            httpClient = createHttpClient();
            HttpGet httpGet = new HttpGet(url);
            //设置header
            setFullHeaders(httpGet, headers);
            //发起请求
            response = httpClient.execute(httpGet);
            //处理返回结果
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 300) {
                throw new HttpRequestException(result);
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
            httpGet.releaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpRequestException("httpclient IO error!", "42000");
        } finally {
            close(response);
        }
        return result;
    }

    // Http协议Post请求
    public static String httpsPost(String url, String json, Map<String, String> headers) throws HttpRequestException {
        CloseableHttpResponse response = null;
        String result = "";
        try {
            httpClient = createHttpClient();
            HttpPost httpPost = new HttpPost(url);
            //设置header
            setFullHeaders(httpPost, headers);
            //填充参数
            httpPost.setEntity(new StringEntity(json, "utf-8"));
            //发起请求
            response = httpClient.execute(httpPost);

            log.debug("[http-call]-[{}]-[status:{}]", url, response.getStatusLine().getStatusCode());
            //处理返回结果
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 300) {
                log.error("[http-call]-[{}]-[error]:{}", url, response);
                throw new HttpRequestException(result);
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpRequestException("httpclient IO error!", "42000");
        } finally {
            close(response);
        }
        return result;
    }

    // Http协议Put请求
    public static String httpsPut(String url, String body, Map<String, String> headers) throws HttpRequestException {
        CloseableHttpResponse response = null;
        String result = "";
        try {
            httpClient = createHttpClient();
            HttpPut httpRequest = new HttpPut(url);
            //设置header
            setFullHeaders(httpRequest, headers);
            //填充参数
            httpRequest.setEntity(new StringEntity(body, "utf-8"));
            //发起请求
            response = httpClient.execute(httpRequest);
            //处理返回结果
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 300) {
                throw new HttpRequestException(result);
            }
            //解析实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpRequestException("httpclient IO error!", "42000");
        } finally {
            close(response);
        }
        return result;
    }

    // Http协议Delete请求
    public static String httpsDelete(String url, Map<String, String> headers) throws HttpRequestException {
        CloseableHttpResponse response = null;
        String result = "";
        try {
            httpClient = createHttpClient();
            HttpDelete httpRequest = new HttpDelete(url);
            //设置header
            setFullHeaders(httpRequest, headers);
            //发起请求
            response = httpClient.execute(httpRequest);
            //处理返回结果
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 300) {
                throw new HttpRequestException(result);
            }
            HttpEntity entity = response.getEntity();
            //返回结果可能为空
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpRequestException("httpclient IO error!", "42000");
        } finally {
            close(response);
        }
        return result;
    }

    //创建一个跳过SSL证书认证策略的简单连接
    public static CloseableHttpClient createSSLClientDefault() throws Exception {
        //信任所有
        SSLContext sslcontext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }

    // 跳过SSL证书认证策略
    private static SSLConnectionSocketFactory createMySSLConnectionSocketFactory() throws Exception {
        SSLContext sslcontext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
        return new SSLConnectionSocketFactory(sslcontext);
    }

    /**
     * 关闭CloseableHttpResponse对象
     *
     * @param response CloseableHttpResponse对象
     */
    private static void close(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}