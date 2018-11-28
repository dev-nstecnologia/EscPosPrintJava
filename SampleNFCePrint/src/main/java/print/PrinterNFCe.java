package print;

import br.eti.ns.nsminiprinters.MiniPrinterModel;
import br.eti.ns.nsminiprinters.escpos.EscPosPrinter;
import br.eti.ns.nsminiprinters.escpos.PrinterOptions;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpec;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpecFactory;

import commons.*;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
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

        logger.debug("Iniciando impressão de CF-e em Mini Impressora");

        //Cria apartir do xml um objeto NFC-e
        Object nfce = XMLtoTNFCe(path);

        //Checa se o .xml é uma NFCe
        if(nfce==null){
            JOptionPane.showMessageDialog(null, "O XML informado não é uma NFCe, tente novamente");
            return;
        }

        //Setta as url, qrcode e protocolo dependendo do tipo de emissão: Autorizada ou Contigência
        if (nfce.getClass().equals(TNFe.class)) {
            TNFe tNfe = (TNFe) nfce;
            parameters.setNfceContent(tNfe);
            parameters.setUrlConsult(tNfe.getInfNFeSupl().getUrlChave());
            parameters.setQrCodeContent(tNfe.getInfNFeSupl().getQrCode());
            parameters.setAuthorizationProtocol("");
        } else {
            TNfeProc tNfeProc = (TNfeProc) nfce;
            parameters.setNfceContent(tNfeProc.getNFe());
            parameters.setUrlConsult(tNfeProc.getNFe().getInfNFeSupl().getUrlChave());
            parameters.setQrCodeContent(tNfeProc.getNFe().getInfNFeSupl().getQrCode());
            parameters.setAuthorizationProtocol(tNfeProc.getProtNFe().getInfProt().getNProt());
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
}
