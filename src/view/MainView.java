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

import algorithms.AlgorithmManager;
import charts.PieChart;
import charts.XYSplineChart;
import charts.data.BugCommentDistributionGenerator;
import charts.data.BugComponentDistributionGenerator;
import charts.data.MetricEvolutionGenerator;
import dao.CommentDAO;
import dao.DAOManager;
import dao.DAONameEnum;
import dao.DocumentDAO;
import dao.TermDAO;
import dao.TopicDAO;
import dao.VersionDAO;
import dto.BugDTO;
import dto.CommentDTO;
import dto.DocumentDTO;
import dto.TopicDTO;
import dto.VersionDTO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultCaret;
import logger.ThesisLogger;
import lucene.ClusteringAlgorithm;
import lucene.LuceneManager;
import metrics.Assigment;
import metrics.Focus;
import metrics.Metric;
import metrics.Scattering;
import metrics.Weight;
import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.stc.STCClusteringAlgorithm;
import org.carrot2.clustering.synthetic.ByFieldClusteringAlgorithm;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;
import org.jfree.ui.ExtensionFileFilter;
import util.PropertiesApp;
import util.ParametersEnum;
import util.TextAreaRenderer;

/**
 * The application's main frame.
 */
public class MainView extends FrameView implements Observer {

    public MainView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        AlgorithmManager.getInstance().addObserver(this);
        DAOManager.getInstance().addObserver(this);
        iniElementsView();        
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = MainApp.getApplication().getMainFrame();
            aboutBox = new AboutBoxView(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        MainApp.getApplication().show(aboutBox);
    }

    @Action
    public void selectXMLFile() {
        int ret = jFileChooser1.showOpenDialog(jButton3);
        if (ret == JFileChooser.APPROVE_OPTION) {
            xmlFile = jFileChooser1.getSelectedFile();
            showActivityMessage(java.util.ResourceBundle.getBundle("view/Bundle").getString("Parser.Mensage1"));
            jTextField1.setText(xmlFile.getName());
        }
    }

    @Action
    public void parse() throws IOException {
        if (xmlFile != null) {
            AlgorithmManager.getInstance().parse(xmlFile.getCanonicalPath(), jCheckBox3.isSelected());
        }
    }

    @Action
    public void stopParse() {
        AlgorithmManager.getInstance().stopParse();
    }

