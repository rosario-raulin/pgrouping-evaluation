package de.raulin.rosario;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    public static final String XPATH_SAMPLE_QUERY
            = "//time[../algorithm='%s' and ../distribution='%s' and ../cardinality='%s' and ../number-of-tuples='%d']";
    public static final int[] TUPLE_SIZES = {  1, 8, 16, 32, 64, 128, 256, 512 };
    public static final int PRUNE_OFFSET = 5;

    public static int tupleNumber(int size) {
        return (size * 1024 * 1024) / 8;
    }

    public static List<Integer> sortedTimeValues(NodeList nlst) {
        List<Integer> lst = new ArrayList<Integer>(nlst.getLength());

        for (int i = 0; i < nlst.getLength(); ++i) {
            Integer x = Integer.parseInt(nlst.item(i).getTextContent());
            lst.add(x);
        }

        Collections.sort(lst);
        return lst;
    }

    public static void printUsage() {
        System.err.println("usage: Main input-xml-path cardinality distribution algorithm");
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            printUsage();
        } else {
            String cardinality = args[1];
            String distribution = args[2];
            String algorithm = args[3];

            DocumentBuilderFactory dfb = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder documentBuilder = dfb.newDocumentBuilder();
                Document doc = documentBuilder.parse(args[0]);

                XPathFactory xPathFactory = XPathFactory.newInstance();

                for (int size : TUPLE_SIZES) {
                    int numberOfTuples = tupleNumber(size);

                    XPath xpath = xPathFactory.newXPath();
                    String xpathRaw = String.format(XPATH_SAMPLE_QUERY, algorithm, distribution, cardinality, numberOfTuples);
                    XPathExpression xPathExpression = xpath.compile(xpathRaw);

                    NodeList nodeList = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
                    List<Integer> sortedTimeValues = sortedTimeValues(nodeList);

                    Integer sum = 0;
                    for (int i = PRUNE_OFFSET; i < sortedTimeValues.size() - PRUNE_OFFSET; ++i) {
                        sum += sortedTimeValues.get(i);
                    }

                    Integer mean = sum / (sortedTimeValues.size() - 2 * PRUNE_OFFSET);
                    System.out.println(numberOfTuples + " " + mean);
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
    }
}
