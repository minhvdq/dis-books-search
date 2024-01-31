import search.TFIDF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args){
        String document = "./resources/books/Rachel-and-Her-Children_-Homeless-Families-in-America-Jonathan-Kozol-Reprint_-2006-Broadway-97803073.txt";
        List<String> words = parseWordsFromFile(document);
        System.out.println(words.size() + " words in file " + document);
    }
    private static List<String> parseWordsFromFile(String document){
        FileReader file = null;
        try {
            file = new FileReader(document);
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
        System.out.println("No error happened");
        BufferedReader bufferedReader = new BufferedReader(file);
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        List<String> words = TFIDF.getWordFromLines(lines);
        return words;
    }
}
