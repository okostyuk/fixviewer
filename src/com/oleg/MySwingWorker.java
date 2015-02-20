package com.oleg;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.ArrayList;
import java.util.HashMap;

class MySwingWorker extends SwingWorker<ArrayList<Field>, Object> {
    private final String msg;
    private final String divider;
    private final DefaultTableModel tableModel;
    private final Node messages, fields;
    JFrame mFrame;

    MySwingWorker(String msg, String divider, DefaultTableModel tableModel, Node messages, Node fields, JFrame mFrame) {
        this.msg = msg;
        this.divider = divider;
        this.tableModel = tableModel;
        this.messages = messages;
        this.fields = fields;
        this.mFrame = mFrame;
    }

    @Override
    protected ArrayList<Field> doInBackground() throws Exception {
        String[] fields = msg.split(divider);
        ArrayList<Field> fieldsList = new ArrayList<Field>();
        String fieldId, fieldName, value, valueName;
        for (String field : fields) {
            try {
                String[] subfields = field.split("=");
                fieldId = subfields[0];
                value = subfields[1];
                fieldName = getFieldName(fieldId);
                valueName = getValueName(fieldId, value);
                fieldsList.add(new Field(fieldId, fieldName, value, valueName));
            } catch (Exception ex) {
                System.out.println("loadFields Error for field:" + field + "\t" + ex.getMessage());
            }
        }
        return fieldsList;
    }



    @Override
    protected void done() {
        try {
            ArrayList<Field> res = get();
            for (Field field : res){
                String[] tableRow = {field.id, field.name, field.valueName, field.value};
                tableModel.addRow(tableRow);
            }
        }catch (Exception ex){
            System.out.println("SW Error " + "\t" + ex.getMessage());
        }
    }

    private String getValueName(String field, String value) {
        try {
            Node node = getField(field);
            NodeList nodes = node.getChildNodes();
            Node valueNode;
            NamedNodeMap attr;
            for (int i = 0; i < nodes.getLength(); i++) {
                valueNode = nodes.item(i);
                attr = valueNode.getAttributes();
                if (attr == null)
                    continue;
                for (int j = 0; j < attr.getLength(); j++) {
                    try {
                        if (attr.getNamedItem("enum").getNodeValue().equals(value)) {
                            return attr.getNamedItem("description").getNodeValue();
                        }
                    } catch (Exception ex) {

                    }
                }
            }
        }catch (Exception ex){}
        return value;
    }



    String getFieldName(String id){
        String res = "unknown";
        try {
            Node field = getField(id);
            if (field != null) {
                res = field.getAttributes().getNamedItem("name").getNodeValue();
            }
        }catch (Exception ex){}
        return res;
    }

    private Node getField(String id) {
        NodeList nodes = fields.getChildNodes();
        NamedNodeMap map;
        Node node, field, res = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            field = nodes.item(i);
            map = field.getAttributes();
            if (map != null) {
                node = map.getNamedItem("number");
                if (node != null && node.getNodeValue().equals(id)) {
                    res = field;
                    break;
                }
            }
        }
        return res;
    }

}
