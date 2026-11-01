package gt.edu.umg.desarrolloweb.pagos.producer;

import gt.edu.umg.desarrolloweb.pagos.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PagoProducer {

    private final RabbitTemplate rabbitTemplate;

    public PagoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarAlBanco(String idempotencyKey, String jsonPago) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_BANCO, jsonPago);
        System.out.println("Enviado a cola BANCO: " + idempotencyKey);
    }

    public void enviarAlErp(String idempotencyKey, String jsonPago) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_ERP, jsonPago);
        System.out.println("Enviado a cola ERP: " + idempotencyKey);
    }
}