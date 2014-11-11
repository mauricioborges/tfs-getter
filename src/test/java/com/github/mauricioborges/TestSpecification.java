package com.github.mauricioborges;

import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

public class TestSpecification {

    @Test
    public void shouldReadRealFile() {
        Properties properties = new Properties();
        try {

            properties.load(getClass().getClassLoader().getResourceAsStream("real.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileInputStream fileInputStream = new TFS().at(properties.getProperty("tfs.server")).withUser(properties.getProperty("tfs.user")).andPassword(properties.getProperty("tfs.password")).on(properties.getProperty("tfs.path")).get(properties.getProperty("tfs.file"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName("ISO-8859-1")));
        String line = null;
        try {
            line = reader.readLine();
            while(line != null){
                System.out.println(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
