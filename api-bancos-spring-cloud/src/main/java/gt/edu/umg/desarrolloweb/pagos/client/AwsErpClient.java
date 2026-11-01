package gt.edu.umg.desarrolloweb.pagos.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import gt.edu.umg.desarrolloweb.pagos.common.ErpMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class AwsErpClient {

    @Value("${aws.erp.url}")
    private String baseUrl;

    @Value("${aws.api.key}")
    private String apiKey;

    @Value("${aws.basic.auth}")
    private String basicAuth;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AwsErpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public HttpResponse<String> enviarPago(ErpMapper.ErpRequest request) throws Exception {
        return enviarPago(request, null);
    }

    public HttpResponse<String> enviarPago(ErpMapper.ErpRequest request, String escenario) throws Exception {
        String json = objectMapper.writeValueAsString(request);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/erp/v1/pagos"))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (escenario != null && !escenario.isEmpty()) {
            builder.header("X-Simulation-Scenario", escenario);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}