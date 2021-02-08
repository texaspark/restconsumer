package com.oracle.oci.restconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.tomitribe.auth.signatures.MissingRequiredHeaderException;
import org.tomitribe.auth.signatures.PEM;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@SpringBootApplication
public class OCIRest {
/*
    // The following paramters are mandatory
    // Tenancy OCID
    private static String m_tenancyOcId = "ocid1.tenancy.oc1..aaaaaaaaholwofp6p5bu4ycy2cnsp4gfx2gibwlnujwj4z4a63jlpflhuqva";
    // User OCID
    private static String m_userOcid = "ocid1.user.oc1..aaaaaaaaak3dche5w5igobvqxv5sa32p2cdn5lqgbxl2lzre5dcq35efc5la";
    // FingerPrint
    private static String m_fingerPrint = "47:2b:7c:f2:ae:ef:fb:fe:40:9b:e0:0d:bf:a0:0e:30";
    // Complete path to the private key file
    private static String m_privateKeyFilename = "C:\\Users\\seokpark\\.oci\\oci_api_key.pem";
    // Private key path in Unix
    //private static String p_privateKeyFilename = "/home/oracle/oci_api_key.pem";
*/
    public static void main(String[] args) {
        SpringApplication.run(OCIRest.class, args);
/*
        String uri = "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances?compartmentId=ocid1.compartment.oc1..aaaaaaaaddq4ttd4urh27c3lr54cuwmtcfo3powi7fsijrbse5p3xfv2ci6a";

        OCIRest ociObj = new OCIRest();
        //ociObj.getSignHeaders(m_tenancyOcId,m_userOcid,m_fingerPrint,m_privateKeyFilename,uri);

        ociObj.getAPIResponse(m_tenancyOcId,m_userOcid,m_fingerPrint,m_privateKeyFilename,uri); */
    }

    /** This method executes the GET request and print the response object.
     *
     * @param tenancyOcid
     * @param userOcid
     * @param fingerPrint
     * @param privateKeyFile
     * @param uri
     * @return
     */
    public String getAPIResponse(String tenancyOcid,
                                 String userOcid,
                                 String fingerPrint,
                                 String privateKeyFile,
                                 String uri)
    {
        HttpRequestBase request;
        String responseAsString = null;

        String apiKey = (tenancyOcid+"/"
                + userOcid+"/"
                + fingerPrint);

        PrivateKey privateKey = loadPrivateKey(privateKeyFile);
        RequestSigner signer = new RequestSigner(apiKey, privateKey);

        request = new HttpGet(uri);
        signer.signRequest(request);

        CloseableHttpClient client = HttpClientBuilder.create().build();

        // In case you need a proxy, please uncomment the followin code

        /*HttpHost proxy = new HttpHost("proxy.example.com", 80);
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        request.setConfig(config);*/

        try
        {

            HttpResponse response = client.execute(request);
            responseAsString = EntityUtils.toString(response.getEntity());
            System.out.println(responseAsString);

        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return responseAsString;
    }


    /** This method generates the required signature and adds to the request headers.
     *  For convenience of understanding the method also prints all the headers when executed.
     * @param tenancyOcid
     * @param userOcid
     * @param fingerPrint
     * @param uri
     * @return Map of Headers
     */
    public HashMap<String,String> getSignHeaders(String tenancyOcid,
                                                 String userOcid,
                                                 String fingerPrint,
                                                 String privateKeyFile,
                                                 String uri)
    {
        HashMap<String,String> headers = new HashMap<String,String>();
        HttpRequestBase request;

        // This is the keyId for a key uploaded through the console

        String apiKey = (tenancyOcid+"/"
                + userOcid+"/"
                + fingerPrint);
        // Private key file path in unix
        //String privateKeyFilename = "/home/oracle/oci_api_key.pem";
        PrivateKey privateKey = loadPrivateKey(privateKeyFile);
        RequestSigner signer = new RequestSigner(apiKey, privateKey);

        System.out.println(uri);
        request = new HttpGet(uri);

        signer.signRequest(request);
        Header[] auth = request.getHeaders("Authorization");
        Header[] date = request.getHeaders("date");
        Header[] host = request.getHeaders("host");
        System.out.println("Authorization Header: " + auth[0].toString().substring(15, auth[0].toString().length()));
        System.out.println("date Header: " + date[0].toString().substring(6, date[0].toString().length()));
        System.out.println("host Header: " + host[0].toString().substring(6, host[0].toString().length()));
        headers.put("Authorization",auth[0].toString().substring(15, auth[0].toString().length()));
        headers.put("date",date[0].toString().substring(6, date[0].toString().length()));
        headers.put("host",host[0].toString().substring(6, host[0].toString().length()));

        return headers;

    }

    /**
     * Load a {@link PrivateKey} from a file.
     */
    private static PrivateKey loadPrivateKey(String privateKeyFilename) {
        try (InputStream privateKeyStream = Files.newInputStream(Paths.get(privateKeyFilename))){
            return PEM.readPrivateKey(privateKeyStream);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid format for private key");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load private key");
        }
    }

