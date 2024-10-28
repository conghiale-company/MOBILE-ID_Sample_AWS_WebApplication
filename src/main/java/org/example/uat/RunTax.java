package org.example.uat;

import org.example.tool_tax_code.TaxIdentityCommon;

public class RunTax {
    public static void main(String[] args) throws Exception {
//        String urlLogin="https://id-dev.mobile-id.vn/dtis/v2/e-identity/general/token/get";
//        String urlGetInfo="https://id-dev.mobile-id.vn/dtis/v2/e-identity/utility/info/document/get";
////        String basicToken="Basic TU9CSUxFLUlEX0RFVjotZGRmTG9obDQ0RTdXREFZbDF6S2JFaTRaampBLVhaZEhvcEJpcE14";
//        String basicToken="Basic VkVJRklDQVRJT05fU0VSVklDRV9WMjpNbjBlcWZFaFV3WVhCS0s1cUt6a0pOS0RMcXRMMXVDLUNJSzVBZjBv";
//        int timeout = 1000000;
////        urlGetInfo = "https://tax-dev.mobile-id.vn/tax/v1/search";
////        String accessKey="RCJBAKFHQENC91VXHF8C";
//        String accessKey="K3YQZMIQS7-VR0AI6FQM";
////        String secretKey="-ddfLohl44E7WDAYl1zKbEi4ZjjA-XZdHopBipMx";
//        String secretKey="Mn0eqfEhUwYXBKK5qKzkJNKDLqtL1uC-CIK5Af0o";
//        String region="vn-south-1";
////        String serviceName="dtis-20.10.05";
//        String serviceName="dtis-20.10.05";
////        String xApiKey="G91W_4tkizGha1pBmSs6YCws4jABAmRUxgtkHB_q";
//        String xApiKey="BsOwfHywvwG2X89u6OkskCN0Pz_60ARrd2SUms8H";

        String urlLogin="https://id-dev.mobile-id.vn/dtis/v2/e-identity/general/token/get";
        String basicToken="Basic TU9CSUxFLUlEX0RFVjotZGRmTG9obDQ0RTdXREFZbDF6S2JFaTRaampBLVhaZEhvcEJpcE14";
        int timeout = 120000;
        String accessKey="RCJBAKFHQENC91VXHF8C";
        String secretKey="-ddfLohl44E7WDAYl1zKbEi4ZjjA-XZdHopBipMx";
        String region="vn-south-1";
        String serviceName="dtis-20.10.05";
        String xApiKey="G91W_4tkizGha1pBmSs6YCws4jABAmRUxgtkHB_q";
        TaxIdentityCommon func = new TaxIdentityCommon();
        String jsonResp = func.loginIdentity(urlLogin, "GET", basicToken, timeout, accessKey, secretKey, region, serviceName, xApiKey);
        System.out.println("A: " + jsonResp);
    }
}
