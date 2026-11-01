package gt.edu.umg.desarrolloweb.pagos.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import gt.edu.umg.desarrolloweb.pagos.client.AwsBancoClient;
import gt.edu.umg.desarrolloweb.pagos.client.AwsErpClient;
import gt.edu.umg.desarrolloweb.pagos.common.*;
import gt.edu.umg.desarrolloweb.pagos.config.RabbitConfig;
import gt.edu.umg.desarrolloweb.pagos.dynamo.DynamoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpResponse;

@Component
public class PagoConsumer {

    private final ObjectMapper objectMapper;
    private final AwsBancoClient bancoClient;
    private final AwsErpClient erpClient;
    private final DynamoService dynamoService;
    private final IdempotencyKeyFactory idFactory = new IdempotencyKeyFactory("EQA");

    public PagoConsumer(ObjectMapper objectMapper, AwsBancoClient bancoClient, AwsErpClient erpClient, DynamoService dynamoService) {
        this.objectMapper = objectMapper;
        this.bancoClient = bancoClient;
        this.erpClient = erpClient;
        this.dynamoService = dynamoService;
    }

   @RabbitListener(queues = RabbitConfig.QUEUE_BANCO)
    public void recibirPagoBanco(String jsonPago, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesarPago(jsonPago, Destino.BANCO, channel, tag);
    }

   @RabbitListener(queues = RabbitConfig.QUEUE_ERP)
    public void recibirPagoErp(String jsonPago, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesarPago(jsonPago, Destino.ERP, channel, tag);
    }

    private void procesarPago(String jsonPago, Destino destino, Channel channel, long tag) throws IOException {
        try {
            System.out.println("----------------------------------------");
            System.out.println("CONSUMER " + destino + " recibió pago.");

            String escenario = "";
            String jsonReal = jsonPago;
            if (jsonPago.contains("||")) {
                String[] partes = jsonPago.split("\\|\\|");
                jsonReal = partes[0];
                escenario = partes.length > 1 ? partes[1] : "";
            }

            PagoOrigen pago = objectMapper.readValue(jsonReal, PagoOrigen.class);
            String llave = idFactory.para(pago.pagoId());

            var errores = ValidadorPago.validar(pago, destino, java.time.ZonedDateTime.now());
            if (!errores.isEmpty()) {
                System.out.println("Pago inválido (fallo permanente). Enviando a DLQ.");
                channel.basicNack(tag, false, false); // false = NO volver a la cola, va a DLQ
                return;
            }

            HttpResponse<String> response;
            if (destino == Destino.BANCO) {
                BancoMapper.BancoRequest req = BancoMapper.mapear(pago, "EQUIPO_A");
                response = bancoClient.enviarPago(req, llave, escenario);
            } else {
                ErpMapper.ErpRequest req = ErpMapper.mapear(pago, "EQUIPO_A", llave);
                response = erpClient.enviarPago(req, escenario);
            }

            int status = response.statusCode();
            String body = response.body();
            System.out.println("Respuesta de AWS (" + destino + "): HTTP " + status);

            String codigoError = null;
            if (status == 409 && (body.contains("DUPLICATE_PAYMENT") || body.contains("DOCUMENTO_DUPLICADO"))) {
                codigoError = destino == Destino.BANCO ? "DUPLICATE_PAYMENT" : "DOCUMENTO_DUPLICADO";
            }

            ClasificadorRespuesta.Accion accion = ClasificadorRespuesta.clasificar(status, codigoError);
            
            if (accion == ClasificadorRespuesta.Accion.EXITO) {
                System.out.println("-> Pago EXITOSO. ACK. (Mensaje eliminado de la cola).");
                dynamoService.guardarLog(String.valueOf(pago.pagoId()), destino.name(), "EXITOSO", status, null);
                dynamoService.actualizarEstado(String.valueOf(pago.pagoId()), destino.name(), "ENVIADO", llave);
                channel.basicAck(tag, false); // ACK: Elimina el mensaje
            } else if (accion == ClasificadorRespuesta.Accion.REINTENTAR) {
                System.out.println("-> Pago FALLÓ temporalmente. NACK. (Se enviará a reintento/DLQ).");
                dynamoService.guardarLog(String.valueOf(pago.pagoId()), destino.name(), "FALLIDO_TEMPORAL", status, "REINTENTAR");
                dynamoService.actualizarEstado(String.valueOf(pago.pagoId()), destino.name(), "REINTENTO_PENDIENTE", llave);
                channel.basicNack(tag, false, false); // NACK: Lo manda a la DLQ o reintento
            } else {
                System.out.println("-> Pago RECHAZADO. NACK. (Se enviará a DLQ).");
                dynamoService.guardarLog(String.valueOf(pago.pagoId()), destino.name(), "FALLIDO_PERMANENTE", status, "DLQ");
                dynamoService.actualizarEstado(String.valueOf(pago.pagoId()), destino.name(), "FALLIDO_PERMANENTE", llave);
                channel.basicNack(tag, false, false); // NACK: Lo manda a la DLQ
            }

        } catch (Exception e) {
            System.err.println("Error de red/timeout: " + e.getMessage());
            // Si falla la red, devolvemos el mensaje a la cola (true) para que se reintente
            channel.basicNack(tag, false, true); 
        }
    }
}