package api_bancos_spring_cloud;

import gt.edu.umg.desarrolloweb.pagos.erp.client.ErpApi;
import gt.edu.umg.desarrolloweb.pagos.erp.dto.ErpSaludDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;


@RestController
public class PruebaController {

    private final ErpApi erpApi;

    public PruebaController(ErpApi erpApi) {
        this.erpApi = erpApi;
    }

    @GetMapping("/probar-salud")
    public ResponseEntity<ErpSaludDto> probarConexion() {
        return erpApi.consultarSaludErp(); 
    }
}   