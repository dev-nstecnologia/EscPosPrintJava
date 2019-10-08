package print;

import br.eti.ns.nsminiprinters.escpos.PrinterOptions;
import commons.PrinterParameters;
import type.DetailType;
import type.ProductsLines;
import type.YesNoType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class PrinterNFCeForm {
    private JPanel panelMain;
    private JCheckBox boxDiscount;
    private JCheckBox boxDrawer;
    private JLabel Impressora;
    private JTextField txtPath;
    private JButton escolherArquivoButton;
    private JTextField txtPorta;
    private JComboBox cbImpressora;
    private JButton imprimirButton;
    private JComboBox cbSerial;
    private JComboBox cbLinhas;
    private JComboBox cbPapel;
    private JComboBox cbLayout;

    public PrinterNFCeForm() {
        escolherArquivoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Cria um file chooser que so seleciona files
                JFileChooser jFC = new JFileChooser();
                jFC.setDialogTitle("Selecione XML");
                jFC.setFileSelectionMode(JFileChooser.FILES_ONLY);

                //Cria um filtro para somente pegar .xml
                FileNameExtensionFilter filter = new FileNameExtensionFilter(".xml", "xml");
                jFC.setFileFilter(filter);

                //Abre uma janela para ser pego o arquivo
                int retornoSelect = jFC.showOpenDialog(escolherArquivoButton);

                //Testa se o arquivo é aprovado pelo filtro
                if(retornoSelect == JFileChooser.APPROVE_OPTION){

                    //Pega o path do .xml escolhido
                    File file = jFC.getSelectedFile();
                    txtPath.setText(file.getPath());
                }
            }
        });
        imprimirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Coloca todos os valores nas variaveis
                String printer = cbImpressora.getSelectedItem().toString();
                String port = txtPorta.getText();
                int portSpeed = Integer.parseInt(cbSerial.getSelectedItem().toString());
                String pathXML = txtPath.getText();
                String paper = cbPapel.getSelectedItem().toString();
                String layout = cbLayout.getSelectedItem().toString();
                String lines = cbLinhas.getSelectedItem().toString();

                //Testa se os campos estão preenchidos
                if(port.equals("") || pathXML.equals("")){
                    JOptionPane.showMessageDialog(null, "Todos os campos devem ser preenchidos!");
                    return;
                }

                //Cria os parametros, o printer e uma via de consumidor
                PrinterParameters parameters = new PrinterParameters();
                PrinterOptions printerOptions = new PrinterOptions();


                //Seta o nome da impressora, a velociade da porta e qual porta é utilizada
                parameters.setPrinterName(printer);
                printerOptions.port = port;
                printerOptions.portSpeed = portSpeed;


                //Chama funções de verificação
                detailPrinter(layout, parameters);
                linesPrinter(lines, parameters);
                drawDiscPrinter(boxDiscount.isSelected(), boxDrawer.isSelected(), parameters);
                paperPrinter(paper, printerOptions);

                //Chama o método que irá imprimir através dos dados settados
                try {
                    PrinterNFCe.printNFCe(pathXML, parameters, printerOptions);
                } catch (Exception e1 ) {
                    e1.printStackTrace();
                }
            }

        });
    }

    //Verifica o tipo de impressão
    public static void detailPrinter(String layout, PrinterParameters parameters){

        if(layout.equalsIgnoreCase("Normal")){
            parameters.setPrintDetail(DetailType.NORMAL);
            parameters.setPrintLayoutIsNormal(true);
        }else{
            parameters.setPrintDetail(DetailType.ECOLOGICA);
            parameters.setPrintLayoutIsNormal(false);
        }
    }

    //Verifica quantas linhas será dividida a parte dos produtos
    public static void linesPrinter(String lines, PrinterParameters parameters){
        if(lines.equals("1 Linha")){
            parameters.setPrintProductsLines(ProductsLines.UMA_LINHA);
        }else if(lines.equals("2 Linhas")){
            parameters.setPrintProductsLines(ProductsLines.DUAS_LINHAS);
        }else{
            parameters.setPrintProductsLines(ProductsLines.LINHAS_DINAMICAS);
        }

    }

    //Verifica se será impresso o desconto, caso tenha, e se abrirá a gaveta ápos a impressão
    public static void drawDiscPrinter(boolean boxDesc, boolean boxGav, PrinterParameters parameters){
        if(boxDesc){
            parameters.setPrintProductsDiscount(YesNoType.YES);
        }else{
            parameters.setPrintProductsDiscount(YesNoType.NO);
        }

        if(boxGav){
            parameters.setOpenDrawer(YesNoType.YES);
        }else{
            parameters.setOpenDrawer(YesNoType.NO);
        }

    }

    //Verifica qual o tamanho do papel utilizado pela impressora
    public static void paperPrinter(String paper, PrinterOptions printerOptions){
        if(paper.equals("58mm")){
            printerOptions.paperWidth = PrinterOptions.PAPERWIDTH.PAPER_58MM;

        }else{
            printerOptions.paperWidth = PrinterOptions.PAPERWIDTH.PAPER_80MM;
        }

    }

    //Método que inicia o form
    public static void main(String args[]) {
        JFrame frame = new JFrame("PrinterNFCeForm");
        frame.setResizable(false);
        frame.setContentPane( new PrinterNFCeForm().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
