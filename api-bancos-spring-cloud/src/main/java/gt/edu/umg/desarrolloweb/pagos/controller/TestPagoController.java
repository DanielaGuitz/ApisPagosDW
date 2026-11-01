package gt.edu.umg.desarrolloweb.pagos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gt.edu.umg.desarrolloweb.pagos.common.*;
import gt.edu.umg.desarrolloweb.pagos.dynamo.DynamoService;
import gt.edu.umg.desarrolloweb.pagos.producer.PagoProducer;
import gt.edu.umg.desarrolloweb.pagos.reader.MonolitoPagoReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestPagoController {

    private final PagoProducer producer;
    private final MonolitoPagoReader reader;
    private final ObjectMapper objectMapper;
    private final DynamoService dynamoService;

    public TestPagoController(PagoProducer producer, MonolitoPagoReader reader, ObjectMapper objectMapper, DynamoService dynamoService) {
        this.producer = producer;
        this.reader = reader;
        this.objectMapper = objectMapper;
        this.dynamoService = dynamoService;
    }

    @GetMapping("/test/leer-y-enviar")
    public String leerYEnviar(@org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "") String escenario) {
        try {
            List<PagoOrigen> pagos = reader.leerPagosCobrados();
            if (pagos.isEmpty()) {
                return "No se encontraron pagos con estado 'Completado' en la BD.";
            }

            IdempotencyKeyFactory factory = new IdempotencyKeyFactory("EQA");
            StringBuilder resultado = new StringBuilder("Se enviaron " + pagos.size() + " pagos a RabbitMQ:\n");

            for (PagoOrigen pago : pagos) {
                String llave = factory.para(pago.pagoId());
                String jsonPago = objectMapper.writeValueAsString(pago);
                
                producer.enviarAlBanco(llave, jsonPago + "||" + escenario);
                producer.enviarAlErp(llave, jsonPago + "||" + escenario);
                
                resultado.append("- Pago ID: ").append(pago.pagoId())
                         .append(" | Cliente: ").append(pago.clienteNombre())
                         .append(" | Monto: Q").append(pago.montoTotal())
                         .append("\n");
            }

            // Imprimimos lo que hay en DynamoDB
            dynamoService.imprimirTodo();

            return resultado.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al leer la BD o enviar: " + e.getMessage();
        }
    }

    @GetMapping("/test/solo-producer")
    public String soloProducer() {
        try {
        
            System.out.println("[PRODUCER-TEST] INICIANDO PRUEBA  CAÍDA");
            

            // 1. Leer de la BD
            List<PagoOrigen> pagos = reader.leerPagosCobrados();
            if (pagos.isEmpty()) {
                return "No se encontraron pagos.";
            }
            System.out.println("[PRODUCER-TEST] 1. Pago obtenido de la base de datos MySQL.");

            // 2. Preparar el JSON
            PagoOrigen pago = pagos.get(0);
            String jsonPago = objectMapper.writeValueAsString(pago);
            String llave = new IdempotencyKeyFactory("EQA").para(pago.pagoId());

            System.out.println("[PRODUCER-TEST] 2. Pago transformado a JSON. Llave: " + llave);

            // 3. Enviar a la cola
            System.out.println("[PRODUCER-TEST] 3. Enviando a RabbitMQ (Exchange: pagos.exchange)...");
            producer.enviarAlBanco(llave, jsonPago + "||SUCCESS");
            producer.enviarAlErp(llave, jsonPago + "||SUCCESS");

            // 4. Confirmación final
            System.out.println("[PRODUCER-TEST] 4. ¡Mensajes entregados a RabbitMQ con éxito!");
            System.out.println("[PRODUCER-TEST] Como el Consumer está APAGADO, los mensajes se quedarán en la cola.");
            System.out.println("=======================================================\n");

            return "Prueba de Productor exitosa. Los mensajes están atrapados en RabbitMQ.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}