    /**
     * A light wrapper around https://github.com/tomitribe/http-signatures-java
     */
    public static class RequestSigner {
        private static final SimpleDateFormat DATE_FORMAT;
        private static final String SIGNATURE_ALGORITHM = "rsa-sha256";
        private static final Map<String, List<String>> REQUIRED_HEADERS;
        static {
            DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
            REQUIRED_HEADERS = ImmutableMap.<String, List<String>>builder()
                    .put("get", ImmutableList.of("date", "(request-target)", "host"))
                    .put("head", ImmutableList.of("date", "(request-target)", "host"))
                    .put("delete", ImmutableList.of("date", "(request-target)", "host"))
                    .put("put", ImmutableList.of("date", "(request-target)", "host", "content-length", "content-type", "x-content-sha256"))
                    .put("post", ImmutableList.of("date", "(request-target)", "host", "content-length", "content-type", "x-content-sha256"))
                    .build();
        }
        private final Map<String, Signer> signers;

        /**
         * @param apiKey The identifier for a key uploaded through the console.
         * @param privateKey The private key that matches the uploaded public key for the given apiKey.
         */
        public RequestSigner(String apiKey, Key privateKey) {
            this.signers = REQUIRED_HEADERS
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> buildSigner(apiKey, privateKey, entry.getKey())));
        }

        /**
         * Create a {@link Signer} that expects the headers for a given method.
         * @param apiKey The identifier for a key uploaded through the console.
         * @param privateKey The private key that matches the uploaded public key for the given apiKey.
         * @param method HTTP verb for this signer
         * @return
         */
        protected Signer buildSigner(String apiKey, Key privateKey, String method) {
            final Signature signature = new Signature(
                    apiKey, SIGNATURE_ALGORITHM, null,REQUIRED_HEADERS.get(method.toLowerCase()));
            return new Signer(privateKey, signature);
        }

        /**
         * Sign a request, optionally including additional headers in the signature.
         *
         * <ol>
         * <li>If missing, insert the Date header (RFC 2822).</li>
         * <li>If PUT or POST, insert any missing content-type, content-length, x-content-sha256</li>
         * <li>Verify that all headers to be signed are present.</li>
         * <li>Set the request's Authorization header to the computed signature.</li>
         * </ol>
         *
         * @param request The request to sign
         */
        public void signRequest(HttpRequestBase request) {
            final String method = request.getMethod().toLowerCase();
            // nothing to sign for options
            if (method.equals("options")) {
                return;
            }

            final String path = extractPath(request.getURI());

            // supply date if missing
            if (!request.containsHeader("date")) {
                request.addHeader("date", DATE_FORMAT.format(new Date()));
            }

            // supply host if mossing
            if (!request.containsHeader("host")) {
                request.addHeader("host", request.getURI().getHost());
            }

            // supply content-type, content-length, and x-content-sha256 if missing (PUT and POST only)
            if (method.equals("put") || method.equals("post")) {
                if (!request.containsHeader("content-type")) {
                    request.addHeader("content-type", "application/json");
                }
                if (!request.containsHeader("content-length") || !request.containsHeader("x-content-sha256")) {
                    byte[] body = getRequestBody((HttpEntityEnclosingRequestBase) request);
                    if (!request.containsHeader("content-length")) {
                        request.addHeader("content-length", Integer.toString(body.length));
                    }
                    if (!request.containsHeader("x-content-sha256")) {
                        request.addHeader("x-content-sha256", calculateSHA256(body));
                    }
                }
            }

            final Map<String, String> headers = extractHeadersToSign(request);
            final String signature = this.calculateSignature(method, path, headers);
            request.setHeader("Authorization", signature);
        }

        /**
         * Extract path and query string to build the (request-target) pseudo-header.
         * For the URI "http://www.host.com/somePath?example=path" return "/somePath?example=path"
         */
        private static String extractPath(URI uri) {
            String path = uri.getRawPath();
            String query = uri.getRawQuery();
            if (query != null && !query.trim().isEmpty()) {
                path = path + "?" + query;
            }
            return path;
        }

        /**
         * Extract the headers required for signing from a {@link HttpRequestBase}, into a Map
         * that can be passed to {@link RequestSigner#calculateSignature}.
         *
         * <p>
         * Throws if a required header is missing, or if there are multiple values for a single header.
         * </p>
         *
         * @param request The request to extract headers from.
         */
        private static Map<String, String> extractHeadersToSign(HttpRequestBase request) {
            List<String> headersToSign = REQUIRED_HEADERS.get(request.getMethod().toLowerCase());
            if (headersToSign == null) {
                throw new RuntimeException("Don't know how to sign method " + request.getMethod());
            }
            return headersToSign.stream()
                    // (request-target) is a pseudo-header
                    .filter(header -> !header.toLowerCase().equals("(request-target)"))
                    .collect(Collectors.toMap(
                            header -> header,
                            header -> {
                                if (!request.containsHeader(header)) {
                                    throw new MissingRequiredHeaderException(header);
                                }
                                if (request.getHeaders(header).length > 1) {
                                    throw new RuntimeException(
                                            String.format("Expected one value for header %s", header));
                                }
                                return request.getFirstHeader(header).getValue();
                            }));
        }

        /**
         * Wrapper around {@link Signer}, returns the {@link Signature} as a String.
         *
         * @param method Request method (GET, POST, ...)
         * @param path The path + query string for forming the (request-target) pseudo-header
         * @param headers Headers to include in the signature.
         */
        private String calculateSignature(String method, String path, Map<String, String> headers) {
            Signer signer = this.signers.get(method);
            if (signer == null) {
                throw new RuntimeException("Don't know how to sign method " + method);
            }
            try {
                return signer.sign(method, path, headers).toString();
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate signature", e);
            }
        }

        /**
         * Calculate the Base64-encoded string representing the SHA256 of a request body
         * @param body The request body to hash
         */
        private String calculateSHA256(byte[] body) {
            byte[] hash = Hashing.sha256().hashBytes(body).asBytes();
            return Base64.getEncoder().encodeToString(hash);
        }

        /**
         * Helper to safely extract a request body.  Because an {@link HttpEntity} may not be repeatable,
         * this function ensures the entity is reset after reading.  Null entities are treated as an empty string.
         *
         * @param request A request with a (possibly null) {@link HttpEntity}
         */
        private byte[] getRequestBody(HttpEntityEnclosingRequestBase request) {
            HttpEntity entity = request.getEntity();
            // null body is equivalent to an empty string
            if (entity == null) {
                return "".getBytes(StandardCharsets.UTF_8);
            }
            // May need to replace the request entity after consuming
            boolean consumed = !entity.isRepeatable();
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            try {
                entity.writeTo(content);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy request body", e);
            }
            // Replace the now-consumed body with a copy of the content stream
            byte[] body = content.toByteArray();
            if (consumed) {
                request.setEntity(new ByteArrayEntity(body));
            }
            return body;
        }
    }
}
