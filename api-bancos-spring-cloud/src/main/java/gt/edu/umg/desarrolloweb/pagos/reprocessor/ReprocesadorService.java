package gt.edu.umg.desarrolloweb.pagos.reprocessor;

import gt.edu.umg.desarrolloweb.pagos.dynamo.DynamoService;
import gt.edu.umg.desarrolloweb.pagos.dynamo.PagoEstado;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReprocesadorService {

    private final DynamoService dynamoService;

    public ReprocesadorService(DynamoService dynamoService) {
        this.dynamoService = dynamoService;
    }

    // Se ejecuta cada 10 segundos
    @Scheduled(fixedRate = 10000)
    public void buscarYReintentar() {
        try {
            List<PagoEstado> pendientes = dynamoService.obtenerPendientes();
            if (!pendientes.isEmpty()) {
                System.out.println("[REPROCESADOR] Encontrados " + pendientes.size() + " pagos pendientes de reintento.");
                for (PagoEstado p : pendientes) {
                    System.out.println("[REPROCESADOR] Reintentando pagoId=" + p.getPagoId() + " para " + p.getDestino());
                    // Aquí iría la lógica para volver a publicar en RabbitMQ
                    // Por ahora solo lo marcamos como EN_PROCESO para que el robot no lo repita infinitamente
                    dynamoService.actualizarEstado(p.getPagoId(), p.getDestino(), "EN_PROCESO", p.getLlaveIdempotencia());
                }
            }
        } catch (Exception e) {
            System.err.println("Error en reprocesador: " + e.getMessage());
        }
    }
}
