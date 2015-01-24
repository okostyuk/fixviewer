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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class FormNew {
    private JTextField fixTextField;
    private JButton browseLogButton;
    private JTextField fixDictTextField;
    private JButton processButton;
    private JPanel mPanel;
    private JTable fieldsTable;
    private JList messagesList;
    private JTextField dividerTextField;
    private JButton browseDictButton;

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
                int retVal = fileChooser.showOpenDialog(mPanel);
                if (retVal == JFileChooser.APPROVE_OPTION){
                    File file = fileChooser.getSelectedFile();
                    fixTextField.setText(file.getAbsolutePath());
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
                }
            }
        });

        processButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tableModel.clear();
                String value = fixTextField.getText();
                InputStream is;
                try {
                    is = new FileInputStream(value);
                    listModel.clear();
                    readMessages(is);
                } catch (FileNotFoundException e1) {
                    listModel.addElement(new Message(value));
                }
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


    private void init(){
        tableModel.addColumn("id");
        tableModel.addColumn("name");
        tableModel.addColumn("value");
        tableModel.addColumn("valueName");
        fieldsTable.setModel(tableModel);
        listModel.addElement(new Message("427=0\u00018=FIX.4.4\u00019=73\u000135=0\u000149=FXTRADE-QUOTE\u000156=CITIFX\u000134=27\u000152=20130509-21:04:01.388\u000157=FXSpot\u000110=139\u0001]\n"));
        messagesList.setModel(listModel);
        messagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dividerTextField.setText("\\u0001");
        fixDictTextField.setText("C:\\src\\FixViewer\\src\\FIX44.xml");
        fixDictTextField.setEnabled(false);


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