import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author SIERRA SHAW
 */
public class Autocorrect {

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    String[] words;
    int threshold;
    ArrayList<String> toReturn = new ArrayList<>();
    ArrayList<Integer> toReturnValues = new ArrayList<>();

    public Autocorrect(String[] words, int threshold) {
        this.words = words;
        this.threshold = threshold;
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distnace, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        int wordLen = words.length;
        for (int i = 0; i < wordLen; i ++) {
            int[][] changes = new int[typed.length() + 1][words[i].length() + 1];
            int lev = lev(changes, typed, words[i]);
            if (lev <= threshold) {
                int insertIndex;

                if (!toReturnValues.isEmpty()) {
                    insertIndex = toReturnValues.size();

                    if (lev < toReturnValues.get(toReturnValues.size() - 1)) {
                        insertIndex = toReturnValues.size() - 1;

                        while (insertIndex > 0 && lev < toReturnValues.get(insertIndex - 1)) {
                            insertIndex -= 1;
                        }
                    }
                    if (lev == toReturnValues.get(insertIndex - 1)) {
                        while (insertIndex > 0 && lev == toReturnValues.get(insertIndex - 1) &&
                                words[i].compareTo(toReturn.get(insertIndex - 1)) < 0) {
                            insertIndex -= 1;
                        }
                    }

                    else {
                        insertIndex = toReturnValues.size();
                    }

                    toReturn.add(insertIndex, words[i]);
                    toReturnValues.add(insertIndex, lev);
                }
                else {
                    toReturn.add(words[i]);
                    toReturnValues.add(lev);
                }
            }
        }

        System.out.println(toReturn.size());
        for (String word: toReturn) {
            System.out.println(word);
        }

        return toReturn.toArray(new String[0]);
    }

    public int lev(int[][] changes, String str1, String str2) {
        int str1Len = str1.length();
        int str2Len = str2.length();

        changes[0][0] = 0;

        for (int i = 1; i <= str1Len; i++) {
            changes[i][0] = i;
        }

        for (int j = 1; j <= str2Len; j++) {
            changes[0][j] = j;
        }

        for (int i = 1; i <= str1Len; i++) {
            for (int j = 1; j <= str2Len; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    changes[i][j] = changes[i - 1][j - 1];
                }
                else {
                    changes[i][j] = 1 + Math.min(changes[i - 1][j - 1], Math.min(changes[i][j - 1], changes[i - 1][j]));
                }
            }
        }

        return changes[str1Len][str2Len];
    }

    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}