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
        NFCeJasperParameters jasperParameters = new NFCeJasperParameters();
        
        logger.debug("Iniciando impressão de NCF-e em Mini Impressora");

        //Cria a partir do xml um objeto NFC-e
        Object nfce = XMLtoTNFCe(path);

        //Checa se o .xml é uma NFCe
        if(nfce==null){
            JOptionPane.showMessageDialog(null, "O XML informado não é uma NFCe, tente novamente");
            return;
        }
        //Setta as informacoes das impressoes
        if (nfce.getClass().equals(TNFe.class)) {
            TNFe tNfe = (TNFe) nfce;
            setPrinterParameters(parameters, tNfe);

        } else {
            TNfeProc tNfe = (TNfeProc) nfce;
            setPrinterParameters(parameters, tNfe);
        }

        //Cria especificações dependendo da impressora utilizada
        MiniPrinterModel miniPrinter = MiniPrinterModel.getByName(parameters.getPrinterName());
        PrinterSpec printerSpec = PrinterSpecFactory.getByModel(miniPrinter, printerOptions.paperWidth);

        //Cria o objeto que monta impressão e o objeto que imprime a NFCe
        CreatePrint print = new CreatePrint(printerSpec, printerOptions, parameters);
        EscPosPrinter escPosPrinter = new EscPosPrinter(printerOptions);

        //Se for uma emissão 'Contigência' irá imprimir via de Estabelecimento e para o Consumidor
        if (!parameters.getNfceContent().getInfNFe().getIde().getTpEmis().equals("1")) {

            //Montará a impressão atraves das informações coletadas e manda para a impressora que irá imprimir
            byte[] nfcePrintEstablishment = print.createNFePrint(NFCeVia.ESTABELECIMENTO);
            escPosPrinter.print(nfcePrintEstablishment);

            //Montará a impressão atraves das informações coletadas e manda para a impressora que irá imprimir
            byte[] nfcePrintConsumer = print.createNFePrint(NFCeVia.CONSUMIDOR);
            escPosPrinter.print(nfcePrintConsumer);

            //Se não irá imprimir somente para o Consumidor
        } else {

            //Montará a impressão atraves das informações coletadas até agora
            byte[] nfcePrintConsumer = print.createNFePrint(NFCeVia.CONSUMIDOR);

            //Pega a impressão montada e manda para a impressora que irá imprimir
            try {
                escPosPrinter.print(nfcePrintConsumer);
            } catch (SerialPortException sp) {

                JOptionPane.showMessageDialog(null, "Impressora não encontrada\n\r" + "Porta Selecionada: " + sp.getPortName());
            }
        }
    }

    //Método que gera uma NFC-e em PDF
    public static void generatePDF(String path, PrinterParameters parameters, PrinterOptions printerOptions) throws Exception {
        NFCeJasperParameters jasperParameters = new NFCeJasperParameters();

        //Cria a partir do xml um objeto NFC-e
        Object nfce = XMLtoTNFCe(path);

        //Checa se o .xml é uma NFCe
        if(nfce==null){
            JOptionPane.showMessageDialog(null, "O XML informado não é uma NFCe, tente novamente");
            return;
        }

        //Setta as url, qrcode e protocolo dependendo do tipo de emissão: Autorizada ou Contigência e ja salva o pdf
        if (nfce.getClass().equals(TNFe.class)) {
            TNFe tNfe = (TNFe) nfce;
            jasperParameters.isProc = false;
            setAndGenerateJasperParameters(printerOptions, parameters, jasperParameters, tNfe, path);
        } else {
            TNfeProc tNfe = (TNfeProc) nfce;
            jasperParameters.isProc = true;
            setAndGenerateJasperParameters(printerOptions, parameters, jasperParameters, tNfe.getNFe(), path);
        }

    }

    //Transforma o xml em objeto NFC-e
    private static Object XMLtoTNFCe(String fileXML) throws Exception {

        try {

            String strXML = new String(Files.readAllBytes(Paths.get(fileXML)));
            if (strXML.contains("nfeProc")) {
                JAXBContext jb = JAXBContext.newInstance(TNfeProc.class);
                Unmarshaller un = jb.createUnmarshaller();
                TNfeProc tnp = un.unmarshal(new StreamSource(new StringReader(strXML)), TNfeProc.class).getValue();
                return tnp;
            } else if (strXML.contains("NFe")) {
                JAXBContext jb = JAXBContext.newInstance(TNFe.class);
                Unmarshaller un = jb.createUnmarshaller();
                TNFe tnp = un.unmarshal(new StreamSource(new StringReader(strXML)), TNFe.class).getValue();
                return tnp;
            }else {
                return null;
            }

        } catch (JAXBException ex) {

            throw new Exception(ex.toString());
        }

    }

    //Salva o PDF gerado pelos Jasper Parameters
    private static void savePDF(NFCeJasperParameters jasperParameters, String fileXML, String pathToSavePDF) throws Exception {
        File localSalvar = new File("./PDFs/");
        if (!localSalvar.exists()) {
            localSalvar.mkdirs();
        }
        String strXML = new String(Files.readAllBytes(Paths.get(fileXML)));
        NFCeJasperPrinter nfceJasperPrinter = new NFCeJasperPrinter(strXML, jasperParameters);

        File arq = new File(pathToSavePDF);
        if(arq.exists()){
            arq.delete();
        }
        nfceJasperPrinter.printToPDFFile(pathToSavePDF);
    }

    //Testa se esta em contigencia e setta os Jasper Parameters
    private static void setAndGenerateJasperParameters(PrinterOptions printerOptions,  PrinterParameters parameters, NFCeJasperParameters jasperParameters, TNFe nfce, String path) throws Exception {
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
        savePDF(jasperParameters, path, "./PDFs/" + nfce.getInfNFe().getId() + "-consumidor.pdf");

        if (!parameters.getNfceContent().getInfNFe().getIde().getTpEmis().equals("1")) {
            jasperParameters.qrCodePath = parameters.getQrCodeContent();
            jasperParameters.qrCodeImage = QRCodeHelper.generateQRCodeToPNGStream(jasperParameters.qrCodePath);
            jasperParameters.isConsumerTicket = false;
           savePDF(jasperParameters, path, "./PDFs/" + nfce.getInfNFe().getId() + "-estabelecimento.pdf");
        }
        JOptionPane.showMessageDialog(null, "Geração de PDF feita com sucesso!!!");
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
