package gt.edu.umg.desarrolloweb.pagos.common;

public class ClasificadorRespuesta {

    public enum Accion {
        EXITO,          // Guardar comprobante y marcar ENVIADO (201, 409 duplicado)
        REINTENTAR,     // Fallo transitorio (429, 500, 502, 503, 504, timeout)
        DLQ             // Fallo permanente, no reintentar (400, 403, 422, 409 reutilizado)
    }

    public static Accion clasificar(int httpStatus, String codigoError) {
        if (httpStatus == 201) return Accion.EXITO;
        
        if (httpStatus == 409) {
            if ("DUPLICATE_PAYMENT".equals(codigoError) || "DOCUMENTO_DUPLICADO".equals(codigoError)) {
                return Accion.EXITO; 
            }
            return Accion.DLQ; 
        }

        if (httpStatus == 400 || httpStatus == 403 || httpStatus == 422) {
            return Accion.DLQ;
        }

        if (httpStatus == 429 || httpStatus == 500 || httpStatus == 502 || httpStatus == 503 || httpStatus == 504) {
            return Accion.REINTENTAR;
        }

        return Accion.DLQ; 
    }
}