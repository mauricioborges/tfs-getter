package com.github.mauricioborges;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestSpecification {

    @Rule
    public ExpectedException exception=ExpectedException.none();
    private static Properties properties;


    @BeforeClass
    public static void loadRealProperties(){
        properties = new Properties();
        try {

            properties.load(TestSpecification.class.getClassLoader().getResourceAsStream("real.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void shouldReadRealFileAndPrintIt() throws IOException {
        VCSConnection connection = new TFS().at(properties.getProperty("tfs.server")).as(properties.getProperty("tfs.user")).with(properties.getProperty("tfs.password")).getConnection();
        Repository repository = connection.toRepositoryRoot(properties.getProperty("tfs.path"));
        FileInputStream fileInputStream = repository.getFile(properties.getProperty("tfs.file"));
        assertNotNull(fileInputStream);
        printLinesFromFile(fileInputStream);
        connection.close();
    }

    private void printLinesFromFile(FileInputStream fileInputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName("ISO-8859-1")));
        String line = null;
        line = reader.readLine();
        while(line != null){
            System.out.println(line);
            line = reader.readLine();
        }
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithUser() {
        exception.expect(WrongUsageException.class);
        assertNull(new TFS().as("x"));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithPassword(){
        exception.expect(WrongUsageException.class);
        assertNull(new TFS().with("x"));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithPath(){
        exception.expect(WrongUsageException.class);
        assertNull(new TFSVCSConnection(null).toRepositoryRoot(""));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithFileName(){
        exception.expect(WrongUsageException.class);
        assertNull(new TFSRepository(null, null,null).getFile("a"));
    }


    //TODO: test closing

}
