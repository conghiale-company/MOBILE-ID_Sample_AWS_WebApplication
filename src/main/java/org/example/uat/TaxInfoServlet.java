package org.example.uat;
import org.example.tool_tax_code.Program;
import org.example.tool_tax_code.TaxIdentityCommon;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TaxInfoServlet  extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
//            out.println("<!DOCTYPE html>");
//            out.println("<html>");
//            out.println("<head>");
//            out.println("<title>Servlet CallTaxInfo</title>");
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<h1>Servlet CallTaxInfo at " + request.getContextPath() + "</h1>");
//            out.println("</body>");
//            out.println("</html>");

            System.out.println("Begin servlet");
            String urlLogin1="https://id-dev.mobile-id.vn/dtis/v2/e-identity/general/token/get";
            String basicToken1="Basic TU9CSUxFLUlEX0RFVjotZGRmTG9obDQ0RTdXREFZbDF6S2JFaTRaampBLVhaZEhvcEJpcE14";
            int timeout1 = 120000;
            String accessKey1="RCJBAKFHQENC91VXHF8C";
            String secretKey1="-ddfLohl44E7WDAYl1zKbEi4ZjjA-XZdHopBipMx";
            String region1="vn-south-1";
            String serviceName1="dtis-20.10.05";
            String xApiKey1="G91W_4tkizGha1pBmSs6YCws4jABAmRUxgtkHB_q";

            TaxIdentityCommon func = new TaxIdentityCommon();
            String jsonResp;
            try {
                jsonResp = func.loginIdentity(urlLogin1, "GET", basicToken1, timeout1, accessKey1, secretKey1, region1, serviceName1, xApiKey1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("A: " + jsonResp);

            /*try {
                Program.running(new String[] {"D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Sample_AWS_intelij\\Sample_AWS_WebApplication\\src\\main\\resources\\config_aws_dev.cfg",
                        "D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Sample_AWS_intelij\\Sample_AWS_WebApplication\\src\\main\\resources\\config_send_email.cfg",
                        "D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Sample_AWS_intelij\\Sample_AWS_WebApplication\\src\\main\\resources\\Tax_Code_Test.txt"}, request, response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }*/



        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
