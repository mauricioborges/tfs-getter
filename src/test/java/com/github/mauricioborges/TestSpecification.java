package com.github.mauricioborges;

import com.github.mauricioborges.model.Repository;
import com.github.mauricioborges.model.VCSConnection;
import com.github.mauricioborges.model.exception.WrongUsageException;
import com.github.mauricioborges.tfs.TFS;
import com.github.mauricioborges.tfs.model.TFSRepository;
import com.github.mauricioborges.tfs.model.TFSVCSConnection;
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

    private static Properties properties;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void loadRealProperties() {
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
        long start = System.currentTimeMillis();
        VCSConnection connection = new TFS().at(properties.getProperty("tfs.server")).as(properties.getProperty("tfs.user")).with(properties.getProperty("tfs.password")).getConnection();
        Repository repository = connection.toRepositoryRoot(properties.getProperty("tfs.path"));
        FileInputStream fileInputStream = repository.getFile(properties.getProperty("tfs.file"));
        assertNotNull(fileInputStream);
        printLinesFromFile(fileInputStream);
        connection.close();
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis / 1000F;
        System.out.println(elapsedTimeMillis);
        System.out.println(elapsedTimeSec);
    }

    private void printLinesFromFile(FileInputStream fileInputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName("ISO-8859-1")));
        String line = null;
        line = reader.readLine();
        while (line != null) {
            //System.out.println(line);
            line = reader.readLine();
        }
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithUser() {
        exception.expect(WrongUsageException.class);
        assertNull(new TFS().as("x"));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithPassword() {
        exception.expect(WrongUsageException.class);
        assertNull(new TFS().with("x"));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithPath() {
        exception.expect(WrongUsageException.class);
        assertNull(new TFSVCSConnection(null).toRepositoryRoot(""));
    }

    @Test
    public void shouldFailIfTryToUseDifferentBuilderSyntaxWithFileName() {
        exception.expect(WrongUsageException.class);
        assertNull(new TFSRepository(null).getFile("a"));
    }


    //TODO: test closing

}
