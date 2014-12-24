/**
 * Copyright (C) 2005 Univ. of Massachusetts Amherst, Computer Science Dept.
 * This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
 * http://www.cs.umass.edu/~mccallum/mallet This software is provided under the
 * terms of the Common Public License, version 1.0, as published by
 * http://www.opensource.org. For further information, see the file `LICENSE'
 * included with this distribution.
 *
 * A simple implementation of Latent Dirichlet Allocation using Gibbs sampling.
 * This code is slower than the regular Mallet LDA implementation, but provides
 * a better starting place for understanding how sampling works and for building
 * new topic models.
 *
 * @author David Mimno, Andrew McCallum
 *
 *
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
package algorithms;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.*;
import cc.mallet.types.*;
import cc.mallet.util.*;
import dao.DAOManager;
import dao.DAONameEnum;
import dto.BaseDTO;
import dto.DocumentDTO;
import dto.TopicDTO;
import java.io.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import logger.ThesisLogger;
import util.PropertiesApp;
import util.ParametersEnum;

public class SimpleLDAGibbs extends Observable implements Runnable {

    private static SimpleLDAGibbs instance;
    /**
     * Internal parameters of LDA
     */
    // Dates to get the document texts
    Date dateFrom, dateTo;
    // <IdDocLDA, IdDocDB>
    HashMap<Integer, Integer> docsId = new HashMap<Integer, Integer>();
    // The input acepted for LDA
    private InstanceList training;
    // The training instances and their topic assignments
    private ArrayList<TopicAssignment> data;
    // The alphabet for the input data
    private Alphabet alphabet;
    // The alphabet for the topics
    private LabelAlphabet topicAlphabet;
    // Pipes to transform the input
    private ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

    /**
     * External parameters of LDA
     */
    // Stop words
    private boolean removeStopWordrs;
    private File stopWordsFile;
    // Numbers of iterations
    private int numIerations;
    // The number of topics requested
    private int numTopics;
    // The size of the vocabulary
    private int numTypes;
    // Prior parameters
    private double alpha;  // Dirichlet(alpha,alpha,...) is the distribution over topics
    private double alphaSum;
    private double beta;   // Prior on per-topic multinomial distribution over words
    private double betaSum;
    public static final double DEFAULT_BETA = 0.01;
    /**
     * External parameters of LDA
     *
     * An array to put the topic counts for the current document. Initialized
     * locally below. Defined here to avoid garbage collection overhead.
     */
    // Statistics needed for sampling.    
    private int[][] typeTopicCounts;    // indexed by <feature index, topic index>
    private int[] tokensPerTopic;       // indexed by <topic index>
    private double[][] docTopicProbs;   // indexed by <document index, topic index>

    private int wordsPerTopic;
    private Randoms random;
    private NumberFormat formatter;
    protected boolean printLogLikelihood = false;

    public SimpleLDAGibbs() {
    }

    /**
     * @param numIerations Indicates the number of iterations will be applied.
     * @param numTopics Indicates the number of topics will be generated.
     * @param alpha Dirichlet(alpha,alpha,...) is the distribution over topics.
     * @param beta Prior on per-topic multinomial distribution over words.
     * @param wordsPerTopic Number of words per topic.
     * @param removeStopWords Indicates if stop words will be removed.
     * @param dateFrom Is the first date of the period.
     * @param dateTo Is the last date of the period.
     */
    public SimpleLDAGibbs(int numIerations, int numTopics, double alpha, double beta, int wordsPerTopic, boolean removeStopWords, Date dateFrom, Date dateTo) {
        this.numIerations = numIerations;
        this.numTopics = numTopics;
        this.alpha = alpha;
        this.alphaSum = alpha * numTopics;
        this.beta = beta;
        this.wordsPerTopic = wordsPerTopic;
        this.removeStopWordrs = removeStopWords;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        data = new ArrayList<TopicAssignment>();
        random = new Randoms();
        tokensPerTopic = new int[numTopics];
        formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(5);
        this.setParametersForPipes();
        this.addInstances(training);
    }

    public static SimpleLDAGibbs getInstance(int numIerations, int numTopics, double alpha, double beta, int wordsPerTopic, boolean removeStopWords, Date dateFrom, Date dateTo) {
        instance = new SimpleLDAGibbs(numIerations, numTopics, alpha, beta, wordsPerTopic, removeStopWords, dateFrom, dateTo);
        return instance;
    }

    public static SimpleLDAGibbs getInstance() {
        return instance;
    }

    @Override
    public void run() {
        try {
            if (dateFrom.before(dateTo)) {
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("LDA.Action.Inic"));
                DAOManager.getDAO(DAONameEnum.TERM_DAO.getName()).delete();
                DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName()).delete();
                this.sample(numIerations);
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("LDA.Action.End"));
            } else {
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("LDA.Action.Error3"));
            }
        } catch (IOException ex) {
            ThesisLogger.get().error("MalletSimpleLDAGibbs.run: " + ex.toString());
        }
    }

    /**
     * Stop the algorithm.
     */
    public void stop() {
        stopWordsFile = null;
        training = null;
        pipeList = null;
        numIerations = 0;
    }

    /**
     * @return The alphabet.
     */
    public Alphabet getAlphabet() {
        return alphabet;
    }

    /**
     * @return Label alphabet.
     */
    public LabelAlphabet getTopicAlphabet() {
        return topicAlphabet;
    }

    /**
     * @return Number of topics.
     */
    public int getNumTopics() {
        return numTopics;
    }

    /**
     * @return The training instances and their topic assignments
     */
    public ArrayList<TopicAssignment> getData() {
        return data;
    }

    /**
     * Methods for run the algorithm.
     *
     * @param iterations Indicates the number of iterations will be applied.
     * @throws IOException
     */
    private void sample(int iterations) throws IOException {
        docTopicProbs = new double[data.size()][numTopics];
        long start = System.currentTimeMillis();
        for (int iteration = 1; iteration <= iterations; iteration++) {
            setChanged();
            notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("LDA.Action.Run"));
            // Loop over every document in the corpus                 
            for (int doc = 0; doc < data.size(); doc++) {
                FeatureSequence tokenSequence = (FeatureSequence) data.get(doc).instance.getData();
                LabelSequence topicSequence = (LabelSequence) data.get(doc).topicSequence;
                sampleTopicsForOneDoc(tokenSequence, topicSequence, doc);
            }
        }
        long end = System.currentTimeMillis() - start;
        insertTopicsAndDocuments();
        setChanged();
        notifyObservers(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("LDA.Mensage1"), DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName()).getCount(), end));
    }

    /**
     * This execute the sample for one doc.
     *
     * @param tokenSequence Tokens from entry text documents.
     * @param topicSequence Token's topics.
     * @param doc Number id of the document.
     */
    private void sampleTopicsForOneDoc(FeatureSequence tokenSequence,
            FeatureSequence topicSequence, int doc) {
        int[] oneDocTopics = topicSequence.getFeatures();
        int[] currentTypeTopicCounts;
        int type, oldTopic, newTopic;
        // double topicWeightsSum;
        int docLength = tokenSequence.getLength();
        int[] localTopicCounts = new int[numTopics];
        // populate topic counts
        for (int position = 0; position < docLength; position++) {
            localTopicCounts[oneDocTopics[position]]++;
        }
        double score, sum;
        double[] topicTermScores = new double[numTopics];
        // Iterate over the positions (words) in the document 
        for (int position = 0; position < docLength; position++) {
            type = tokenSequence.getIndexAtPosition(position);
            oldTopic = oneDocTopics[position];
            // Grab the relevant row from our two-dimensional array
            currentTypeTopicCounts = typeTopicCounts[type];
            //	Remove this token from all counts. 
            localTopicCounts[oldTopic]--;
            tokensPerTopic[oldTopic]--;
            assert (tokensPerTopic[oldTopic] >= 0) : "old Topic " + oldTopic + " below 0";
            currentTypeTopicCounts[oldTopic]--;
            // Now calculate and add up the scores for each topic for this word
            sum = 0.0;
            // Here's where the math happens! Note that overall performance is 
            //  dominated by what you do in this loop.
            for (int topic = 0; topic < numTopics; topic++) {
                score = (alpha + localTopicCounts[topic])
                        * ((beta + currentTypeTopicCounts[topic])
                        / (betaSum + tokensPerTopic[topic]));
                sum += score;
                topicTermScores[topic] = score;
                docTopicProbs[doc][topic] = score;
            }
            for (int topic = 0; topic < numTopics; topic++) {
                docTopicProbs[doc][topic] = docTopicProbs[doc][topic] / sum;
            }
            // Choose a random point between 0 and the sum of all topic scores
            double sample = random.nextUniform() * sum;
            // Figure out which topic contains that point
            newTopic = -1;
            while (sample > 0.0) {
                newTopic++;
                sample -= topicTermScores[newTopic];
            }
            // Make sure we actually sampled a topic
            if (newTopic == -1) {
                throw new IllegalStateException("SimpleLDA: New topic not sampled.");
            }
            // Put that new topic into the counts
            oneDocTopics[position] = newTopic;
            localTopicCounts[newTopic]++;
            tokensPerTopic[newTopic]++;
            currentTypeTopicCounts[newTopic]++;
        }
    }

    /**
     * Save the topics and documents into the data base.
     */
    private void insertTopicsAndDocuments() {
        IDSorter[] sortedWords = new IDSorter[numTypes];
        for (int topicId = 0; topicId < numTopics; topicId++) {
            TopicDTO topic = new TopicDTO();
            for (int type = 0; type < numTypes && type < wordsPerTopic; type++) {
                sortedWords[type] = new IDSorter(type, typeTopicCounts[type][topicId]);
                if (sortedWords[type].getWeight() != 0) {
                    topic.putWord(alphabet.lookupObject(sortedWords[type].getID()).toString(), new BigDecimal(sortedWords[type].getWeight() / tokensPerTopic[topicId]));
                }
            }
            for (int doc = 0; doc < data.size(); doc++) {
                topic.putDoc(docsId.get(doc), new BigDecimal(docTopicProbs[doc][topicId]));
            }
            (DAOManager.getDAO(DAONameEnum.TOPIC_DAO.getName())).insert(topic);
        }
    }

    /**
     * Prepare the pipes for process the text of each document. An instance
     * contains four generic fields of predefined name: "data", "target",
     * "name", and "source". "Data" holds the data represented `by the instance,
     * "target" is often a label associated with the instance, "name" is a short
     * identifying name for the instance (such as a filename), and "source" is
     * human-readable sourceinformation, (such as the original text).
     */
    private void setParametersForPipes() {
        // Create the pipes for text processing.  
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        if (removeStopWordrs) {
            PropertiesApp.getInstance().fileLoad(".\\properties\\malletLDA.properties");
            stopWordsFile = new File(PropertiesApp.getInstance().getPropertie(ParametersEnum.LDA_STOP_WORDS.toString()));
            pipeList.add(new TokenSequenceRemoveStopwords(stopWordsFile, "UTF-8", false, false, false));
        }
        pipeList.add((Pipe) new TokenSequence2FeatureSequence());
        training = new InstanceList(new SerialPipes(pipeList));
        /**
         * Getting the documents between the dates. Then put each doc into de
         * the pipe.
         */
        List<BaseDTO> docs = (DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getBeetwDates(new java.sql.Date(dateFrom.getTime()), new java.sql.Date(dateTo.getTime()));
        int idDocLDA = 0;
        for (BaseDTO baseDto : docs) {
            DocumentDTO doc = (DocumentDTO) baseDto;
            docsId.put(idDocLDA, doc.getId());
            training.addThruPipe(new StringArrayIterator(new String[]{removeChars(doc.getText())}));
            idDocLDA++;
        }

    }

    /**
     * This remove some noice from the text documents.
     */
    private String removeChars(String text) {
        // Regular exp for url.
        //text = text.replaceAll("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", text); 
        // Regular exp for 24 hours time.
        //text = text.replaceAll("([01]?[0-9]|2[0-3]):[0-5][0-9]", text);
        // Regular exp for 12 hours time.
        //text = text.replaceAll("(1[012]|[1-9]):[0-5][0-9](\\\\s)?(?i)(am|pm)", text);
        // Regular exp for 12 hours time.
        text = text.replaceAll("^(\\d+\\\\.)?(\\d+\\\\.)?(\\\\*|\\d+)$", text);
        text = text.replaceAll("\\.", " ");
        text = text.replaceAll(":", " ");
        text = text.replaceAll("/", " ");
        text = text.replaceAll("\\(", " ");
        text = text.replaceAll("\\)", " ");
        text = text.replaceAll("\"", " ");
        text = text.replaceAll("\\?", " ");
        text = text.replaceAll("\\!", " ");
        text = text.replaceAll("-", " ");
        text = text.replaceAll("\\\\", " ");
        text = text.replaceAll("_", " ");
        return text;
    }

    /**
     * Put the instances into topic data.
     *
     * @param training Is the list of intances.
     */
    private void addInstances(InstanceList training) {
        if (training.size() > 0) {
            alphabet = training.getDataAlphabet();
            numTypes = alphabet.size();
            betaSum = beta * numTypes;
            typeTopicCounts = new int[numTypes][numTopics];
            for (Instance instance : training) {
                FeatureSequence tokens = (FeatureSequence) instance.getData();
                LabelSequence topicSequence = new LabelSequence(topicAlphabet, new int[tokens.size()]);
                int[] topics = topicSequence.getFeatures();
                for (int position = 0; position < tokens.size(); position++) {
                    int topic = random.nextInt(numTopics);
                    topics[position] = topic;
                    tokensPerTopic[topic]++;
                    int type = tokens.getIndexAtPosition(position);
                    typeTopicCounts[type][topic]++;
                }
                TopicAssignment t = new TopicAssignment(instance, topicSequence);
                data.add(t);
            }
        } else {

        }
    }

}
