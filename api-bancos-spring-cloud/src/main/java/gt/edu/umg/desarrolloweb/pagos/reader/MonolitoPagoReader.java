package gt.edu.umg.desarrolloweb.pagos.reader;

import gt.edu.umg.desarrolloweb.pagos.common.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class MonolitoPagoReader {

    private final JdbcTemplate jdbc;

    public MonolitoPagoReader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    

    public List<PagoOrigen> leerPagosCobrados() {
        // Hacemos el JOIN para traer cliente, contador y lectura.
        // Asumimos que 'Completado' significa cobrado. Solo traemos 1 para prueba.
        String sql = """
            SELECT p.id AS pago_id, p.monto, p.fecha_pago, p.metodo,
                   c.nombre AS cliente_nombre, c.id AS cliente_id,
                   l.periodo, ct.codigo AS contador_codigo
            FROM Pagos p
            JOIN Lecturas l ON p.lectura_id = l.id
            JOIN Contadores ct ON l.contador_id = ct.id
            JOIN Clientes c ON ct.cliente_id = c.id
            WHERE p.estado = 'Completado'
            LIMIT 5
        """;

        

        

        return jdbc.query(sql, (rs, rowNum) -> {
            long pagoId = rs.getLong("pago_id");
            BigDecimal monto = rs.getBigDecimal("monto");
            
            // Mapeo de método de pago de la BD al Enum
            String metodoDb = rs.getString("metodo");
            FormaPago forma = FormaPago.EFECTIVO; // Por defecto
            if (metodoDb != null) {
                if (metodoDb.toUpperCase().contains("TARJETA")) forma = FormaPago.TARJETA;
                else if (metodoDb.toUpperCase().contains("TRANSF")) forma = FormaPago.TRANSFERENCIA;
                else if (metodoDb.toUpperCase().contains("CHEQUE")) forma = FormaPago.CHEQUE;
            }

            // Fechas: la BD guarda DATE, le ponemos hora 12:00 y zona de Guatemala
            java.sql.Date fechaSql = rs.getDate("fecha_pago");
            ZonedDateTime fecha = fechaSql.toLocalDate().atTime(12, 0).atZone(ZoneId.of("America/Guatemala"));

            // Periodo (viene como DATE en la tabla Lecturas)
            java.sql.Date periodoDate = rs.getDate("periodo");
            int anio = periodoDate.toLocalDate().getYear();
            int mes = periodoDate.toLocalDate().getMonthValue();

            return new PagoOrigen(
                pagoId,
                "CLI-" + rs.getInt("cliente_id"), // Formateamos el ID de cliente
                rs.getString("cliente_nombre"),
                "CF", // La BD no tiene NIT, asumimos Consumidor Final
                rs.getString("contador_codigo"),
                monto,
                List.of(new RubroDetalle(Rubro.AGUA_POTABLE, "Consumo de agua", monto)), // Asumimos todo a agua
                forma,
                fecha,
                anio, mes,
                "Pago desde monolito"
            );
        });
    }
}