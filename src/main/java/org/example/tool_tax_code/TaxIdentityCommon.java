package org.example.tool_tax_code;

import java.net.URL;

public class TaxIdentityCommon {
    public String loginIdentity(String url, String sMethod, String basicToken, int timeout, String accessKey, String secretKey, String region,
                                String serviceName, String xApiKey) throws Exception {
        System.out.println("url: " + url);
        System.out.println("sMethod: " + sMethod);
        System.out.println("basicToken: " + basicToken);
        System.out.println("accessKey: " + accessKey);
        System.out.println("secretKey: " + secretKey);
        System.out.println("region: " + region);

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
