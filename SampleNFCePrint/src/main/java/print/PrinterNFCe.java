package print;

import br.eti.ns.nsminiprinters.MiniPrinterModel;
import br.eti.ns.nsminiprinters.escpos.EscPosPrinter;
import br.eti.ns.nsminiprinters.escpos.PrinterOptions;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpec;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpecFactory;

import commons.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;

import jssc.SerialPortException;
import org.apache.commons.io.FileUtils;
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

    // Método que irá imprimir a NFC-e
    public static void printNFCe(String path, PrinterParameters parameters, PrinterOptions printerOptions) throws Exception {
        System.out.print("\nComeçando a impressão...");
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

    // Método que gera uma NFC-e em PDF
    public static void generatePDF(String path, String pathSavePDF, String pathLogo, NFCeJasperParameters parameters) throws Exception {
        System.out.print("\nGerando PDF a partir de uma NFCe...");

        Object nfce = XMLtoTNFCe(path);

        if (!Objects.equals(nfce, null)){
            TNFe nota;
            if (nfce.getClass().equals(TNFe.class)) {
                nota = (TNFe) nfce;
                parameters.isProc = false;
            } else {
                TNfeProc tNFeProc = (TNfeProc) nfce;
                nota = tNFeProc.getNFe();
                parameters.isProc = true;
            }
            setAndGenerateJasperParameters(parameters, nota, pathLogo);
            parameters.isConsumerTicket = true;
            savePDF(parameters, path, pathSavePDF, nota.getInfNFe().getId() + "-consumidor.pdf");

            if (!nota.getInfNFe().getIde().getTpEmis().equals("1")) {
                setAndGenerateJasperParameters(parameters, nota, pathLogo);
                parameters.isConsumerTicket = false;
                savePDF(parameters, path, pathSavePDF,  nota.getInfNFe().getId() + "-estabelecimento.pdf");
            }
            JOptionPane.showMessageDialog(null, "Geração de PDF feita com sucesso!!!");
        } else {
            JOptionPane.showMessageDialog(null, "O XML informado não é uma NFCe, tente novamente");
        }
    }

    // Transforma o File XML em objeto TNFe ou TNFeProc
    private static Object XMLtoTNFCe(String fileXML) throws Exception {

        Object tnp;
        try {
            String strXML = new String(Files.readAllBytes(Paths.get(fileXML)));
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

    // Testa se esta em contigencia e setta os Jasper Parameters
    private static void setAndGenerateJasperParameters(NFCeJasperParameters jasperParameters, TNFe nfce, String pathLogo) throws Exception {

        if(!jasperParameters.paperWidth.equals(NFCeJasperParameters.PAPERWIDTH.PAPER_58MM)) {
            jasperParameters.paperWidth = NFCeJasperParameters.PAPERWIDTH.PAPER_80MM;
        }

        jasperParameters.qrCodePath = nfce.getInfNFeSupl().getQrCode();
        jasperParameters.urlConsulta = nfce.getInfNFeSupl().getUrlChave();
        jasperParameters.qrCodeImage = QRCodeHelper.generateQRCodeToPNGStream(jasperParameters.qrCodePath);
        jasperParameters.itemsCount = nfce.getInfNFe().getDet().size();

        if(nfce.getInfNFe().getPag().getVTroco() != null){
            jasperParameters.valueTroco = nfce.getInfNFe().getPag().getVTroco();
            double vRecebido = 0;
            for (TNFe.InfNFe.Pag.DetPag detPag : nfce.getInfNFe().getPag().getDetPag()) {
                vRecebido += Double.parseDouble(detPag.getVPag());
            }
            jasperParameters.valueRec = String.valueOf(vRecebido);
        } else {
            for (TNFe.InfNFe.InfAdic.ObsCont obsCont : nfce.getInfNFe().getInfAdic().getObsCont()) {
                if ("valorRec".equals(obsCont.getXCampo())) {
                    jasperParameters.valueRec = obsCont.getXTexto();
                } else if ("valorTroco".equals(obsCont.getXCampo())) {
                    jasperParameters.valueTroco = obsCont.getXTexto();
                }
            }
        }
//        if (!pathLogo.equals("")) {
//            jasperParameters.logoEmit = encodeFileToBase64Binary(pathLogo);
//        }
    }

    // Transforma um File em Base64
    private static String encodeFileToBase64Binary(String filePath){
        String logo = null;
        try {
            byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
            logo =  Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logo;
    }

    // Salva o PDF gerado pelos Jasper Parameters
    private static void savePDF(NFCeJasperParameters jasperParameters, String fileXML, String pathToSavePDF, String nameArq) throws Exception {

        if(!pathToSavePDF.endsWith("\\")) pathToSavePDF += "\\";

        File localSalvar = new File(pathToSavePDF);
        if (!localSalvar.exists())localSalvar.mkdirs();

        String strXML = new String(Files.readAllBytes(Paths.get(fileXML)));
        NFCeJasperPrinter nfceJasperPrinter = new NFCeJasperPrinter(strXML, jasperParameters);

        File arq = new File(pathToSavePDF + nameArq);
        if(arq.exists()) arq.delete();
        System.out.print("\nSalvando seu PDF ...");
        nfceJasperPrinter.printToPDFFile(pathToSavePDF+ nameArq);
    }

    // Setta os Prints Parameters NFCe
    private static void setPrinterParameters(PrinterParameters parameters, TNFe tNfe){
        parameters.setNfceContent(tNfe);
        parameters.setUrlConsult(tNfe.getInfNFeSupl().getUrlChave());
        parameters.setQrCodeContent(tNfe.getInfNFeSupl().getQrCode());
        parameters.setAuthorizationProtocol("");
    }

    // Setta os Prints Parameters com NFCeProc
    private static void setPrinterParameters(PrinterParameters parameters, TNfeProc tNfe){
        parameters.setNfceContent(tNfe.getNFe());
        parameters.setUrlConsult(tNfe.getNFe().getInfNFeSupl().getUrlChave());
        parameters.setQrCodeContent(tNfe.getNFe().getInfNFeSupl().getQrCode());
        parameters.setAuthorizationProtocol(tNfe.getProtNFe().getInfProt().getNProt());
    }

}
