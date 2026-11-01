package gt.edu.umg.desarrolloweb.pagos.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import gt.edu.umg.desarrolloweb.pagos.common.BancoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class AwsBancoClient {

    @Value("${aws.bank.url}")
    private String baseUrl;

    @Value("${aws.api.key}")
    private String apiKey;

    @Value("${aws.basic.auth}")
    private String basicAuth;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AwsBancoClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3)) // Timeout de conexión: 3s
                .build();
    }

    public HttpResponse<String> enviarPago(BancoMapper.BancoRequest request, String idempotencyKey) throws Exception {
        return enviarPago(request, idempotencyKey, null);
    }

    public HttpResponse<String> enviarPago(BancoMapper.BancoRequest request, String idempotencyKey, String escenario) throws Exception {
        String json = objectMapper.writeValueAsString(request);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/bank/v1/payments"))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", idempotencyKey)
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (escenario != null && !escenario.isEmpty()) {
            builder.header("X-Simulation-Scenario", escenario);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}