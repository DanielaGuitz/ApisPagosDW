package gt.edu.umg.desarrolloweb.pagos.dynamo;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class DynamoService {

    private final DynamoDbTable<PagoEstado> estadoTable;
    private final DynamoDbTable<PagoLog> logTable;

    public DynamoService(DynamoDbEnhancedClient client) {
        this.estadoTable = client.table("pagos_envio_estado", TableSchema.fromBean(PagoEstado.class));
        this.logTable = client.table("pagos_envio_logs", TableSchema.fromBean(PagoLog.class));
        try {
            this.estadoTable.createTable();
            this.logTable.createTable();
        } catch (Exception e) {
            // Ya existen, no pasa nada
        }
    }

    public void guardarLog(String pagoId, String destino, String estado, int status, String error) {
        PagoLog log = new PagoLog();
        log.setId(UUID.randomUUID().toString());
        log.setPaymentId(pagoId);
        log.setDestino(destino);
        log.setEstado(estado);
        log.setHttpStatus(status);
        log.setCodigoError(error);
        log.setFecha(ZonedDateTime.now().toString());
        logTable.putItem(log);
    }

    public void actualizarEstado(String pagoId, String destino, String estado, String llave) {
        PagoEstado p = new PagoEstado();
        p.setPagoId(pagoId);
        p.setDestino(destino);
        p.setEstado(estado);
        p.setLlaveIdempotencia(llave);
        estadoTable.putItem(p);
    }

    public List<PagoEstado> obtenerPendientes() {
        return estadoTable.scan().items().stream()
                .filter(p -> "REINTENTO_PENDIENTE".equals(p.getEstado()))
                .toList();
    }

    public void imprimirTodo() {
        System.out.println("--- CONTENIDO DE DYNAMODB (LOGS) ---");
        try {
            logTable.scan().items().forEach(log -> {
                System.out.println("Log ID: " + log.getId() + 
                                   " | Pago ID: " + log.getPaymentId() + 
                                   " | Destino: " + log.getDestino() + 
                                   " | Estado: " + log.getEstado() + 
                                   " | HTTP: " + log.getHttpStatus());
            });
        } catch (Exception e) {
            System.out.println("No hay logs guardados aún o error: " + e.getMessage());
        }
        System.out.println("------------------------------------");
    }
}