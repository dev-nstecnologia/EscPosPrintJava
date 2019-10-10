package printjasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;


import java.util.Base64;

/**
 * Created by alissonlima on 3/15/16.
 */
public class JasperPDFExporter extends JasperExporter{

    public JasperPDFExporter(JasperData jasperData) {
        super(jasperData);
    }

    public void exportToFile(String filePath) throws JRException {
        JasperExportManager.exportReportToPdfFile(createJasperPrint(), filePath);
    }

    public byte[] exportToByteArray() throws JRException {
        return JasperExportManager.exportReportToPdf(createJasperPrint());
    }

    public byte[] exportToBase64() throws JRException {
        return Base64.getEncoder().encode(exportToByteArray());
    }

}
