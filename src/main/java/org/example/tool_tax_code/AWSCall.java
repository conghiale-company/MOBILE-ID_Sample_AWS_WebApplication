package org.example.tool_tax_code;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import vn.mobileid.aws.client.v2.AWSV4Auth;
import vn.mobileid.aws.client.v2.AWSV4Constants;

/**
 * 2021/10/06
 *
 * @author TuoiCm
 */
public class AWSCall {
    private Logger LOG = Logger.getLogger(AWSCall.class);

    final protected static String FILE_DIRECTORY_PDF = "file/";
    private URL baseUrl;
    private String httpMethod;
    private String accessKey;
    private String secretKey;
    private String regionName;
    private String serviceName;
    private int timeOut;
    private String xApiKey;
    private String contentType;
    public String sessionToken;
    public String bearerToken;
    private TreeMap<String, String> awsHeaders;
    private AWSV4Auth.Builder builder;

    public AWSCall(String baseUrl,
                   String httpMethod,
                   String accessKey,
                   String secretKey,
                   String regionName,
                   String serviceName,
                   int timeOut,
                   String xApiKey,
                   String contentType,
                   TreeMap<String, String> queryParametes) throws MalformedURLException {
        try {
            this.httpMethod = httpMethod;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.regionName = regionName;
            this.serviceName = serviceName;
            this.timeOut = timeOut;
            this.xApiKey = xApiKey;
            this.contentType = contentType;

            this.baseUrl = new URL(baseUrl);

            this.awsHeaders = new TreeMap<>();
            awsHeaders.put(AWSV4Constants.X_API_KEY, this.xApiKey);
            awsHeaders.put(AWSV4Constants.CONTENT_TYPE, this.contentType);

            LOG.debug("AWSV4Auth.Builder...START");
            this.builder = new AWSV4Auth.Builder(accessKey, secretKey)
                    .regionName(regionName)
                    .serviceName(serviceName)
                    .httpMethodName(httpMethod)
                    .queryParametes(queryParametes)
                    .awsHeaders(awsHeaders);
            LOG.debug("AWSV4Auth.Builder...END");
        } catch (Exception e) {
            LOG.debug("Error in AWSCall constructor: " + e.getMessage());
            e.printStackTrace(); // In thông tin chi tiết về lỗi
        }
    }

    //AWS4Auth
    public Map<String, String> getAWSV4Auth(String payload, String token) throws MalformedURLException {
        this.awsHeaders.put(AWSV4Constants.X_AMZ_SECURITY_TOKEN, token);
        AWSV4Auth aWSV4Auth = this.builder
                .endpointURI(new URL(this.baseUrl.toString()))
                .payload(payload)
                .build();
        return aWSV4Auth.getHeaders();
    }

    public Map<String, String> getAWSV4AuthForFormData(String payload, String token, File file) throws MalformedURLException {
        this.awsHeaders.put(AWSV4Constants.X_AMZ_SECURITY_TOKEN, token);
        this.awsHeaders.put("User-Agent", "PostmanRuntime/7.26.8");
        AWSV4Auth aWSV4Auth = this.builder
                .endpointURI(new URL(this.baseUrl.toString()))
                .payload(payload)
                .addFile("document", file)
                .build();
        return aWSV4Auth.getHeaders();
    }

}
