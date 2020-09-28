/*
 * Copyright 2014 - 2017 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognizant.cognizantits.ide.main.utils.table;

import com.cognizant.cognizantits.datalib.component.TestStep;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 *
 * 
 */
public class WebservicePayloadArea extends javax.swing.JDialog {

    TestStep currentStep;
    DefaultCompletionProvider provider;

    public WebservicePayloadArea(java.awt.Frame parent, TestStep step, String stepType, List<String> searchStr) {
        super(parent);
        this.currentStep = step;
        initComponents();
        if(stepType.equals("SOAP"))
        {
        installAutoComplete(searchStr, SyntaxConstants.SYNTAX_STYLE_XML);
        setTitle("XML Payload Editor");
        }
        if(stepType.equals("REST"))
        {
        installAutoComplete(searchStr, SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS);
        setTitle("JSON Payload Editor");
        }
        jTextArea1.setText(step.getInput());
        setLocationRelativeTo(parent);
        initCloseListener();
        setVisible(true);
    }

    private void initCloseListener() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional.ofNullable(jTextArea1.getText())
                        .filter((val) -> !val.trim().isEmpty())
                        .map((val) -> val.startsWith("@") ? val : val)
                        .ifPresent(currentStep::setInput);
                dispose();
            }
        });
    }

    private void installAutoComplete(List<String> searchStr, String syntaxConstants ) {
        ((RSyntaxTextArea) jTextArea1).setCodeFoldingEnabled(true);
        ((RSyntaxTextArea) jTextArea1).setSyntaxEditingStyle(syntaxConstants);
        provider = new DefaultCompletionProvider();
        setSearchString(searchStr);
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(jTextArea1);
    }

    private void setSearchString(List<String> searchString) {
        searchString.stream().forEach((string) -> {
            provider.addCompletion(new ShorthandCompletion(provider, string, "{" + string + "}"));
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new RSyntaxTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setUndecorated(true);

        jTextArea1.setBackground(new java.awt.Color(255, 255, 204));
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
