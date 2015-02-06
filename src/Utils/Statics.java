/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author akhil
 */
public class Statics {

    public static void displayMsg(String toString) {
        JTextArea textArea = new JTextArea();
        toString = toString.replaceAll(",", "\t");
        textArea.setText(toString);
        textArea.setRows(40);
        textArea.setColumns(130);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.selectAll();
        
        //textArea.setLineWrap(true);
        //textArea.setWrapStyleWord(true);
        JScrollPane jspane = new JScrollPane(textArea);

        //JOptionPane.showMessageDialog(null, jspane, "Message:", JOptionPane.INFORMATION_MESSAGE);
        JOptionPane pane = new JOptionPane(jspane, JOptionPane.INFORMATION_MESSAGE);
        final JDialog d = pane.createDialog((JFrame) null, "Message:");
        d.setLocation(400, 75);

        try {
            d.setVisible(true);
        } catch (java.lang.IllegalStateException e1) {
            if (e1.getMessage().indexOf("cannot open system clipboard") == -1) {
                e1.printStackTrace();
            } else {

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
