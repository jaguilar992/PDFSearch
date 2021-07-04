package test;

import files.Database;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        var dir = "/Users/helloiconic/Downloads/constancias";
//        Directory.indexDirectory(dir);
        Database db = new Database();
        var query = "";
        var results = db.search("%"+query+"%", dir);
        System.out.println(results.size());
        db.close();
//        Directory.createZipFile("/Users/helloiconic/Downloads/", query, results);

    }
}
