package print;

import br.eti.ns.nsminiprinters.MiniPrinterModel;
import br.eti.ns.nsminiprinters.escpos.EscPosPrinter;
import br.eti.ns.nsminiprinters.escpos.PrinterOptions;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpec;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpecFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author matheus.mazzoni
 */
public class PrinterNFCe {

    private static ObjectMapper mapper;
    private final static Object nfeMapperLock = new Object();

    // Método que irá imprimir a NFC-e
    public static void printNFCe(String pathContent, PrinterParameters parameters, PrinterOptions printerOptions) throws Exception {
        System.out.print("\nComeçando a impressão...");
        String content = new String(Files.readAllBytes(Paths.get(pathContent)));
        Object nfce;

        if (!pathContent.contains(".xml") && pathContent.contains(".json")) {
            nfce = JSONToTNFe(content);
        }else{
            nfce = XMLToTNFe(content);
        }
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
    public static void generatePDF(String pathContent, String pathSavePDF, String pathLogo, NFCeJasperParameters parameters) throws Exception {

        System.out.print("Gerando PDF a partir de uma NFCe...\n");
        Object nfce;
        TNFe nota;
        String content = new String(Files.readAllBytes(Paths.get(pathContent)));

        if (!pathContent.contains(".xml") && pathContent.contains(".json")) {
            nfce = JSONToTNFe(content);
            content = TNFeToXML((TNFe) nfce).trim();
        } else {
            nfce = XMLToTNFe(content);
        }

        if (!Objects.equals(nfce, null)){

            if (nfce.getClass().equals(TNFe.class)) {
                nota = (TNFe) nfce;
                parameters.isProc = false;
            } else {
                TNfeProc tNFeProc = (TNfeProc) nfce;
                nota = tNFeProc.getNFe();
                parameters.isProc = true;
            }
            setAndGenerateJasperParameters(parameters, nota, pathLogo, true);
            savePDF(parameters, content, pathSavePDF, nota.getInfNFe().getId() + "-consumidor.pdf");

            if (!nota.getInfNFe().getIde().getTpEmis().equals("1")) {
                setAndGenerateJasperParameters(parameters, nota, pathLogo, false);
                savePDF(parameters, content, pathSavePDF,  nota.getInfNFe().getId() + "-estabelecimento.pdf");
            }
            JOptionPane.showMessageDialog(null, "Geração de PDF feita com sucesso!!!");
        } else {
            JOptionPane.showMessageDialog(null, "O XML informado não é uma NFCe, tente novamente");
            throw new Exception("XML Informado não é um NFCe!");
        }
    }

    private static TNFe JSONToTNFe(String nfeJson) {
        TNFe tnFe;
        try {
            nfeJson = getMapper().readTree(nfeJson).get("NFe").toString();
            tnFe = getMapper().readValue(nfeJson, TNFe.class);
        } catch (Exception e){
            System.out.println(e.getMessage());
            tnFe = null;
        }
        return tnFe;
    }

    private static ObjectMapper getMapper(){
        if (mapper == null){
            synchronized (nfeMapperLock){
                if (mapper == null) {
                    mapper = new ObjectMapper();
                    mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(mapper.getTypeFactory()));
                    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
                    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                }
            }
        }
        return mapper;
    }

    // Transforma o File XML em objeto TNFe ou TNFeProc
    private static Object XMLToTNFe(String strXML) throws Exception {

        Object tnp;
        try {
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

    private static String TNFeToXML(TNFe nfce) throws JAXBException {

        JAXBContext contextObj = JAXBContext.newInstance(TNFe.class);
        Marshaller marshallerObj = contextObj.createMarshaller();
        marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();
        marshallerObj.marshal(nfce, sw);
        return sw.toString();
    }

    // Testa se esta em contigencia e setta os Jasper Parameters
    private static void setAndGenerateJasperParameters(NFCeJasperParameters jasperParameters, TNFe nfce, String pathLogo, boolean isConsumerTicket) throws Exception {

        if(!jasperParameters.paperWidth.equals(NFCeJasperParameters.PAPERWIDTH.PAPER_58MM)) {
            jasperParameters.paperWidth = NFCeJasperParameters.PAPERWIDTH.PAPER_80MM;
        }
        jasperParameters.isConsumerTicket = isConsumerTicket;
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
    private static void savePDF(NFCeJasperParameters jasperParameters, String content, String pathToSavePDF, String nameArq) throws Exception {

        if(!pathToSavePDF.endsWith("\\")) pathToSavePDF += "\\";

        File localSalvar = new File(pathToSavePDF);
        if (!localSalvar.exists())localSalvar.mkdirs();

        NFCeJasperPrinter nfceJasperPrinter = new NFCeJasperPrinter(content, jasperParameters);

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