    @Action
    public void selectIndexDir() {
        int ret = jFileChooser2.showOpenDialog(jButton4);
        if (ret == jFileChooser2.APPROVE_OPTION) {
            indexDir = jFileChooser2.getSelectedFile();
            if (!indexDir.exists()) {
                showActivityMessage(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage2"));
            } else {
                if (!indexDir.isDirectory()) {
                    showActivityMessage(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage3"));
                } else {
                    try {
                        jTextField2.setText(indexDir.getCanonicalPath());
                    } catch (IOException e) {
                        showActivityMessage(ResourceBundle.getBundle("view/Bundle").getString("General.Mensage1"));
                        ThesisLogger.get().error("MainView.selectIndexDir: " + e.toString());
                    }
                }
            }
        }
    }

    @Action
    public void index() {
        if (DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName()).getCount() > 0) {
            ParametersEnum analyzer = ((ParametersEnum) jList1.getSelectedValue());
            if (analyzer != null && !analyzer.equals("")) {
                LuceneManager.getInstance(analyzer).indexing(indexDir, !this.jCheckBox1.isSelected());
            } else {
                LuceneManager.getInstance(ParametersEnum.INDEX_DEFAULT).indexing(indexDir, !this.jCheckBox1.isSelected());
            }
            LuceneManager.getInstance().addObserver(this);
        } else {
            showActivityMessage(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Mensage3"));
        }
    }

    @Action
    public void stopIndexing() {
        LuceneManager.getInstance().stopIndexing();
    }

    @Action
    public void search() {
        String query = textToQuery();
        if (!query.equals("")) {
            ParametersEnum analyzer = (ParametersEnum) jList1.getSelectedValue();
            if (analyzer != null && !analyzer.equals("")) {
                LuceneManager.getInstance(analyzer).search(indexDir, query, 
                        ((Integer) jComboBox2.getSelectedItem()), 
                        ((ClusteringAlgorithm) jComboBox3.getSelectedItem()));
            } else {
                LuceneManager.getInstance(ParametersEnum.INDEX_DEFAULT).search(indexDir, query, 
                        ((Integer) jComboBox2.getSelectedItem()), 
                        ((ClusteringAlgorithm) jComboBox3.getSelectedItem()));
            }
            LuceneManager.getInstance().addObserver(this);
        } else {
            showActivityMessage(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Mensage1"));
        }
    }

    @Action
    public void stopSearching() {
        LuceneManager.getInstance().stopSearching();
    }

    @Action
    public void generateVersions() {
        if (jDateChooser1.getDate() != null && jDateChooser2.getDate() != null) {
            AlgorithmManager.getInstance().generateVersions((ParametersEnum) jComboBox6.getSelectedItem(),
                    (Integer) jSpinner4.getValue(),
                    new java.sql.Date(jDateChooser1.getDate().getTime()),
                    new java.sql.Date(jDateChooser2.getDate().getTime()));
        } else {
            update(null, ResourceBundle.getBundle("view/Bundle").getString("LDA.Error2"));
        }
    }

    @Action
    public void stopGenerateVersions() {
        AlgorithmManager.getInstance().stopGenerateVersions();
    }

    @Action
    public void generateDocsForLDA() {
        AlgorithmManager.getInstance().generateDocs();
    }

    @Action
    public void stopGenerateDocsForLDA() {
        AlgorithmManager.getInstance().stopGenerateDocs();
    }

    @Action
    public void runLDA() {
        if (jDateChooser1.getDate() != null && jDateChooser2.getDate() != null) {
            AlgorithmManager.getInstance().lda(((Integer) jSpinner1.getValue()).intValue(),
                    ((Integer) jSpinner3.getValue()).intValue(),
                    ((Double) jSpinner5.getValue()).doubleValue(),
                    ((Double) jSpinner6.getValue()).doubleValue(),
                    ((Integer) jSpinner2.getValue()).intValue(),
                    jCheckBox2.isSelected(),
                    new java.sql.Date(jDateChooser1.getDate().getTime()),
                    new java.sql.Date(jDateChooser2.getDate().getTime()));
        } else {
            update(null, ResourceBundle.getBundle("view/Bundle").getString("LDA.Error2"));
        }
    }

    @Action
    public void stopLDA() {
        AlgorithmManager.getInstance().stopLDA();
    }

    @Override
    public void finalize() {
        //DAOManager.getInstance().closeConecction();
    }

    private void iniElementsView() {
        iniProgressBar();
        iniTextFields();
        iniLabels();
        iniButtons();
        iniJSpinners();
        iniTopicComboBox();   
        iniVersionComboBox(); 
        refreshTablesDocumentTopicMatrix();
        if (DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()).getCount() > 0) {
            bugComponentDistributionData.generateData();
            bugDistributionData.generateData();
        }
        refreshJLists();
    }
    
    public void iniVersionComboBox() {
        ParametersEnum typeVersion = ((VersionDAO)DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getTypeVersion();
        if(typeVersion != null)
            jComboBox6.setSelectedItem(typeVersion);
    }
    
    private void iniTopicComboBox() {
        int topicsCount = (DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getCount();
        for (int tIndx = 1; tIndx <= topicsCount; tIndx++) {
            jComboBox1.addItem(((TopicDAO) DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getTopicWords(tIndx));
        }
    }

    private void showActivityMessage(String msj) {
        jTextArea1.append(jTextArea1.getLineCount() + " - " + msj + "\n");
        jTextArea1.setCaretPosition(jTextArea1.getText().length());
    }

    private void showIndexResults(String msj) {
        jTextArea2.append(jTextArea2.getLineCount() + " - " + msj + "\n");
        jTextArea2.setCaretPosition(jTextArea2.getText().length());
    }

    private void iniLabels() {
        statusMessageLabel.setText("");
        statusAnimationLabel.setText("");
    }

    private void iniProgressBar() {
        progressBar.setVisible(true);
        progressBar.setMinimum(0);
    }
    
    private void iniTextFields() {
        jTextField8.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount()));
        jTextField9.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.BUG_DAO.getName())).getCount()));
        jTextField10.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.COMMENT_DAO.getName())).getCount()));
        jTextField11.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getCount()));
        jTextField12.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.TERM_DAO.getName())).getCount()));
        Date dateFrom = ((VersionDAO) DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getDateFromVersion();
        Date dateTo = ((VersionDAO) DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getDateToVersion();
        if (dateFrom != null && dateTo != null) {
            jDateChooser1.setDate(dateFrom);
            jDateChooser2.setDate(dateTo);
        }
    }

    private void iniButtons() {
        jButton6.setEnabled(false);
        jButton7.setEnabled(false);
        jButton8.setEnabled(false);
        jButton10.setEnabled(false);
        jButton12.setEnabled(false);
        jButton14.setEnabled(false);
    }

    private void iniJSpinners() {
        PropertiesApp.getInstance().fileLoad(ParametersEnum.MALLET_PROPERTIE_FILE_DEFAULT_PATH.getValue());
        jSpinner1.setValue(new Integer(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_ITERATIONS.toString())));
        jSpinner2.setValue(new Integer(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_WORDS_PER_TOPIC.toString())));
        jSpinner3.setValue(new Integer(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_NUM_TOPICS.toString())));
        Integer stepVersion = ((VersionDAO)DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getStepVersion();
        if(stepVersion != null)
            jSpinner4.setValue(stepVersion);
        jSpinner5.setValue(new Double(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_ALPHA.toString())));
        jSpinner6.setValue(new Double(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_BETA.toString())));
    }

    private void refreshJLists() {
        int topicsCount = ((TopicDAO) DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getCount();
        TopicDTO[] topics = new TopicDTO[topicsCount];
        if (topicsCount != 0) {
            for (int topicId = 1; topicId <= topicsCount; topicId++) {
                topics[topicId - 1] = (TopicDTO) ((TopicDAO) DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).get(topicId);
            }
            jList2.setListData(topics);
        }
    }

    public void setLabelBar(String text) {
        statusMessageLabel.removeAll();
        statusMessageLabel.setText(text);
        statusMessageLabel.repaint();
    }

    private void setProgressBar(int count) {
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setMaximum(count);
        progressBar.repaint();

    }

    private void repaintProgressBar(int percent) {
        if (percent >= progressBar.getMaximum()) {
            progressBar.setValue(0);
        } else {
            progressBar.setValue(percent);
        }
        progressBar.repaint();
    }

    private String textToQuery() {
        StringBuilder query = new StringBuilder();
        if (!jTextField3.getText().equals("")) {
            query.append(jTextField3.getText().toLowerCase().replace(" ", " AND "));
        }
        if (!jTextField4.getText().equals("")) {
            query.append(" \"" + jTextField4.getText().toLowerCase() + "\"^8.0");
        }
        query.append(jTextField5.getText());
        if (!jTextField6.getText().equals("")) {
            query.append(" -" + jTextField6.getText().toLowerCase().replace(" ", " -"));
        }
        if (!jTextField7.getText().equals("")) {
            query.append(" " + ((String) jComboBox4.getSelectedItem()).toLowerCase() + ":(" + jTextField7.getText().toLowerCase() + ")^20.0");
        }
        return query.toString();
    }

    private void refreshTablesDocumentTopicMatrix() {
        tableModel = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        TableRowSorter<TableModel> rowShorter = new TableRowSorter<TableModel>(tableModel);
        jTable1.setRowSorter(rowShorter);

        tableModel2 = (javax.swing.table.DefaultTableModel) jTable2.getModel();
        TableRowSorter<TableModel> rowShorter2 = new TableRowSorter<TableModel>(tableModel2);
        jTable2.setRowSorter(rowShorter2);

        tableModel3 = (javax.swing.table.DefaultTableModel) jTable3.getModel();
        TableRowSorter<TableModel> rowShorter3 = new TableRowSorter<TableModel>(tableModel3);
        jTable3.setRowSorter(rowShorter3);

        int topicsCount = ((TopicDAO) DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getCount();
        if (topicsCount != 0) {
            // Delete tables.
            deleteTables();
            DefaultTableCellRenderer centerRenderer2 = new DefaultTableCellRenderer();
            centerRenderer2.setHorizontalAlignment(JLabel.CENTER);            
            if (jComboBox1.getSelectedItem() != null) {
                int tIndx = ((TopicDTO) jComboBox1.getSelectedItem()).getID();
                TopicDTO topic = ((TopicDAO) DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getTopicWords(tIndx);
                fillTableTopic(topic, tableModel3);
                fillTableTopicTerm(topic, tableModel);
                topic = ((TopicDAO) DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getTopicDocs(tIndx);
                fillTableDocumentTopic(topic, tableModel2);
                jTable1.getColumnModel().getColumn(1).setCellRenderer(centerRenderer2);
                jTable2.getColumnModel().getColumn(1).setCellRenderer(centerRenderer2);
                jTable2.getColumnModel().getColumn(0).setMinWidth(60);
                jTable2.getColumnModel().getColumn(0).setMaxWidth(60);
                jTable1.getColumnModel().getColumn(0).setMinWidth(300);
                jTable1.getColumnModel().getColumn(0).setMaxWidth(300);
                jTable3.getColumnModel().getColumn(0).setMinWidth(400);
                jTable3.getColumnModel().getColumn(0).setMaxWidth(400);
            }           
        }
    }

    private void deleteTables() {
        tableModel3.setRowCount(0);
        tableModel3.setColumnCount(0);
        tableModel.setRowCount(0);
        tableModel.setColumnCount(1);
        ArrayList terms = (ArrayList) ((TermDAO) DAOManager.getDAO(DAONameEnum.TERM_DAO.getName())).getAllTerm();
        int ii = 1;
        for (Iterator<String> it = terms.iterator(); it.hasNext();) {
            String term = it.next();
            if (!term.equals("")) {
                tableModel.addRow(new Object[]{term});
                ii++;
            }
        }
        tableModel2.setRowCount(0);
        tableModel2.setColumnCount(1);
        for (int i = 1; i <= ((DocumentDAO) DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount(); i++) {
            tableModel2.addRow(new Object[]{i});
        }
    }

    private void fillTableTopic(TopicDTO topic, javax.swing.table.DefaultTableModel tableModel3) {
        if (topic != null) {
            Object[] terms = new Object[topic.getDataWords().size()];
            int i = 0;
            for (Iterator<Entry<String, BigDecimal>> it = topic.getSortDataWords().entrySet().iterator(); it.hasNext();) {
                Entry<String, BigDecimal> entry = it.next();
                terms[i] = entry.getKey();
                i++;
            }
            tableModel3.addColumn("<html><b>Z" + topic.getID() + "</b></html>", terms);
        }
    }

    private void fillTableTopicTerm(TopicDTO topic, javax.swing.table.DefaultTableModel tableModel) {
        if (topic != null) {
            Object[] probs = new Object[((TermDAO) DAOManager.getDAO(DAONameEnum.TERM_DAO.getName())).getCount()];
            for (Iterator<Entry<String, BigDecimal>> it = topic.getDataWords().entrySet().iterator(); it.hasNext();) {
                Entry<String, BigDecimal> entry = it.next();
                int pos = ((TermDAO) DAOManager.getDAO(DAONameEnum.TERM_DAO.getName())).getTermId(entry.getKey());
                if (pos != -1) {
                    probs[pos - 1] = entry.getValue();
                }
            }
            tableModel.addColumn("<html><b>Z" + topic.getID() + "</b></html>", probs);
        }
    }

    private void fillTableDocumentTopic(TopicDTO topic, javax.swing.table.DefaultTableModel tableModel) {
        if (topic != null) {
            Object[] probs = new Object[((DocumentDAO) DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount()];
            for (Entry<Integer, BigDecimal> entry : topic.getDataDocs().entrySet()) {
                probs[entry.getKey() - 1] = entry.getValue();
            }
            tableModel.addColumn("<html><b>Z" + topic.getID() + "</b></html>", probs);
        }
    }

    private void showBugTable(BugDTO bug, ArrayList<CommentDTO> comments) {
        inicBugTable();
        DefaultTableModel tableModel4 = (javax.swing.table.DefaultTableModel) jTable4.getModel();
        Date dateClosed = bug.getClosedDate();
        tableModel4.addColumn("<html><b>Tag</b></htlm>");
        tableModel4.addColumn("<html><b>Value</b></htlm>");
        tableModel4.addRow(new Object[]{"<html><b>Title</b></html>", bug.getTitle()});
        tableModel4.addRow(new Object[]{"<html><b>Status</b></html>", bug.getStatus()});
        tableModel4.addRow(new Object[]{"<html><b>Owner</b></html>", bug.getOwner()});
        tableModel4.addRow(new Object[]{"<html><b>Type Priority</b></html>", bug.getPriority()});
        tableModel4.addRow(new Object[]{"<html><b>Component</b></html>", bug.getComponent()});
        if (dateClosed != null) {
            tableModel4.addRow(new Object[]{"<html><b>Closed On</b></html>", sdf.format(dateClosed)});
        } else {
            tableModel4.addRow(new Object[]{"<html><b>Closed On</b></html>", null});
        }
        tableModel4.addRow(new Object[]{"<html><b>Stars</b></html>", bug.getStars()});
        tableModel4.addRow(new Object[]{"<html><b>Reported By</b></html>", bug.getReportedBY()});
        tableModel4.addRow(new Object[]{"<html><b>Opened Date</b></html>", sdf.format(bug.getOpenedDate())});
        tableModel4.addRow(new Object[]{"<html><b>Description</b></html>", bug.getDescription()});
        if (comments != null) {
            tableModel4.addRow(new Object[]{"<html><b>Comments</b></html>", comments.size()});
            for (CommentDTO comment : comments) {
                tableModel4.addRow(new Object[]{"<html><b>Author</b></html>", comment.getAuthor()});
                tableModel4.addRow(new Object[]{"<html><b>Date</b></html>", sdf.format(comment.getDate())});
                tableModel4.addRow(new Object[]{"<html><b>Text</b></html>", comment.getText()});
            }
        } else {
            tableModel4.addRow(new Object[]{"<html><b>Comments</b></html>", 0});
        }
        jTable4.getColumnModel().getColumn(0).setMaxWidth(88);
        jTable4.getColumnModel().getColumn(0).setMinWidth(88);
        jTable4.getColumnModel().getColumn(1).setCellRenderer(new TextAreaRenderer(new JScrollPane()));
        jTable4.getColumnModel().getColumn(1).setMaxWidth(500);
    }

    private void inicBugTable() {
        DefaultTableModel tableModel4 = (javax.swing.table.DefaultTableModel) jTable4.getModel();
        tableModel4.setRowCount(0);
        tableModel4.setColumnCount(0);
    }

    private void showVersionTable(VersionDTO version) {
        inicVersionTable();
        DefaultTableModel tableModel5 = (javax.swing.table.DefaultTableModel) jTable5.getModel();
        tableModel5.addColumn("<html><b>Date</b></html>", new Object[]{"<html><b>From</b></html>", "<html><b>To</b></html>"});
        tableModel5.addColumn("<html><b>Value</b></html>", new Object[]{sdf.format(version.getDateFrom()), sdf.format(version.getDateTo())});
    }

    private void inicVersionTable() {
        DefaultTableModel tableModel5 = (javax.swing.table.DefaultTableModel) jTable5.getModel();
        tableModel5.setRowCount(0);
        tableModel5.setColumnCount(0);
    }

    private void showDocumentTable(DocumentDTO document) {
        inicDocumentTable();
        DefaultTableModel tableModel6 = (javax.swing.table.DefaultTableModel) jTable6.getModel();
        tableModel6.addColumn("<html><b>Tag</b></html>", new Object[]{"<html><b>Time Stamp</b></html>", "<html><b>Text</b></html>"});
        tableModel6.addColumn("<html><b>Value</b></html>", new Object[]{sdf.format(document.getDate()), document.getText()});
        jTable6.getColumnModel().getColumn(0).setMaxWidth(88);
        jTable6.getColumnModel().getColumn(0).setMinWidth(88);
        jTable6.getColumnModel().getColumn(1).setCellRenderer(new TextAreaRenderer(new JScrollPane()));
    }

    private void inicDocumentTable() {
        DefaultTableModel tableModel6 = (javax.swing.table.DefaultTableModel) jTable6.getModel();
        tableModel6.setRowCount(0); 
        tableModel6.setColumnCount(0);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jCheckBox3 = new javax.swing.JCheckBox();
        jButton3 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel13 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jTextField8 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jComboBox6 = new javax.swing.JComboBox();
        jSpinner4 = new javax.swing.JSpinner();
        jLabel23 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jLabel37 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jPanel9 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        jSpinner3 = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jButton10 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jSpinner5 = new javax.swing.JSpinner();
        jSpinner6 = new javax.swing.JSpinner();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel31 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jSpinner8 = new javax.swing.JSpinner();
        jLabel34 = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jLabel36 = new javax.swing.JLabel();
        jSpinner9 = new javax.swing.JSpinner();
        jSpinner7 = new javax.swing.JSpinner();
        bugComponentDistribution = new PieChart();
        bugComponentDistributionData = new charts.data.BugComponentDistributionGenerator(bugComponentDistribution);
        bugComponentDistribution.setPreferredSize(380, 300);
        jScrollPane8 = new javax.swing.JScrollPane(bugComponentDistribution.getGraphicPanel());
        BugDistributionChart = new XYSplineChart();
        BugDistributionChart.setTitle("Bug and Comments Distribution");
        BugDistributionChart.setYLabel("Number");

        bugDistributionData = new charts.data.BugCommentDistributionGenerator(BugDistributionChart);
        BugDistributionChart.setPreferredSize(400, 300);
        jScrollPane9 = new javax.swing.JScrollPane(BugDistributionChart.getGraphicPanel());
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel35 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox();
        jLabel29 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        splineChart = new XYSplineChart();
        splineChartGenData = new charts.data.MetricEvolutionGenerator(splineChart);
        jScrollPane6 = new javax.swing.JScrollPane(splineChart.getGraphicPanel());
        jPanel15 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton7 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel33 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jSeparator1 = new javax.swing.JSeparator();
        jFileChooser1 = new javax.swing.JFileChooser();
        jFileChooser2 = new javax.swing.JFileChooser();

        mainPanel.setName("mainPanel"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setName("jPanel5"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("view/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("MainView.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextField1.setEditable(false);
        jTextField1.setText(bundle.getString("MainView.jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        jCheckBox3.setText(bundle.getString("MainView.jCheckBox3.text")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(view.MainApp.class).getContext().getActionMap(MainView.class, this);
        jButton3.setAction(actionMap.get("selectXMLFile")); // NOI18N
        jButton3.setText(bundle.getString("MainView.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        jButton8.setAction(actionMap.get("stopParse")); // NOI18N
        jButton8.setText(bundle.getString("MainView.jButton8.text")); // NOI18N
        jButton8.setName("jButton8"); // NOI18N

        jButton1.setAction(actionMap.get("parse")); // NOI18N
        jButton1.setText(bundle.getString("MainView.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jLabel19.setText(bundle.getString("MainView.jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        jLabel20.setText(bundle.getString("MainView.jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        jTextField9.setEditable(false);
        jTextField9.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField9.setText(bundle.getString("MainView.jTextField9.text")); // NOI18N
        jTextField9.setName("jTextField9"); // NOI18N

        jLabel21.setText(bundle.getString("MainView.jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        jTextField10.setEditable(false);
        jTextField10.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField10.setText(bundle.getString("MainView.jTextField10.text")); // NOI18N
        jTextField10.setName("jTextField10"); // NOI18N

        jLabel38.setText(bundle.getString("MainView.jLabel38.text")); // NOI18N
        jLabel38.setName("jLabel38"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3))
                            .addComponent(jLabel1)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel38)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel19)
                                    .addComponent(jCheckBox3))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(7, 7, 7)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel21)
                    .addComponent(jLabel20)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 153, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton8)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setText("");
        jTextArea1.setEditable(false);
        DefaultCaret caret = (DefaultCaret)jTextArea1.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        jLabel13.setText(bundle.getString("MainView.jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel8.setName("jPanel8"); // NOI18N

        jLabel18.setText(bundle.getString("MainView.jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        jButton11.setAction(actionMap.get("generateDocsForLDA")); // NOI18N
        jButton11.setText(bundle.getString("MainView.jButton11.text")); // NOI18N
        jButton11.setName("jButton11"); // NOI18N

        jTextField8.setEditable(false);
        jTextField8.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField8.setText(bundle.getString("MainView.jTextField8.text")); // NOI18N
        jTextField8.setName("jTextField8"); // NOI18N

        jLabel22.setText(bundle.getString("MainView.jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jButton12.setAction(actionMap.get("stopGenerateDocsForLDA")); // NOI18N
        jButton12.setText(bundle.getString("MainView.jButton12.text")); // NOI18N
        jButton12.setName("jButton12"); // NOI18N

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new ParametersEnum[] {
            ParametersEnum.LDA_STEP_DAY,
            ParametersEnum.LDA_STEP_MONTH,
            ParametersEnum.LDA_STEP_YEAR }));
jComboBox6.setSelectedIndex(0);
jComboBox6.setName("jComboBox6"); // NOI18N

jSpinner4.setName("jSpinner4");
jSpinner4.addChangeListener(new javax.swing.event.ChangeListener() {
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        jSpinner4StateChanged(evt);
    }
    });

    jLabel23.setText(bundle.getString("MainView.jLabel23.text")); // NOI18N
    jLabel23.setName("jLabel23"); // NOI18N

    jTextField11.setEditable(false);
    jTextField11.setHorizontalAlignment(javax.swing.JTextField.CENTER);
    jTextField11.setText(bundle.getString("MainView.jTextField11.text")); // NOI18N
    jTextField11.setName("jTextField11"); // NOI18N

    jLabel24.setText(bundle.getString("MainView.jLabel24.text")); // NOI18N
    jLabel24.setName("jLabel24"); // NOI18N

    jLabel25.setText(bundle.getString("MainView.jLabel25.text")); // NOI18N
    jLabel25.setName("jLabel25"); // NOI18N

    jLabel30.setText(bundle.getString("MainView.jLabel30.text")); // NOI18N
    jLabel30.setName("jLabel30"); // NOI18N

    jLabel17.setText(bundle.getString("MainView.jLabel17.text")); // NOI18N
    jLabel17.setName("jLabel17"); // NOI18N

    jTextField12.setEditable(false);
    jTextField12.setHorizontalAlignment(javax.swing.JTextField.CENTER);
    jTextField12.setText(bundle.getString("MainView.jTextField12.text")); // NOI18N
    jTextField12.setName("jTextField12"); // NOI18N

    jButton13.setAction(actionMap.get("generateVersions")); // NOI18N
    jButton13.setText(bundle.getString("MainView.jButton13.text")); // NOI18N
    jButton13.setName("jButton13"); // NOI18N

    jButton14.setAction(actionMap.get("stopGenerateVersions")); // NOI18N
    jButton14.setText(bundle.getString("MainView.jButton14.text")); // NOI18N
    jButton14.setName("jButton14"); // NOI18N

    jLabel37.setText(bundle.getString("MainView.jLabel37.text")); // NOI18N
    jLabel37.setName("jLabel37"); // NOI18N

    jDateChooser1.setToolTipText(bundle.getString("MainView.jDateChooser1.toolTipText")); // NOI18N
    jDateChooser1.setName("jDateChooser1"); // NOI18N

    jDateChooser2.setName("jDateChooser2"); // NOI18N

    javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
    jPanel8.setLayout(jPanel8Layout);
    jPanel8Layout.setHorizontalGroup(
        jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel8Layout.createSequentialGroup()
            .addGap(88, 88, 88)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel37, javax.swing.GroupLayout.Alignment.CENTER)
                .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel8Layout.createSequentialGroup()
                    .addComponent(jButton13)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton14))
                .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel8Layout.createSequentialGroup()
                    .addComponent(jButton11)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton12))
                .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.CENTER))
            .addGap(89, 89, 89))
        .addGroup(jPanel8Layout.createSequentialGroup()
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel30)
                        .addComponent(jLabel24)
                        .addComponent(jLabel25)
                        .addComponent(jLabel23))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jSpinner4))
                        .addComponent(jTextField11)
                        .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel18)
                        .addComponent(jLabel17))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTextField12)
                        .addComponent(jTextField8))))
            .addContainerGap())
    );
    jPanel8Layout.setVerticalGroup(
        jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel8Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel22)
            .addGap(11, 11, 11)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel18))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel17)
                .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jButton12)
                .addComponent(jButton11))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
            .addComponent(jLabel37)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel23))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel30)
                .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel24))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel25))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton13)
                .addComponent(jButton14))
            .addContainerGap())
    );

    jSpinner4.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

    jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jPanel9.setName("jPanel9"); // NOI18N

    jLabel12.setText(bundle.getString("MainView.jLabel12.text")); // NOI18N
    jLabel12.setName("jLabel12"); // NOI18N

    jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), null, null, Integer.valueOf(500)));
    jSpinner1.setName("jSpinner1"); // NOI18N
    jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSpinner1StateChanged(evt);
        }
    });

    jLabel16.setText(bundle.getString("MainView.jLabel16.text")); // NOI18N
    jLabel16.setName("jLabel16"); // NOI18N

    jSpinner3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jSpinner3.setName("jSpinner3"); // NOI18N
    jSpinner3.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSpinner3StateChanged(evt);
        }
    });

    jLabel15.setText(bundle.getString("MainView.jLabel15.text")); // NOI18N
    jLabel15.setName("jLabel15"); // NOI18N

    jSpinner2.setName("jSpinner2"); // NOI18N
    jSpinner2.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSpinner2StateChanged(evt);
        }
    });

    jButton10.setAction(actionMap.get("stopLDA")); // NOI18N
    jButton10.setText(bundle.getString("MainView.jButton10.text")); // NOI18N
    jButton10.setName("jButton10"); // NOI18N

    jButton9.setAction(actionMap.get("runLDA")); // NOI18N
    jButton9.setText(bundle.getString("MainView.jButton9.text")); // NOI18N
    jButton9.setName("jButton9"); // NOI18N

    jLabel26.setText(bundle.getString("MainView.jLabel26.text")); // NOI18N
    jLabel26.setName("jLabel26"); // NOI18N

    jLabel27.setText(bundle.getString("MainView.jLabel27.text")); // NOI18N
    jLabel27.setName("jLabel27"); // NOI18N

    jSpinner5.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.00d), null, null, Double.valueOf(0.01d)));
    jSpinner5.setName("jSpinner5"); // NOI18N
    jSpinner5.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSpinner5StateChanged(evt);
        }
    });

    jSpinner6.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.00d), null, null, Double.valueOf(0.01d)));
    jSpinner6.setName("jSpinner6"); // NOI18N
    jSpinner6.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSpinner6StateChanged(evt);
        }
    });

    jCheckBox2.setSelected(true);
    jCheckBox2.setText(bundle.getString("MainView.jCheckBox2.text")); // NOI18N
    jCheckBox2.setName("jCheckBox2"); // NOI18N

    jLabel31.setText(bundle.getString("MainView.jLabel31.text")); // NOI18N
    jLabel31.setName("jLabel31"); // NOI18N

    javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
    jPanel9.setLayout(jPanel9Layout);
    jPanel9Layout.setHorizontalGroup(
        jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel9Layout.createSequentialGroup()
            .addGap(10, 10, 10)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel9Layout.createSequentialGroup()
                    .addComponent(jButton9)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton10))
                .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSpinner5, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSpinner2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSpinner3, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSpinner6)
                        .addComponent(jCheckBox2, javax.swing.GroupLayout.Alignment.LEADING))))
            .addGap(0, 19, Short.MAX_VALUE))
    );
    jPanel9Layout.setVerticalGroup(
        jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel9Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel31)
            .addGap(27, 27, 27)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel12))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel16))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel15))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jSpinner5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel26))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jSpinner6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel27))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jCheckBox2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jButton10)
                .addComponent(jButton9))
            .addContainerGap())
    );

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jScrollPane1)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel13)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(0, 381, Short.MAX_VALUE)))
            .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel13)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
            .addContainerGap())
    );

    jTabbedPane1.addTab(bundle.getString("MainView.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

    jPanel12.setName("jPanel12"); // NOI18N

    jPanel13.setName("jPanel13"); // NOI18N

    jPanel14.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jPanel14.setName("jPanel14"); // NOI18N

    jLabel32.setText(bundle.getString("MainView.jLabel32.text")); // NOI18N
    jLabel32.setName("jLabel32"); // NOI18N

    jScrollPane10.setName("jScrollPane10"); // NOI18N

    jTable4.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {},
        new String [] {}
    ){
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class
        };
        boolean[] canEdit = new boolean [] {
            false, false
        };

        public boolean isCellEditable (int row, int column) {
            return false; //canEdit [column];
        }
    }

    );
    jTable4.setToolTipText(bundle.getString("MainView.jTable4.toolTipText")); // NOI18N
    jScrollPane10.setViewportView(jTable4);
    jTable4.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jTable4.AccessibleContext.accessibleName")); // NOI18N

    jSpinner8.setName("jSpinner8"); // NOI18N
    jSpinner8.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSpinner8StateChanged(evt);
        }
    });

    jLabel34.setText(bundle.getString("MainView.jLabel34.text")); // NOI18N
    jLabel34.setName("jLabel34"); // NOI18N

    jScrollPane12.setName("jScrollPane12"); // NOI18N

    jTable5.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {},
        new String [] {}

    ){
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class
        };
        boolean[] canEdit = new boolean [] {
            false, false
        };

        public boolean isCellEditable (int row, int column) {
            return false; //canEdit [column];
        }
    }
    );
    jTable5.setToolTipText(bundle.getString("MainView.jTable5.toolTipText")); // NOI18N
    jScrollPane12.setViewportView(jTable5);
    jTable5.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jTable5.AccessibleContext.accessibleName")); // NOI18N

    jScrollPane13.setName("jScrollPane13"); // NOI18N

    jTable6.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {},
        new String [] {}

    ){
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class
        };
        boolean[] canEdit = new boolean [] {
            false, false
        };

        public boolean isCellEditable (int row, int column) {
            return false; //canEdit [column];
        }
    });
    jTable6.setToolTipText(bundle.getString("MainView.jTable6.toolTipText")); // NOI18N
    jScrollPane13.setViewportView(jTable6);
    jTable6.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jTable6.AccessibleContext.accessibleName")); // NOI18N

    jLabel36.setText(bundle.getString("MainView.jLabel36.text")); // NOI18N
    jLabel36.setName("jLabel36"); // NOI18N

    jSpinner9.setName("jSpinner9"); // NOI18N
    jSpinner9.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSpinner9StateChanged(evt);
        }
    });

    jSpinner7.setName("jSpinner7"); // NOI18N
    jSpinner7.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSpinner7StateChanged(evt);
        }
    });

    javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
    jPanel14.setLayout(jPanel14Layout);
    jPanel14Layout.setHorizontalGroup(
        jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel14Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel14Layout.createSequentialGroup()
                    .addComponent(jLabel34)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jSpinner8, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGroup(jPanel14Layout.createSequentialGroup()
                    .addComponent(jLabel32)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jSpinner7)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel14Layout.createSequentialGroup()
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(jPanel14Layout.createSequentialGroup()
                    .addComponent(jLabel36)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jSpinner9)))
            .addContainerGap())
    );
    jPanel14Layout.setVerticalGroup(
        jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel14Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36)
                    .addComponent(jSpinner9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jLabel34))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel14Layout.createSequentialGroup()
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel32)
                        .addComponent(jSpinner7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE))
                .addComponent(jScrollPane13))
            .addContainerGap())
    );

    jScrollPane10.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jScrollPane10.AccessibleContext.accessibleName")); // NOI18N
    jScrollPane12.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jScrollPane12.AccessibleContext.accessibleName")); // NOI18N
    jScrollPane13.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jScrollPane13.AccessibleContext.accessibleName")); // NOI18N

    jScrollPane8.setName("jScrollPane8"); // NOI18N

    jScrollPane9.setName("jScrollPane9"); // NOI18N

    javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
    jPanel13.setLayout(jPanel13Layout);
    jPanel13Layout.setHorizontalGroup(
        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel13Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
                .addComponent(jScrollPane8))
            .addContainerGap())
    );
    jPanel13Layout.setVerticalGroup(
        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel13Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel13Layout.createSequentialGroup()
                    .addComponent(jScrollPane8)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane9))
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );

    javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
    jPanel12.setLayout(jPanel12Layout);
    jPanel12Layout.setHorizontalGroup(
        jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel12Layout.createSequentialGroup()
            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
    );
    jPanel12Layout.setVerticalGroup(
        jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    jTabbedPane1.addTab(bundle.getString("MainView.jPanel12.TabConstraints.tabTitle"), jPanel12); // NOI18N

    jPanel2.setName("jPanel2"); // NOI18N

    jPanel7.setName("jPanel7"); // NOI18N

    jScrollPane5.setName("jScrollPane5"); // NOI18N

    jTable3.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
        },
        new String [] {

        }
    ){
        Class[] types = new Class [] {
            java.lang.String.class, java.math.BigDecimal.class
        };
        boolean[] canEdit = new boolean [] {
            false, false
        };

        public boolean isCellEditable (int row, int column) {
            return false; //canEdit [column];
        }
    }

    );
    jTable3.setToolTipText(bundle.getString("MainView.jTable3.toolTipText")); // NOI18N
    jScrollPane5.setViewportView(jTable3);
    //jTable3.setRowHeight(20);
    DefaultTableCellRenderer centerRenderer3 = new DefaultTableCellRenderer();
    centerRenderer3.setHorizontalAlignment(JLabel.CENTER);
    jScrollPane5.setViewportView(jTable3);
    jTable3.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jTable3.AccessibleContext.accessibleName")); // NOI18N

    jScrollPane2.setName("jScrollPane2"); // NOI18N

    jTable1.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "<html><b>Term Id</b></htlm>"
        }
    ){
        Class[] types = new Class [] {
            java.lang.String.class, java.math.BigDecimal.class
        };
        boolean[] canEdit = new boolean [] {
            false, false
        };

        public boolean isCellEditable (int row, int column) {
            return false; //canEdit [column];
        }
    }
    );
    jTable1.setToolTipText(bundle.getString("MainView.jTable1.toolTipText")); // NOI18N
    jTable1.setName("jTable1");
    jTable1.getColumnModel().getColumn(0).setMaxWidth(300);
    //jTable1.setRowHeight(20);
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    jTable1.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
    jScrollPane2.setViewportView(jTable1);

    jScrollPane3.setName("jScrollPane3"); // NOI18N

    jTable2.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "<html><b>Doc Id</b></htlm>"
        }
    ){
        Class[] types = new Class [] {
            java.lang.String.class, java.math.BigDecimal.class
        };
        boolean[] canEdit = new boolean [] {
            false, false
        };

        public boolean isCellEditable (int row, int column) {
            return false;//canEdit [column];
        }
    }
    );
    jTable2.setToolTipText(bundle.getString("MainView.jTable2.toolTipText")); // NOI18N
    jTable2.setName("jTable2");
    jTable2.getColumnModel().getColumn(0).setMaxWidth(60);
    //jTable2.setRowHeight(20);
    DefaultTableCellRenderer centerRenderer2 = new DefaultTableCellRenderer();
    centerRenderer2.setHorizontalAlignment(JLabel.CENTER);
    jTable2.getColumnModel().getColumn(0).setCellRenderer(centerRenderer2);
    jScrollPane3.setViewportView(jTable2);
    jTable2.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jTable2.AccessibleContext.accessibleName")); // NOI18N

    jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new TopicDTO[] { }));
    jComboBox1.setName("jComboBox1");
    jComboBox1.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            jComboBox1ItemStateChanged(evt);
        }
    });

    jLabel35.setText(bundle.getString("MainView.jLabel35.text")); // NOI18N
    jLabel35.setName("jLabel35"); // NOI18N

    javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
    jPanel7.setLayout(jPanel7Layout);
    jPanel7Layout.setHorizontalGroup(
        jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel7Layout.createSequentialGroup()
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addComponent(jLabel35)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(34, 34, 34)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
            .addGap(34, 34, 34)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    jPanel7Layout.setVerticalGroup(
        jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel7Layout.createSequentialGroup()
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel35))
            .addGap(18, 18, 18)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane5)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
                .addComponent(jScrollPane3))
            .addContainerGap())
    );

    jScrollPane5.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jScrollPane5.AccessibleContext.accessibleName")); // NOI18N
    jScrollPane2.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jScrollPane2.AccessibleContext.accessibleName")); // NOI18N
    jScrollPane3.getAccessibleContext().setAccessibleName(bundle.getString("MainView.jScrollPane3.AccessibleContext.accessibleName")); // NOI18N

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jTabbedPane1.addTab(bundle.getString("MainView.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

    jPanel3.setName("jPanel3"); // NOI18N

    jPanel10.setName("jPanel10"); // NOI18N

    jPanel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jPanel11.setName("jPanel11"); // NOI18N

    jLabel28.setText(bundle.getString("MainView.jLabel28.text")); // NOI18N
    jLabel28.setName("jLabel28"); // NOI18N

    jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new Metric[] {
        new Assigment(),
        new Weight(),
        new Scattering(),
        new Focus()}));
