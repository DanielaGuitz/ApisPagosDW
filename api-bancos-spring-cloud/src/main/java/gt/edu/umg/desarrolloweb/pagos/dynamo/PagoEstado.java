package gt.edu.umg.desarrolloweb.pagos.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class PagoEstado {
    private String pagoId;
    private String destino;
    private String estado;
    private String llaveIdempotencia;
    private String ultimoError;

    @DynamoDbPartitionKey
    public String getPagoId() { return pagoId; }
    public void setPagoId(String pagoId) { this.pagoId = pagoId; }

    @DynamoDbSortKey
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getLlaveIdempotencia() { return llaveIdempotencia; }
    public void setLlaveIdempotencia(String llaveIdempotencia) { this.llaveIdempotencia = llaveIdempotencia; }

    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String ultimoError) { this.ultimoError = ultimoError; }
}