package org.example.tool_tax_code;

public class DemoTax {
    public static void runTaxCode(String[] args) throws Exception {
        String urlLogin=args[0];
        String basicToken=args[1];
        int timeout = Integer.parseInt(args[2]);
        String accessKey=args[3];
        String secretKey=args[4];
        String region=args[5];
        String serviceName=args[6];
        String xApiKey=args[7];
        TaxIdentityCommon func = new TaxIdentityCommon();

        System.out.println("urlLogin: " + urlLogin.equalsIgnoreCase("https://id-dev.mobile-id.vn/dtis/v2/e-identity/general/token/get"));
        System.out.println("basicToken: " + basicToken.equalsIgnoreCase("Basic TU9CSUxFLUlEX0RFVjotZGRmTG9obDQ0RTdXREFZbDF6S2JFaTRaampBLVhaZEhvcEJpcE14"));
        System.out.println("timeout: " + (timeout == 120000));
        System.out.println("accessKey: " + accessKey.equalsIgnoreCase("RCJBAKFHQENC91VXHF8C"));
        System.out.println("secretKey: " + secretKey.equalsIgnoreCase("-ddfLohl44E7WDAYl1zKbEi4ZjjA-XZdHopBipMx"));
        System.out.println("region: " + region.equalsIgnoreCase("vn-south-1"));
        System.out.println("serviceName: " + serviceName.equalsIgnoreCase("dtis-20.10.05"));
        System.out.println("xApiKey: " + xApiKey.equalsIgnoreCase("G91W_4tkizGha1pBmSs6YCws4jABAmRUxgtkHB_q"));

        String urlLogin1="https://id-dev.mobile-id.vn/dtis/v2/e-identity/general/token/get";
        String basicToken1="Basic TU9CSUxFLUlEX0RFVjotZGRmTG9obDQ0RTdXREFZbDF6S2JFaTRaampBLVhaZEhvcEJpcE14";
        int timeout1 = 120000;
        String accessKey1="RCJBAKFHQENC91VXHF8C";
        String secretKey1="-ddfLohl44E7WDAYl1zKbEi4ZjjA-XZdHopBipMx";
        String region1="vn-south-1";
        String serviceName1="dtis-20.10.05";
        String xApiKey1="G91W_4tkizGha1pBmSs6YCws4jABAmRUxgtkHB_q";
        String jsonResp = func.loginIdentity(urlLogin1, "GET", basicToken1, timeout1, accessKey1, secretKey1, region1, serviceName1, xApiKey1);
        System.out.println("A: " + jsonResp);
    }
}
