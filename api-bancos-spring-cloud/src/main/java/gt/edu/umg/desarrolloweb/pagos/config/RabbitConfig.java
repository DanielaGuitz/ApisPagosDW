package gt.edu.umg.desarrolloweb.pagos.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "pagos.exchange";
    public static final String QUEUE_BANCO = "pagos.banco";
    public static final String QUEUE_ERP = "pagos.erp";
    public static final String DLQ_BANCO = "pagos.banco.dlq";
    public static final String DLQ_ERP = "pagos.erp.dlq";
    public static final String ROUTING_BANCO = "routing.banco";
    public static final String ROUTING_ERP = "routing.erp";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queueBanco() {
        return QueueBuilder.durable(QUEUE_BANCO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", "routing.banco.dlq")
                .build();
    }

    @Bean
    public Queue queueErp() {
        return QueueBuilder.durable(QUEUE_ERP)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", "routing.erp.dlq")
                .build();
    }

    @Bean
    public Queue dlqBanco() {
        return QueueBuilder.durable(DLQ_BANCO).build();
    }

    @Bean
    public Queue dlqErp() {
        return QueueBuilder.durable(DLQ_ERP).build();
    }

    @Bean
    public Binding bindingBanco(Queue queueBanco, DirectExchange exchange) {
        return BindingBuilder.bind(queueBanco).to(exchange).with(ROUTING_BANCO);
    }

    @Bean
    public Binding bindingErp(Queue queueErp, DirectExchange exchange) {
        return BindingBuilder.bind(queueErp).to(exchange).with(ROUTING_ERP);
    }

    @Bean
    public Binding bindingDlqBanco(Queue dlqBanco, DirectExchange exchange) {
        return BindingBuilder.bind(dlqBanco).to(exchange).with("routing.banco.dlq");
    }

    @Bean
    public Binding bindingDlqErp(Queue dlqErp, DirectExchange exchange) {
        return BindingBuilder.bind(dlqErp).to(exchange).with("routing.erp.dlq");
    }
}