jComboBox5.setName("jComboBox5");
jComboBox5.addItemListener(new java.awt.event.ItemListener() {
public void itemStateChanged(java.awt.event.ItemEvent evt) {
    jComboBox5ItemStateChanged(evt);
    }
    });

    jLabel29.setText(bundle.getString("MainView.jLabel29.text")); // NOI18N
    jLabel29.setName("jLabel29"); // NOI18N

    jScrollPane7.setName("jScrollPane7"); // NOI18N

    jList2.setModel(new javax.swing.AbstractListModel() {
        TopicDTO[] topcis = {};
        public int getSize() { return topcis.length; }
        public TopicDTO getElementAt(int i) { return topcis[i]; }
    });
    jList2.setName("jList2");
    jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
            jList2ValueChanged(evt);
        }
    });
    jScrollPane7.setViewportView(jList2);

    javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
    jPanel11.setLayout(jPanel11Layout);
    jPanel11Layout.setHorizontalGroup(
        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel11Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel28)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel29))
                    .addGap(0, 0, Short.MAX_VALUE))
                .addComponent(jComboBox5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );
    jPanel11Layout.setVerticalGroup(
        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel11Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel28)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jLabel29)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
            .addContainerGap())
    );

    //jComboBox5.removeAllItems();
    //jComboBox5.addItem(new Assigment());
    //jComboBox5.addItem(new Weight());
    //jComboBox5.addItem(new Scattering());
    //jComboBox5.addItem(new Focus());

    jScrollPane6.setName("jScrollPane6"); // NOI18N

    javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
    jPanel10.setLayout(jPanel10Layout);
    jPanel10Layout.setHorizontalGroup(
        jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel10Layout.createSequentialGroup()
            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 927, Short.MAX_VALUE))
    );
    jPanel10Layout.setVerticalGroup(
        jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane6)
        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
    );

    jTabbedPane1.addTab(bundle.getString("MainView.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

    jPanel15.setName("jPanel15"); // NOI18N

    jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jPanel4.setName("jPanel4"); // NOI18N

    jLabel2.setText(bundle.getString("MainView.jLabel2.text")); // NOI18N
    jLabel2.setName("jLabel2"); // NOI18N

    jTextField2.setEditable(false);
    jTextField2.setText(bundle.getString("MainView.jTextField2.text")); // NOI18N
    jTextField2.setName("jTextField2"); // NOI18N

    jScrollPane4.setName("jScrollPane4"); // NOI18N

    jList1.setModel(new javax.swing.AbstractListModel() {
        util.ParametersEnum[] strings = {util.ParametersEnum.INDEX_WHITESPACE_ANALYZER,
            util.ParametersEnum.INDEX_SIMPLE_ANALYZER,
            util.ParametersEnum.INDEX_STOP_ANALYZER,
            util.ParametersEnum.INDEX_STANDAR_ANALYZER};
        public int getSize() { return strings.length; }
        public util.ParametersEnum getElementAt(int i) { return strings[i]; }
    });
    jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jList1.setToolTipText(bundle.getString("MainView.jList1.toolTipText")); // NOI18N
    jList1.setName("jList1"); // NOI18N
    jScrollPane4.setViewportView(jList1);

    jLabel3.setText(bundle.getString("MainView.jLabel3.text")); // NOI18N
    jLabel3.setName("jLabel3"); // NOI18N

    jCheckBox1.setText(bundle.getString("MainView.jCheckBox1.text")); // NOI18N
    jCheckBox1.setName("jCheckBox1"); // NOI18N

    jButton7.setAction(actionMap.get("stopIndexing")); // NOI18N
    jButton7.setText(bundle.getString("MainView.jButton7.text")); // NOI18N
    jButton7.setName("jButton7"); // NOI18N

    jButton2.setAction(actionMap.get("index")); // NOI18N
    jButton2.setText(bundle.getString("MainView.jButton2.text")); // NOI18N
    jButton2.setName("jButton2"); // NOI18N

    jButton4.setAction(actionMap.get("selectIndexDir")); // NOI18N
    jButton4.setText(bundle.getString("MainView.jButton4.text")); // NOI18N
    jButton4.setName("jButton4"); // NOI18N

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jButton2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton4))
                        .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)))
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel3)
                        .addComponent(jLabel2)
                        .addComponent(jCheckBox1)))
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
    );
    jPanel4Layout.setVerticalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel3)
            .addGap(1, 1, 1)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jCheckBox1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton2)
                .addComponent(jButton7)
                .addComponent(jButton4))
            .addContainerGap())
    );

    jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jPanel6.setName("jPanel6"); // NOI18N

    jLabel4.setText(bundle.getString("MainView.jLabel4.text")); // NOI18N
    jLabel4.setName("jLabel4"); // NOI18N

    jLabel6.setText(bundle.getString("MainView.jLabel6.text")); // NOI18N
    jLabel6.setName("jLabel6"); // NOI18N

    jLabel7.setText(bundle.getString("MainView.jLabel7.text")); // NOI18N
    jLabel7.setName("jLabel7"); // NOI18N

    jLabel8.setText(bundle.getString("MainView.jLabel8.text")); // NOI18N
    jLabel8.setName("jLabel8"); // NOI18N

    jLabel9.setText(bundle.getString("MainView.jLabel9.text")); // NOI18N
    jLabel9.setName("jLabel9"); // NOI18N

    jLabel5.setText(bundle.getString("MainView.jLabel5.text")); // NOI18N
    jLabel5.setName("jLabel5"); // NOI18N

    jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new ClusteringAlgorithm[] {
        new ClusteringAlgorithm(new BisectingKMeansClusteringAlgorithm(), ParametersEnum.SEARCH_ALGORITHM_KMEANS.toString()),
        new ClusteringAlgorithm(new STCClusteringAlgorithm(), ParametersEnum.SEARCH_ALGORITHM_SUFFIX.toString()),
        new ClusteringAlgorithm(new LingoClusteringAlgorithm(), ParametersEnum.SEARCH_ALGORITHM_LINGO.toString()),
        new ClusteringAlgorithm(new ByFieldClusteringAlgorithm(), ParametersEnum.SEARCH_ALGORITHM_SYNTHETIC.toString())}));
