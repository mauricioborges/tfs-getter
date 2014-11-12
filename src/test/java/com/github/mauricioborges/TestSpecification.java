package com.github.mauricioborges;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.junit.Assert.assertNull;

public class TestSpecification {

    @Rule
    public ExpectedException exception=ExpectedException.none();

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

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithUser() {
        exception.expect(WrongUsageException.class);
        assertNull(new TFS().withUser("x"));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithPassword(){
        exception.expect(WrongUsageException.class);
        assertNull(new TFS().andPassword("x"));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithPath(){
        exception.expect(WrongUsageException.class);
        assertNull(new TFS().on("x"));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithFileName(){
        exception.expect(WrongUsageException.class);
        assertNull(new TFS().get("x"));
    }
}
