package printjasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Created by alissonlima on 3/15/16.
 */
public abstract class JasperExporter {

    protected final JasperData jasperData;

    protected JasperExporter(JasperData jasperData) {
        this.jasperData = jasperData;
    }

    protected JasperPrint createJasperPrint() throws JRException {
        return JasperFillManager.fillReport(
                loadJasperReport(),
                jasperData.parameters,
                jasperData.jrDataSource);
    }

    private JasperReport loadJasperReport() throws JRException {
        return (JasperReport) JRLoader.loadObject(jasperData.jasperInputStream);
    }
}