jComboBox3.setName("jComboBox3"); // NOI18N

jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new Integer[] {
new Integer(4),
new Integer(10),
new Integer(20),
new Integer(30),
new Integer(50),
new Integer(100),
new Integer(500),
new Integer(1000),
new Integer(5000),
new Integer(10000)}));
jComboBox2.setName("jComboBox2"); // NOI18N

jLabel11.setText(bundle.getString("MainView.jLabel11.text")); // NOI18N
jLabel11.setName("jLabel11"); // NOI18N

jTextField3.setText(bundle.getString("MainView.jTextField3.text")); // NOI18N
jTextField3.setName("jTextField3"); // NOI18N

jTextField4.setText(bundle.getString("MainView.jTextField4.text")); // NOI18N
jTextField4.setName("jTextField4"); // NOI18N

jTextField5.setText(bundle.getString("MainView.jTextField5.text")); // NOI18N
jTextField5.setName("jTextField5"); // NOI18N

jTextField6.setText(bundle.getString("MainView.jTextField6.text")); // NOI18N
jTextField6.setName("jTextField6"); // NOI18N

jTextField7.setText(bundle.getString("MainView.jTextField7.text")); // NOI18N
jTextField7.setName("jTextField7"); // NOI18N

jLabel10.setText(bundle.getString("MainView.jLabel10.text")); // NOI18N
jLabel10.setName("jLabel10"); // NOI18N

jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new ParametersEnum[] {
    ParametersEnum.INDEX_FIELD1,
    ParametersEnum.INDEX_FIELD2,
    ParametersEnum.INDEX_FIELD3}));
    jComboBox4.setName("jComboBox4"); // NOI18N

    jButton5.setAction(actionMap.get("search")); // NOI18N
    jButton5.setText(bundle.getString("MainView.jButton5.text")); // NOI18N
    jButton5.setName("jButton5"); // NOI18N

    jButton6.setAction(actionMap.get("stopSearching")); // NOI18N
    jButton6.setText(bundle.getString("MainView.jButton6.text")); // NOI18N
    jButton6.setName("jButton6"); // NOI18N

    javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
    jPanel6.setLayout(jPanel6Layout);
    jPanel6Layout.setHorizontalGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel6Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
            .addGap(4, 4, 4)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addComponent(jTextField7)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel10)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jTextField6)
                .addComponent(jTextField5)
                .addComponent(jTextField4)
                .addComponent(jTextField3))
            .addContainerGap())
        .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel6Layout.createSequentialGroup()
            .addGap(236, 236, 236)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel6Layout.createSequentialGroup()
                    .addComponent(jButton5)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton6))
                .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel6Layout.createSequentialGroup()
                    .addComponent(jLabel5)
                    .addGap(4, 4, 4)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel11)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(269, 269, 269))
    );
    jPanel6Layout.setVerticalGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel6Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel4))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel6))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel7))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel8))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jLabel9)
                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel10)
                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(12, 12, 12)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jLabel5)
                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel11)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jButton6)
                .addComponent(jButton5))
            .addContainerGap())
    );

    jLabel33.setText(bundle.getString("MainView.jLabel33.text")); // NOI18N
    jLabel33.setName("jLabel33"); // NOI18N

    jScrollPane11.setName("jScrollPane11"); // NOI18N

    jTextArea2.setText("");
    jTextArea2.setEditable(false);
    caret = (DefaultCaret)jTextArea2.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    jTextArea2.setEditable(false);
    jTextArea2.setColumns(20);
    jTextArea2.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
    jTextArea2.setRows(5);
    jTextArea2.setName("jTextArea2"); // NOI18N
    jScrollPane11.setViewportView(jTextArea2);

    javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
    jPanel15.setLayout(jPanel15Layout);
    jPanel15Layout.setHorizontalGroup(
        jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel15Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel15Layout.createSequentialGroup()
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel15Layout.createSequentialGroup()
                    .addComponent(jLabel33)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addComponent(jScrollPane11))
            .addContainerGap())
    );
    jPanel15Layout.setVerticalGroup(
        jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel15Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel33)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
            .addContainerGap())
    );

    jTabbedPane1.addTab(bundle.getString("MainView.jPanel15.TabConstraints.tabTitle"), jPanel15); // NOI18N

    javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
    mainPanel.setLayout(mainPanelLayout);
    mainPanelLayout.setHorizontalGroup(
        mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
    );
    mainPanelLayout.setVerticalGroup(
        mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jTabbedPane1)
    );

    menuBar.setName("menuBar"); // NOI18N

    fileMenu.setText(bundle.getString("MainView.fileMenu.text")); // NOI18N
    fileMenu.setName("fileMenu"); // NOI18N

    exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
    exitMenuItem.setText(bundle.getString("MainView.exitMenuItem.text")); // NOI18N
    exitMenuItem.setToolTipText(bundle.getString("MainView.exitMenuItem.toolTipText")); // NOI18N
    exitMenuItem.setName("exitMenuItem"); // NOI18N
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    helpMenu.setText(bundle.getString("MainView.helpMenu.text")); // NOI18N
    helpMenu.setName("helpMenu"); // NOI18N

    aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
    aboutMenuItem.setText(bundle.getString("MainView.aboutMenuItem.text")); // NOI18N
    aboutMenuItem.setName("aboutMenuItem"); // NOI18N
    helpMenu.add(aboutMenuItem);

    menuBar.add(helpMenu);

    statusPanel.setName("statusPanel"); // NOI18N

    statusMessageLabel.setText(bundle.getString("MainView.statusMessageLabel.text")); // NOI18N
    statusMessageLabel.setName("statusMessageLabel");
    statusMessageLabel.setText("Status");

    statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    statusAnimationLabel.setText(bundle.getString("MainView.statusAnimationLabel.text")); // NOI18N
    statusAnimationLabel.setName("statusAnimationLabel");
    statusAnimationLabel.setVisible(false);

    progressBar.setName("progressBar");

    jSeparator1.setName("jSeparator1"); // NOI18N

    javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
    statusPanel.setLayout(statusPanelLayout);
    statusPanelLayout.setHorizontalGroup(
        statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
        .addComponent(jSeparator1)
        .addGroup(statusPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(statusMessageLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 1160, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addComponent(statusAnimationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    statusPanelLayout.setVerticalGroup(
        statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(statusPanelLayout.createSequentialGroup()
            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(statusAnimationLabel)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(statusMessageLabel))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jFileChooser1.setCurrentDirectory(new File(".//data"));
    FileFilter filterXML = new ExtensionFileFilter("XML (Extensible Markup Language) (*.xml)", ".xml");
    jFileChooser1.setFileFilter(filterXML);
    jFileChooser1.setName("jFileChooser1"); // NOI18N

    jFileChooser2.setCurrentDirectory(new File(".//"));
    jFileChooser2.setFileSelectionMode(jFileChooser2.DIRECTORIES_ONLY);
    jFileChooser2.setName("jFileChooser2"); // NOI18N

    setComponent(mainPanel);
    setMenuBar(menuBar);
    setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jSpinner2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner2StateChanged
        PropertiesApp.getInstance().fileLoad(ParametersEnum.MALLET_PROPERTIE_FILE_DEFAULT_PATH.getValue());
        Integer value = new Integer(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_WORDS_PER_TOPIC.toString()));
        if ((Integer) jSpinner2.getValue() < value) {
            jSpinner2.setValue(value);
        }
    }//GEN-LAST:event_jSpinner2StateChanged

    private void jSpinner3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner3StateChanged
        PropertiesApp.getInstance().fileLoad(ParametersEnum.MALLET_PROPERTIE_FILE_DEFAULT_PATH.getValue());
        Integer value = new Integer(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_NUM_TOPICS.toString()));
        if ((Integer) jSpinner3.getValue() < value) {
            jSpinner3.setValue(value);
        }
    }//GEN-LAST:event_jSpinner3StateChanged

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        PropertiesApp.getInstance().fileLoad(ParametersEnum.MALLET_PROPERTIE_FILE_DEFAULT_PATH.getValue());
        Integer value = new Integer(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_ITERATIONS.toString()));
        if ((Integer) jSpinner1.getValue() < value) {
            jSpinner1.setValue(value);
        }
    }//GEN-LAST:event_jSpinner1StateChanged

    private void jSpinner4StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner4StateChanged
        if ((Integer) jSpinner4.getValue() < 0) {
            jSpinner4.setValue(0);
        }
    }//GEN-LAST:event_jSpinner4StateChanged

    private void jSpinner5StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner5StateChanged
        PropertiesApp.getInstance().fileLoad(ParametersEnum.MALLET_PROPERTIE_FILE_DEFAULT_PATH.getValue());
        Double value = new Double(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_ALPHA.toString()));
        if ((Double) jSpinner5.getValue() < value) {
            jSpinner5.setValue(value);
        }
    }//GEN-LAST:event_jSpinner5StateChanged

    private void jSpinner6StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner6StateChanged
        PropertiesApp.getInstance().fileLoad(ParametersEnum.MALLET_PROPERTIE_FILE_DEFAULT_PATH.getValue());
        Double value = new Double(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_BETA.toString()));
        if ((Double) jSpinner6.getValue() < value) {
            jSpinner6.setValue(value);
        }
    }//GEN-LAST:event_jSpinner6StateChanged

    private void jList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList2ValueChanged
        if (!evt.getValueIsAdjusting()) {
            if (jList2.getSelectedValue() != null) {
                splineChartGenData.generateData((ArrayList<TopicDTO>) jList2.getSelectedValuesList(), (Metric) jComboBox5.getSelectedItem());
            }
        }
    }//GEN-LAST:event_jList2ValueChanged

    private void jComboBox5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox5ItemStateChanged
        if (jList2.getSelectedValue() != null) {
            splineChartGenData.generateData((ArrayList<TopicDTO>) jList2.getSelectedValuesList(), (Metric) jComboBox5.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox5ItemStateChanged

    private void jSpinner8StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner8StateChanged
        Integer value = (Integer) jSpinner8.getValue();
        if (value > 0 && value <= (DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getCount()) {
            VersionDTO version = (VersionDTO) (DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).get(value);
            if (version != null) {
                showVersionTable(version);
            }
        } else {
            jSpinner8.setValue(new Integer(0));
            inicVersionTable();
        }
    }//GEN-LAST:event_jSpinner8StateChanged

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
        refreshTablesDocumentTopicMatrix();
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jSpinner9StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner9StateChanged
        Integer value = (Integer) jSpinner9.getValue();
        if (value > 0 && value <= (DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount()) {
            DocumentDTO document = (DocumentDTO) DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName()).get(value);
            if (document != null) {
                showDocumentTable(document);
            }
        } else {
            jSpinner9.setValue(new Integer(0));
            inicDocumentTable();
        }
    }//GEN-LAST:event_jSpinner9StateChanged

    private void jSpinner7StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner7StateChanged
        Integer value = (Integer) jSpinner7.getValue();
        if (value > 0 && value <= (DAOManager.getDAO(DAONameEnum.BUG_DAO.getName())).getCount()) {
            BugDTO bug = (BugDTO) (DAOManager.getDAO(DAONameEnum.BUG_DAO.getName())).get(value.intValue());
            ArrayList<CommentDTO> comments = (ArrayList<CommentDTO>) ((CommentDAO) DAOManager.getDAO(DAONameEnum.COMMENT_DAO.getName())).getComments(value);
            if (bug != null) {
                showBugTable(bug, comments);
            }
        } else {
            jSpinner7.setValue(0);
            inicBugTable();
        }
    }//GEN-LAST:event_jSpinner7StateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFileChooser jFileChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JSpinner jSpinner5;
    private javax.swing.JSpinner jSpinner6;
    private javax.swing.JSpinner jSpinner7;
    private javax.swing.JSpinner jSpinner8;
    private javax.swing.JSpinner jSpinner9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTable6;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private javax.swing.table.DefaultTableModel tableModel, tableModel2, tableModel3;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private File indexDir, xmlFile;

    private XYSplineChart splineChart;
    private MetricEvolutionGenerator splineChartGenData;

    private PieChart bugComponentDistribution;
    private BugComponentDistributionGenerator bugComponentDistributionData;

    private XYSplineChart BugDistributionChart;
    private BugCommentDistributionGenerator bugDistributionData;

    @Override
    public void update(Observable o, Object arg) {
        // Parser.           
        if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.Inic"))) {
            setProgressBar(100); // Ver con que lo inicio.
            setLabelBar(ResourceBundle.getBundle("view/Bundle").getString("Parser.Status"));
            jButton1.setEnabled(false);
            jButton3.setEnabled(false);
            jButton8.setEnabled(true);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.Run"))) {
            repaintProgressBar(progressBar.getValue() + 1);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Parser.Action.End"))) {
            bugComponentDistributionData.generateData();
            jButton1.setEnabled(true);
            jButton3.setEnabled(true);
            jButton8.setEnabled(false);
            setProgressBar(0);
            setLabelBar("");
        } else // Base de datos.
                if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("H2.Action.Inic"))) {
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Action.Inic"))) {
            setProgressBar((DAOManager.getDAO(DAONameEnum.BUG_DAO.getName())).getCount() + (DAOManager.getDAO(DAONameEnum.COMMENT_DAO.getName())).getCount());
            setLabelBar(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Status"));
            jButton11.setEnabled(false);
            jButton12.setEnabled(true);
            jButton13.setEnabled(false);
            jButton14.setEnabled(false);
            deleteTables();
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Action.Run"))) {
            jTextField8.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount()));
            repaintProgressBar(progressBar.getValue() + 1);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("DocGen.Action.End"))) {
            jTextField8.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount()));
            jButton11.setEnabled(true);
            jButton12.setEnabled(false);
            jButton13.setEnabled(true);
            setProgressBar(0);
            setLabelBar("");
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("H2.Action.Run.PutBug"))) {
            jTextField9.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.BUG_DAO.getName()).getCount())));
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("H2.Action.Run.PutComment"))) {
            jTextField10.setText(Integer.toString(DAOManager.getDAO(DAONameEnum.COMMENT_DAO.getName()).getCount()));
        } else // Indexador.
                if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Action.Inic"))) {
            setProgressBar((DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount());
            setLabelBar(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Status"));
            jButton2.setEnabled(false);
            jButton4.setEnabled(false);
            jButton7.setEnabled(true);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Action.Run"))) {
            repaintProgressBar(progressBar.getValue() + 1);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Indexer.Action.End"))) {
            jButton2.setEnabled(true);
            jButton4.setEnabled(true);
            jButton7.setEnabled(false);
            setProgressBar(0);
            setLabelBar("");
        } else // Buscador.
                if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Action.Inic"))) {
            setProgressBar(10);
            repaintProgressBar(8);
            setLabelBar(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Status"));
            jButton5.setEnabled(false);
            jButton6.setEnabled(true);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Action.Run"))) {
            //repaintProgressBar(progressBar.getValue() + 1);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("Searcher.Action.End"))) {
            jButton5.setEnabled(true);
            jButton6.setEnabled(false);
            setProgressBar(0);
            setLabelBar("");
        } else // DOCUMENT-TOPIC MATRIX.
//        if (((String) arg).equals(PostLDAMessages.INIC.getMessage())) {
//            setProgressBar((DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).getCount());
//            setLabelBar(PostLDAMessages.M00.getMessage());
//            jButton10.setEnabled(true);
//        } else if (((String) arg).equals(PostLDAMessages.RUN.getMessage())) {
//            repaintProgressBar(progressBar.getValue() + 1);
//        } else if (((String) arg).equals(PostLDAMessages.END.getMessage())) {
//            jButton10.setEnabled(false);
//            setProgressBar(0);
//            setLabelBar("");
//        } else // LDA. TOPIC-TERM MATRIX.
                if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("LDA.Action.Inic"))) {
            setProgressBar((Integer) jSpinner1.getValue());
            setLabelBar(ResourceBundle.getBundle("view/Bundle").getString("LDA.Status"));
            jButton9.setEnabled(false);
            jButton10.setEnabled(true);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("LDA.Action.Run"))) {
            repaintProgressBar(progressBar.getValue() + 1);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("LDA.Action.End"))) {
            jTextField12.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.TERM_DAO.getName())).getCount()));
            jButton9.setEnabled(true);
            jButton10.setEnabled(false);
            setProgressBar(0);
            setLabelBar("");
            iniTopicComboBox();
            refreshJLists();
            refreshTablesDocumentTopicMatrix();
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Action.Inic"))) { // LDA. VERSIONS. 
            setProgressBar((DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getCount());
            setLabelBar(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Status"));
            jButton13.setEnabled(false);
            jButton14.setEnabled(true);
            jButton11.setEnabled(false);
            jButton12.setEnabled(false);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Action.Run"))) {
            repaintProgressBar(progressBar.getValue() + 1);
        } else if (((String) arg).equals(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Action.End"))) {
            jTextField11.setText(Integer.toString((DAOManager.getDAO(DAONameEnum.VERSION_DAO.getName())).getCount()));
            bugDistributionData.generateData();
            setProgressBar(0);
            setLabelBar("");
            jButton13.setEnabled(true);
            jButton14.setEnabled(false);
            jButton11.setEnabled(true);
        } else if (o instanceof LuceneManager) {
            showIndexResults((String) arg);
        } else {
            showActivityMessage((String) arg);
        }
    }
}
