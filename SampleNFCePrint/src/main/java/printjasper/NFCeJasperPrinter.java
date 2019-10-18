/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package printjasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRXmlDataSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;


/**
 *
 * @author AlissonLima
 */
public class NFCeJasperPrinter {
    private final String nfeContent;
    private final NFCeJasperParameters parameters;

    public NFCeJasperPrinter(String nfeContent, NFCeJasperParameters parameters) {
        this.nfeContent = nfeContent;
        this.parameters = parameters;
    }


    public byte[] printToPDFByteArray() throws Exception {

        JasperPDFExporter jasperPDFExporter = new JasperPDFExporter(createJasperData());
        return jasperPDFExporter.exportToByteArray();
    }

    public void printToPDFFile(String path) throws Exception {
        JasperPDFExporter jasperPDFExporter = new JasperPDFExporter(createJasperData());
        jasperPDFExporter.exportToFile(path);
    }

    private JasperData createJasperData() throws JRException {
        JasperData jasperData = new JasperData();

        if (NFCeJasperParameters.PAPERWIDTH.PAPER_58MM.equals(parameters.paperWidth)) {
            jasperData.jasperInputStream = NFCeJasperPrinter.class.getClassLoader().getResourceAsStream("jasper_58/NSDanfeNFCe.jasper");
        } else {
            jasperData.jasperInputStream = NFCeJasperPrinter.class.getClassLoader().getResourceAsStream("jasper/NSDanfeNFCe.jasper");
        }

        jasperData.parameters = parameters.toMap();

        String jrXmlQuery;
        if (parameters.isProc){
            jrXmlQuery = "/nfeProc/NFe";
        }else{
            jrXmlQuery = "/NFe";
        }

        jasperData.jrDataSource = new JRXmlDataSource(new ByteArrayInputStream(nfeContent.getBytes(StandardCharsets.UTF_8)), jrXmlQuery);

        return jasperData;
    }


}
