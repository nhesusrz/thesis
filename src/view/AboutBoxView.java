/**
 * Copyright (C) 2014 Martín Pacheco.
 *
 * This file is part of my Thesis aplication called "Extraction and Analysis
 * System of Topics for Software History Reports". Faculty of Exact Sciences of
 * the UNICEN University. Tandil, Argentine. http://www.exa.unicen.edu.ar/
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Email: mpacheco@alumnos.exa.unicen.edu.ar
 *
 * @author Martín Pacheco
 */
package view;

import org.jdesktop.application.Action;

public class AboutBoxView extends javax.swing.JDialog {

    public AboutBoxView(java.awt.Frame parent) {
        super(parent);
        initComponents();
        getRootPane().setDefaultButton(closeButton);
    }

    @Action
    public void closeAboutBox() {
        dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        javax.swing.JLabel versionLabel = new javax.swing.JLabel();
        javax.swing.JLabel appVersionLabel = new javax.swing.JLabel();
        javax.swing.JLabel vendorLabel = new javax.swing.JLabel();
        javax.swing.JLabel appVendorLabel = new javax.swing.JLabel();
        javax.swing.JLabel appDescLabel = new javax.swing.JLabel();
        javax.swing.JLabel imageLabel = new javax.swing.JLabel();
        javax.swing.JLabel versionLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel appVersionLabel1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel versionLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel appVersionLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel appHomepageLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel appTitleLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("view/Bundle"); // NOI18N
        setTitle(bundle.getString("AboutBoxView.Title")); // NOI18N
        setModal(true);
        setName("aboutBox"); // NOI18N
        setResizable(false);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(view.MainApp.class).getContext().getActionMap(AboutBoxView.class, this);
        closeButton.setAction(actionMap.get("closeAboutBox")); // NOI18N
        closeButton.setText(bundle.getString("AboutBoxView.closeButton")); // NOI18N
        closeButton.setName("closeButton"); // NOI18N

        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        versionLabel.setText(bundle.getString("AboutBoxView.directorLabel")); // NOI18N
        versionLabel.setName("versionLabel"); // NOI18N

        appVersionLabel.setText(bundle.getString("AboutBoxView.director")); // NOI18N
        appVersionLabel.setName(""); // NOI18N

        vendorLabel.setFont(vendorLabel.getFont().deriveFont(vendorLabel.getFont().getStyle() | java.awt.Font.BOLD));
        vendorLabel.setText(bundle.getString("AboutBoxView.authorLabel")); // NOI18N
        vendorLabel.setName("vendorLabel"); // NOI18N

        appVendorLabel.setText(bundle.getString("AboutBoxView.author")); // NOI18N
        appVendorLabel.setName("appVendorLabel"); // NOI18N

        appDescLabel.setText(bundle.getString("AboutBoxView.appDescLabel")); // NOI18N
        appDescLabel.setName("appDescLabel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(view.MainApp.class).getContext().getResourceMap(AboutBoxView.class);
        imageLabel.setIcon(resourceMap.getIcon("imageLabel.icon")); // NOI18N
        imageLabel.setName("imageLabel"); // NOI18N

        versionLabel1.setFont(versionLabel1.getFont().deriveFont(versionLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        versionLabel1.setText(bundle.getString("AboutBoxView.appCarrerLabel")); // NOI18N
        versionLabel1.setName("versionLabel1"); // NOI18N

        appVersionLabel1.setText(bundle.getString("AboutBoxView.appCarrer")); // NOI18N
        appVersionLabel1.setName("appVersionLabel1"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(bundle.getString("AboutBoxView.authorMail")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        versionLabel2.setFont(versionLabel2.getFont().deriveFont(versionLabel2.getFont().getStyle() | java.awt.Font.BOLD));
        versionLabel2.setText(bundle.getString("AboutBoxView.coDirectorLabel")); // NOI18N
        versionLabel2.setName("versionLabel2"); // NOI18N

        appVersionLabel2.setText(bundle.getString("AboutBoxView.coDirector")); // NOI18N
        appVersionLabel2.setName("appVersionLabel2"); // NOI18N

        appHomepageLabel1.setFont(resourceMap.getFont("appHomepageLabel1.font")); // NOI18N
        appHomepageLabel1.setText(bundle.getString("AboutBoxView.coDirectorMail")); // NOI18N
        appHomepageLabel1.setName("appHomepageLabel1"); // NOI18N

        appTitleLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        appTitleLabel1.setText(bundle.getString("AboutBoxView.appTitleLabel1")); // NOI18N
        appTitleLabel1.setName("appTitleLabel1"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(bundle.getString("AboutBoxView.directorMail")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(appDescLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(versionLabel2)
                                .addComponent(versionLabel)
                                .addComponent(vendorLabel)
                                .addComponent(versionLabel1))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(appVersionLabel1)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(appVendorLabel)
                                        .addComponent(appVersionLabel2)
                                        .addComponent(appVersionLabel))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(appHomepageLabel1)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel2)))))
                        .addComponent(appTitleLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(closeButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(appTitleLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(appDescLabel)
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel1)
                    .addComponent(appVersionLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vendorLabel)
                    .addComponent(appVendorLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(appVersionLabel)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel2)
                    .addComponent(appVersionLabel2)
                    .addComponent(appHomepageLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
            .addComponent(imageLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

}
