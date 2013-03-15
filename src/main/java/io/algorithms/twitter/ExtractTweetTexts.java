package io.algorithms.twitter;
/**
 * Copyright 2005 The Apache Software Foundation
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


import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Split the Reuters SGML documents into Simple Text files containing: Title, Date, Dateline, Body
 */
public class ExtractTweetTexts
{
    private File tweetsJson;
    private File outputDir;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public ExtractTweetTexts(File tweeterJson, File outputDir)
    {
        this.tweetsJson = tweeterJson;
        this.outputDir = outputDir;
        System.out.println("Deleting all files in " + outputDir);
        File [] files = outputDir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            files[i].delete();
        }

    }

    Pattern EXTRACTION_PATTERN = Pattern.compile("<TITLE>(.*?)</TITLE>|<DATE>(.*?)</DATE>|<BODY>(.*?)</BODY>");

    private static String[] META_CHARS
            = {"&", "<", ">", "\"", "'"};

    private static String[] META_CHARS_SERIALIZATIONS
            = {"&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};

    private static JsonPath path = JsonPath.compile("$..twitter");
    /**
     * Override if you wish to change what is extracted
     */
    protected void extract()
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader(tweetsJson));
            String json = br.readLine();
            while ((json = br.readLine()) != null) {
                List<Object> tweets = path.read(json);
                for (Object obj : tweets) {
                    System.out.println("obj = " + obj);
                    if (obj.getClass() != JSONObject.class)
                        continue;
                    JSONObject jsonObject = (JSONObject) obj;
                    String id = (String) jsonObject.get("id");
                    if (id == null)
                        continue;
                    String text = (String) jsonObject.get("text");
                    FileWriter fr = new FileWriter(new File(outputDir.getCanonicalPath() + File.separator + id + ".txt"));
                    fr.write(text);
                    fr.close();

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            printUsage();
        }
        File tweetsJson = new File(args[0]);
        try {
            System.out.println(tweetsJson.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if (tweetsJson.exists())
        {
            File outputDir = new File(args[1]);
            outputDir.mkdirs();
            ExtractTweetTexts extractor = new ExtractTweetTexts(tweetsJson, outputDir);
            extractor.extract();
        }
        else
        {
            printUsage();
        }
    }

    private static void printUsage()
    {
        System.err.println("Usage: java -cp <...> io.algorithms.twitter.ExtractTweeterTexts <path to twitter json file> <output path>");
    }
}
