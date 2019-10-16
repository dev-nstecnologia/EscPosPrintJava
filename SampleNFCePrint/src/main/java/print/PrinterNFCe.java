package print;

import br.eti.ns.nsminiprinters.MiniPrinterModel;
import br.eti.ns.nsminiprinters.escpos.EscPosPrinter;
import br.eti.ns.nsminiprinters.escpos.PrinterOptions;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpec;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpecFactory;

import commons.*;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import printjasper.NFCeJasperParameters;
import printjasper.NFCeJasperPrinter;
import printjasper.QRCodeHelper;
import schema.TNFe;
import schema.TNfeProc;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author matheus.mazzoni
 */
public class PrinterNFCe {
    private final static Logger logger = Logger.getLogger(PrinterNFCe.class);

    //Método que irá imprimir a NFC-e
    public static void printNFCe(String path, PrinterParameters parameters, PrinterOptions printerOptions) throws Exception {

        logger.debug("Iniciando impressão de NCF-e em Mini Impressora");

        Object nfce = XMLtoTNFCe(path);

        if(!Objects.equals(nfce, null)){

            if (nfce.getClass().equals(TNFe.class)) {
                TNFe tNFe = (TNFe) nfce;
                setPrinterParameters(parameters, tNFe);

            } else {
                TNfeProc tNFe = (TNfeProc) nfce;
                setPrinterParameters(parameters, tNFe);
            }

            MiniPrinterModel miniPrinter = MiniPrinterModel.getByName(parameters.getPrinterName());
            if (Objects.equals(miniPrinter, null)) return;
            PrinterSpec printerSpec = PrinterSpecFactory.getByModel(miniPrinter, printerOptions.paperWidth);

            CreatePrint print = new CreatePrint(printerSpec, printerOptions, parameters);
            EscPosPrinter escPosPrinter = new EscPosPrinter(printerOptions);

            byte[] nfcePrintConsumer = print.createNFePrint(NFCeVia.CONSUMIDOR);

            try {
                escPosPrinter.print(nfcePrintConsumer);
            } catch (SerialPortException sp) {
                JOptionPane.showMessageDialog(null, "Impressora não encontrada\n\r" + "Porta Selecionada: " + sp.getPortName());
            }

            if (!parameters.getNfceContent().getInfNFe().getIde().getTpEmis().equals("1")) {

                byte[] nfcePrintEstablishment = print.createNFePrint(NFCeVia.ESTABELECIMENTO);

                try {
                    escPosPrinter.print(nfcePrintEstablishment);
                } catch (SerialPortException sp) {
                    JOptionPane.showMessageDialog(null, "Impressora não encontrada\n\r" + "Porta Selecionada: " + sp.getPortName());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "O XML informado não é uma NFCe, tente novamente");
        }
    }

    //Método que gera uma NFC-e em PDF
    public static void generatePDF(String path, String pathSave, PrinterParameters parameters, PrinterOptions printerOptions) throws Exception {

        NFCeJasperParameters jasperParameters = new NFCeJasperParameters();

        Object nfce = XMLtoTNFCe(path);

        if(Objects.equals(nfce, null)){
            JOptionPane.showMessageDialog(null, "O XML informado não é uma NFCe, tente novamente");
        }else{

            if (nfce.getClass().equals(TNFe.class)) {

                TNFe tNFe = (TNFe) nfce;
                jasperParameters.isProc = false;
                setAndGenerateJasperParameters(printerOptions, parameters, jasperParameters, tNFe, path, pathSave);

            } else {

                TNfeProc tNFeProc = (TNfeProc) nfce;
                jasperParameters.isProc = true;
                setAndGenerateJasperParameters(printerOptions, parameters, jasperParameters, tNFeProc.getNFe(), path, pathSave);
            }
        }
    }

    //Transforma o xml em objeto NFC-e
    private static Object XMLtoTNFCe(String fileXML) throws Exception {

        try {
            String strXML = new String(Files.readAllBytes(Paths.get(fileXML)));
            Object tnp;
            if (strXML.contains("nfeProc")) {

                JAXBContext jb = JAXBContext.newInstance(TNfeProc.class);
                Unmarshaller un = jb.createUnmarshaller();
                tnp = un.unmarshal(new StreamSource(new StringReader(strXML)), TNfeProc.class).getValue();

            } else if (strXML.contains("NFe")) {

                JAXBContext jb = JAXBContext.newInstance(schema.TNFe.class);
                Unmarshaller un = jb.createUnmarshaller();
                tnp = un.unmarshal(new StreamSource(new StringReader(strXML)), TNFe.class).getValue();

            } else {
                tnp = null;
            }
            return tnp;

        } catch (JAXBException ex) { throw new Exception(ex.toString()); }
    }

    //Testa se esta em contigencia e setta os Jasper Parameters
    private static void setAndGenerateJasperParameters(PrinterOptions printerOptions,  PrinterParameters parameters, NFCeJasperParameters jasperParameters, TNFe nfce, String path, String pathSave) throws Exception {
        setPrinterParameters(parameters, nfce);
        if(printerOptions.paperWidth.equals(PrinterOptions.PAPERWIDTH.PAPER_58MM)){
            jasperParameters.paperWidth = NFCeJasperParameters.PAPERWIDTH.PAPER_58MM;
        }else{
            jasperParameters.paperWidth = NFCeJasperParameters.PAPERWIDTH.PAPER_80MM;
        }

        jasperParameters.qrCodePath = parameters.getQrCodeContent();
        jasperParameters.urlConsulta = parameters.getUrlConsult();
        jasperParameters.qrCodeImage = QRCodeHelper.generateQRCodeToPNGStream(jasperParameters.qrCodePath);
        jasperParameters.valorTroco = nfce.getInfNFe().getPag().getVTroco();
        jasperParameters.isConsumerTicket = true;

        savePDF(jasperParameters, path, pathSave, nfce.getInfNFe().getId() + "-consumidor.pdf");

        if (!parameters.getNfceContent().getInfNFe().getIde().getTpEmis().equals("1")) {
            jasperParameters.qrCodePath = parameters.getQrCodeContent();
            jasperParameters.qrCodeImage = QRCodeHelper.generateQRCodeToPNGStream(jasperParameters.qrCodePath);
            jasperParameters.isConsumerTicket = false;
            savePDF(jasperParameters, path, pathSave,  nfce.getInfNFe().getId() + "-estabelecimento.pdf");
        }
        JOptionPane.showMessageDialog(null, "Geração de PDF feita com sucesso!!!");
    }

    //Salva o PDF gerado pelos Jasper Parameters
    private static void savePDF(NFCeJasperParameters jasperParameters, String fileXML, String pathToSavePDF, String nameArq) throws Exception {

        if(!pathToSavePDF.endsWith("\\")) pathToSavePDF += "\\";

        File localSalvar = new File(pathToSavePDF);
        if (!localSalvar.exists())localSalvar.mkdirs();

        String strXML = new String(Files.readAllBytes(Paths.get(fileXML)));
        NFCeJasperPrinter nfceJasperPrinter = new NFCeJasperPrinter(strXML, jasperParameters);

        File arq = new File(pathToSavePDF + nameArq);
        if(arq.exists()) arq.delete();

        nfceJasperPrinter.printToPDFFile(pathToSavePDF+ nameArq);
    }

    //Setta os Prints Parameters
    private static void setPrinterParameters(PrinterParameters parameters, TNFe tNfe){
        parameters.setNfceContent(tNfe);
        parameters.setUrlConsult(tNfe.getInfNFeSupl().getUrlChave());
        parameters.setQrCodeContent(tNfe.getInfNFeSupl().getQrCode());
        parameters.setAuthorizationProtocol("");
    }

    private static void setPrinterParameters(PrinterParameters parameters, TNfeProc tNfe){
        parameters.setNfceContent(tNfe.getNFe());
        parameters.setUrlConsult(tNfe.getNFe().getInfNFeSupl().getUrlChave());
        parameters.setQrCodeContent(tNfe.getNFe().getInfNFeSupl().getQrCode());
        parameters.setAuthorizationProtocol(tNfe.getProtNFe().getInfProt().getNProt());
    }

}
