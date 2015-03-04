package com.oleg;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;

public class FormNew {
    private static final String testMessage = "427=0\u00018=FIX.4.4\u00019=73\u000135=0\u000149=FXTRADE-QUOTE\u000156=CITIFX\u000134=27\u000152=20130509-21:04:01.388\u000157=FXSpot\u000110=139\u0001]\n";

    private JTextField fixTextField;
    private JButton browseLogButton;
    private JTextField fixDictTextField;
    private JButton processButton;
    private JPanel mPanel;
    private JTable fieldsTable;
    private JList messagesList;
    private JTextField dividerTextField;
    private JButton browseDictButton;
    private JButton xButton;
    private JTextField singleLineTextField;

    final MyTableModel tableModel = new MyTableModel();
    final DefaultListModel<Message> listModel = new DefaultListModel<Message>();

    private final JFileChooser fileChooser = new JFileChooser();

    private JFrame mFrame;
    public FormNew(JFrame frame) {
        super();
        init();
        mFrame = frame;
        browseLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    fileChooser.setCurrentDirectory(new File(fixTextField.getText()));
                }catch (Exception ex){}
                int retVal = fileChooser.showOpenDialog(mPanel);
                if (retVal == JFileChooser.APPROVE_OPTION){
                    File file = fileChooser.getSelectedFile();
                    String filePath = file.getAbsolutePath();

                    Properties p = loadProperties();
                    p.setProperty("logfile", file.getAbsolutePath());
                    saveProperties(p);

                    fixTextField.setText(filePath);
                    tableModel.clear();
                    try {
                        InputStream is;
                        is = new FileInputStream(filePath);
                        listModel.clear();
                        readMessages(is);
                    } catch (FileNotFoundException e1) {
                        fixDictTextField.setText("File: " + filePath + " not found");
                    }
                }
            }
        });

        browseDictButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fileChooser.setCurrentDirectory(new File(fixDictTextField.getText()));
                int retVal = fileChooser.showOpenDialog(mPanel);
                if (retVal == JFileChooser.APPROVE_OPTION){
                    messages = null;
                    File file = fileChooser.getSelectedFile();
                    fixDictTextField.setText(file.getAbsolutePath());
                    Properties p = loadProperties();
                    p.setProperty("dictionary", file.getAbsolutePath());
                    saveProperties(p);
                }
            }
        });

        processButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String value = singleLineTextField.getText();
                if (value == null || value.trim().isEmpty())
                    return;
                tableModel.clear();
                Message msg = new Message(value);
                listModel.addElement(msg);
                messagesList.setSelectedIndex(listModel.size()-1);
                messagesList.ensureIndexIsVisible(listModel.size() - 1);
            }
        });

        messagesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                if (messagesList.getSelectedIndices().length == 0)
                    return;
                try{
                    int index = messagesList.getSelectedIndices()[0];
                    Message msg = listModel.get(index);
                    tableModel.clear();
                    if (messages == null){
                        try{
                            readFixDictFile(fixDictTextField.getText());
                        }catch (Exception ex){
                            fixDictTextField.setText("Cant read file: " + fixDictTextField.getText());
                            throw  ex;
                        }
                    }
                    new MySwingWorker(msg.getMsg(), dividerTextField.getText(), tableModel, messages, fields, mFrame)
                            .execute(); //loadFields
                }catch (Exception ex){
                    System.out.println("List selection error: " + ex.getMessage());
                }

            }
        });

        xButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                singleLineTextField.setText("");
            }
        });
    }

    Node messages, fields;
    private void readFixDictFile(String filePath) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File file = new File(filePath);
        Document doc = builder.parse(file);
        NodeList nodes = doc.getFirstChild().getChildNodes();
        System.out.println(nodes.getLength());
        for (int i=0; i< nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equals("messages")){
                messages = node;
            }
            if (node.getNodeName().equals("fields")){
                fields = node;
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("FixViewer");
        frame.setContentPane(new FormNew(frame).mPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        //frame.pack();
        frame.setVisible(true);
    }

    private Properties loadProperties(){
        Properties properties = new Properties();
        try {
            properties.load(new FileReader("fixviewer.properties"));
        }catch (Exception ex){
            properties.setProperty("divider", "\\u0001");
            properties.setProperty("dictionary", "FIX44.xml");
            properties.setProperty("logfile", "C:\\a2b\\a2b.1112.derivatives.actforex.asd\\log\\derivativesRates.log");
            saveProperties(properties);
        }
        return properties;
    }

    private void saveProperties(Properties properties){
        try {
            FileWriter fw = new FileWriter("fixviewer.properties");
            properties.store(fw, "test comment");
        }catch (Exception ex){}
    }


    private void init(){
        Properties properties = loadProperties();
        tableModel.addColumn("tag");
        tableModel.addColumn("tag name");
        tableModel.addColumn("value name");
        tableModel.addColumn("value");
        fieldsTable.setModel(tableModel);
        listModel.addElement(new Message(testMessage));
        messagesList.setModel(listModel);
        messagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messagesList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        dividerTextField.setText(properties.getProperty("divider"));
        fixDictTextField.setText(properties.getProperty("dictionary"));
        fixTextField.setText(properties.getProperty("logfile"));
        fixDictTextField.setEnabled(false);
        fieldsTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        fieldsTable.getColumnModel().getColumn(0).setMaxWidth(40);

    }



    private void readMessages(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while (true){
                line = reader.readLine();
                if (line == null)
                    break;
                listModel.addElement(new Message(line));
            }
        } catch (IOException ignored) {

        } finally {
            try {
                is.close();
            } catch (IOException e) {}
        }
    }

    class MyTableModel extends DefaultTableModel {
        public void clear(){
            dataVector.removeAllElements();
            int last = dataVector.size();
            fireTableRowsDeleted(0, last);
        }
    }
}
