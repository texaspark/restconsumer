package com.oracle.oci.restconsumer.controller;

import com.oracle.oci.restconsumer.OCIRest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    // The following paramters are mandatory
    // Tenancy OCID
    String m_tenancyOcId = "ocid1.tenancy.oc1..aaaaaaaaholwofp6p5bu4ycy2cnsp4gfx2gibwlnujwj4z4a63jlpflhuqva";
    // User OCID
    String m_userOcid = "ocid1.user.oc1..aaaaaaaaak3dche5w5igobvqxv5sa32p2cdn5lqgbxl2lzre5dcq35efc5la";
    // FingerPrint
    String m_fingerPrint = "47:2b:7c:f2:ae:ef:fb:fe:40:9b:e0:0d:bf:a0:0e:30";
    // Complete path to the private key file
    String m_privateKeyFilename = "C:\\Users\\seokpark\\.oci\\oci_api_key.pem";
    // Private key path in Unix
    //private static String p_privateKeyFilename = "/home/oracle/oci_api_key.pem";

    @RequestMapping(value="/v1/instance")
    public String compute() throws Exception{

        String uri = "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances?compartmentId=ocid1.compartment.oc1..aaaaaaaaddq4ttd4urh27c3lr54cuwmtcfo3powi7fsijrbse5p3xfv2ci6a";

        OCIRest ociObj = new OCIRest();
        return ociObj.getAPIResponse(m_tenancyOcId,m_userOcid,m_fingerPrint,m_privateKeyFilename,uri);
    }
}
