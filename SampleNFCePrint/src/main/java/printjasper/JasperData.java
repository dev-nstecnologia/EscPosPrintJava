package printjasper;

import net.sf.jasperreports.engine.JRDataSource;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by alissonlima on 3/15/16.
 */
public class JasperData {

    /**
     * Hold and inputstream to the .jasper file of the report.
     */
    public InputStream jasperInputStream;

    /**
     * The report datasource.
     */
    public JRDataSource jrDataSource;

    /**
     * Parameters to be passed to the report.
     */
    public Map<String, Object> parameters;
}
