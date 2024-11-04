package org.example.tool_tax_code;

import org.apache.log4j.Logger;

import java.net.URL;

public class TaxIdentityCommon {
    private final Logger LOG = Logger.getLogger(TaxIdentityCommon.class);
    public String loginIdentity(String url, String sMethod, String basicToken, int timeout, String accessKey, String secretKey, String region,
                                String serviceName, String xApiKey) throws Exception {
        LOG.debug("url: " + url);
        LOG.debug("sMethod: " + sMethod);
        LOG.debug("basicToken: " + basicToken);
        LOG.debug("timeout: " + timeout);
        LOG.debug("accessKey: " + accessKey);
        LOG.debug("secretKey: " + secretKey);
        LOG.debug("region: " + region);
        LOG.debug("serviceName: " + serviceName);
        LOG.debug("xApiKey: " + xApiKey);

        String payload = null;
        AWSCall awsCall = new AWSCall(url, sMethod, accessKey, secretKey, region, serviceName, timeout, xApiKey, "application/json", null);

        String jsonResp = HttpUtils.invokeHttpRequest(
                new URL(url),
                sMethod,
                timeout,
                awsCall.getAWSV4Auth(payload, basicToken),
                payload);
        return jsonResp;
    }
}
