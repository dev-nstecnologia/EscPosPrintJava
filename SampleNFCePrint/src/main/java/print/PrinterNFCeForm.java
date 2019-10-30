package print;

import br.eti.ns.nsminiprinters.escpos.PrinterOptions;
import commons.PrinterParameters;
import printjasper.NFCeJasperParameters;
import type.DetailType;
import type.ProductsLines;
import type.YesNoType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class PrinterNFCeForm extends JFrame{
    private JPanel panelMain;
    private JCheckBox boxDiscount;
    private JCheckBox boxDrawer;
    private JTextField txtPath;
    private JButton escolherArquivoButton;
    private JTextField txtPorta;
    private JComboBox cbImpressora;
    private JButton imprimirButton;
    private JComboBox cbSerial;
    private JComboBox cbLinhas;
    private JComboBox cbPapel;
    private JButton generatePDFButton;
    private JLabel Impressora;
    private JCheckBox boxDetail;
    private JTextField txtLogo;


    //Método que inicia o form
    public static void main(String[] args) {
        try {
            if(args[0].toUpperCase().equals("GERARPDFNFCE")){
                gerarPDF(args[1], args[2], args[3], args[4], args[5], Boolean.parseBoolean(args[6]), Boolean.parseBoolean(args[7]));
            } else if(args[0].toUpperCase().equals("IMPRIMIRNFCE")){
                imprimir(args[1], args[2], Integer.parseInt(args[3]), args[4], args[5], args[6], Boolean.parseBoolean(args[7]),
                        Boolean.parseBoolean(args[8]), Boolean.parseBoolean(args[9]));
            }
            System.out.print(Arrays.toString(args));
        } catch (Exception e){
            EventQueue.invokeLater(() -> {
                try{
                    PrinterNFCeForm frame = new PrinterNFCeForm();
                    frame.setResizable(false);
                    frame.setContentPane(new PrinterNFCeForm().panelMain);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            });
            e.printStackTrace();
        }
    }

    public PrinterNFCeForm() {
        escolherArquivoButton.addActionListener(e -> {

            //Cria um file chooser que so seleciona files
            JFileChooser jFC = new JFileChooser();
            jFC.setDialogTitle("Selecione XML");
            jFC.setFileSelectionMode(JFileChooser.FILES_ONLY);

            //Cria um filtro para somente pegar .xml
            FileNameExtensionFilter filter = new FileNameExtensionFilter(".xml .json", "xml", "json");
            jFC.setFileFilter(filter);

            //Abre uma janela para ser pego o arquivo
            int retornoSelect = jFC.showOpenDialog(escolherArquivoButton);

            //Testa se o arquivo é aprovado pelo filtro
            if(retornoSelect == JFileChooser.APPROVE_OPTION){

                //Pega o path do .xml escolhido
                File file = jFC.getSelectedFile();
                txtPath.setText(file.getPath());
            }
        });

        imprimirButton.addActionListener(e -> {

            imprimir(cbImpressora.getSelectedItem().toString(), txtPorta.getText(),
                    Integer.parseInt(cbSerial.getSelectedItem().toString()), txtPath.getText(),
                    cbPapel.getSelectedItem().toString(),
                    cbLinhas.getSelectedItem().toString(), boxDetail.isSelected(), boxDiscount.isSelected(), boxDrawer.isSelected());
        });

        generatePDFButton.addActionListener(e -> {

            gerarPDF(txtPath.getText(), "./PDFs/",  txtLogo.getText(), cbPapel.getSelectedItem().toString(), cbLinhas.getSelectedItem().toString(), boxDiscount.isSelected(), boxDetail.isSelected());
        });
    }

    private static void imprimir(String printer, String port, int portSpeed, String pathXML, String paper, String lines, boolean layout, boolean hasDiscount, boolean hasDrawer){
        PrinterOptions printerOptions = setPrinterOptions(port, portSpeed, paper);
        PrinterParameters parameters = setPrinterParameters(printer, layout, lines, hasDiscount, hasDrawer);
        try {
            PrinterNFCe.printNFCe(pathXML, parameters, printerOptions);
        } catch (Exception e1 ) {
            e1.printStackTrace();
        }
    }

    // Define as opções da impressora
    private static PrinterOptions setPrinterOptions(String port, int portSpeed, String paper){

        PrinterOptions printerOptions = new PrinterOptions();
        printerOptions.port = port;
        printerOptions.portSpeed = portSpeed;
        paperPrinterOptions(paper, printerOptions);

        return printerOptions;
    }

    // Define os parametros de impressao
    private static PrinterParameters setPrinterParameters(String printer, boolean layout, String lines, boolean hasDiscount, boolean hasDrawer){
        PrinterParameters parameters = new PrinterParameters();
        parameters.setPrinterName(printer);
        detailPrinterParameters(layout, parameters);
        linesPrinterParameters(lines, parameters);
        drawDiscPrinterParameters(hasDiscount, hasDrawer, parameters);

        return parameters;
    }

    // Verifica o tipo de impressão
    private static void detailPrinterParameters(boolean layout, PrinterParameters parameters){

        if (layout){
            parameters.setPrintDetail(DetailType.NORMAL);
        } else {
            parameters.setPrintDetail(DetailType.ECOLOGICA);
        }
        parameters.setPrintLayoutIsNormal(layout);
    }

    // Verifica quantas linhas será dividida a parte dos produtos
    private static void linesPrinterParameters(String lines, PrinterParameters parameters){
        if(lines.equals("1 Linha") || lines.equals("1")){
            parameters.setPrintProductsLines(ProductsLines.UMA_LINHA);
        }else if(lines.equals("2 Linhas")|| lines.equals("2")){
            parameters.setPrintProductsLines(ProductsLines.DUAS_LINHAS);
        }else{
            parameters.setPrintProductsLines(ProductsLines.LINHAS_DINAMICAS);
        }

    }

    // Verifica se será impresso o desconto, caso tenha, e se abrirá a gaveta ápos a impressão
    private static void drawDiscPrinterParameters(boolean boxDesc, boolean boxGav, PrinterParameters parameters){
        if (boxDesc){
            parameters.setPrintProductsDiscount(YesNoType.YES);
        } else {
            parameters.setPrintProductsDiscount(YesNoType.NO);
        }

        if (boxGav){
            parameters.setOpenDrawer(YesNoType.YES);
        } else {
            parameters.setOpenDrawer(YesNoType.NO);
        }

    }

    // Verifica qual o tamanho do papel utilizado pela impressora
    private static void paperPrinterOptions(String paper, PrinterOptions printerOptions){
        if(paper.equals("58mm") || paper.equals("55")){
            printerOptions.paperWidth = PrinterOptions.PAPERWIDTH.PAPER_58MM;
        } else {
            printerOptions.paperWidth = PrinterOptions.PAPERWIDTH.PAPER_80MM;
        }

    }




    private static void gerarPDF(String pathConteudo, String pathSavePDF, String pathLogo, String paper, String lines, boolean hasDiscount, boolean hasDetail) {

       NFCeJasperParameters parameters = setJasperParameters(paper, lines, hasDiscount, hasDetail);
        try {
            PrinterNFCe.generatePDF(pathConteudo, pathSavePDF, pathLogo, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Define os parametros de configuração do PDF
    private static NFCeJasperParameters setJasperParameters(String paper, String lines, boolean hasDiscount, boolean hasDetail){

        NFCeJasperParameters parameters = new NFCeJasperParameters();
        parameters.printItemDiscount = hasDiscount;
        parameters.printDetail = hasDetail;
        if (parameters.printDetail) {
            linesJasperParameters(lines, parameters);
        }
        paperJasperParameters(paper, parameters);

        return parameters;
    }

    // Define o papel que será utilizado para molde do PDF
    private static void paperJasperParameters(String paper, NFCeJasperParameters parameters){
        if (paper.equals("58mm") || paper.equals("58")){
            parameters.paperWidth = NFCeJasperParameters.PAPERWIDTH.PAPER_58MM;

        } else {
            parameters.paperWidth = NFCeJasperParameters.PAPERWIDTH.PAPER_80MM;
        }
    }

    // Determina quantas linhas terá as linhas dos produtos
    private static void linesJasperParameters(String lines, NFCeJasperParameters parameters){
        if (lines.equals("1 Linha") || lines.equals("1")) {
            parameters.printItemsLines = 1;
        } else if (lines.equals("2 Linhas") || lines.equals("2")) {
            parameters.printItemsLines = 2;
        } else {
            parameters.printItemsLines = 3;
        }
    }

}
