import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

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
    public static int nGram = 3;
    public static int radix = 26;
    public static int q = 131071;
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
        ArrayList[] sortedDict = new ArrayList[q];
        for (int i = 0; i < q; i++) {
            sortedDict[i] = new ArrayList();
        }

        // For every word in the dictionary, hash by n-grams
        for (int i = 0; i < wordLen; i ++) {
            // Get first hash and add it to the new data Structure
            int hash = hash(words[i], nGram);
            sortedDict[hash].add(words[i]);

            // Continue to hash n-grams until none left
            int toLook = nGram;

            while (toLook < words[i].length()) {
                // Remove the first letter of the current hash
                hash = ((hash + q) - words[i].charAt(toLook - nGram) * (int)Math.pow(radix, nGram - 1) % q) % q;
                // Add the next letter to the hash
                hash = ((hash * radix) + words[i].charAt(toLook)) % q;
                // Add to dictionary data structure
                sortedDict[hash].add(words[i]);

                toLook += 1;
            }
        }

        // Create a hash set
        HashSet<String> filtered = new HashSet<String>();

        // Get first hash, add everything in that index to the hash set
        int hash = hash(typed, nGram);
        filtered.addAll(sortedDict[hash]);

        // Continue through the rest of the word
        int toLook = nGram;

        while (toLook < typed.length()) {
            // Remove the first letter of the current hash
            hash = ((hash + q) - typed.charAt(toLook - nGram) * (int)Math.pow(radix, nGram - 1) % q) % q;
            // Add the next letter to the hash
            hash = ((hash * radix) + typed.charAt(toLook)) % q;
            // Add to dictionary data structure
            filtered.addAll(sortedDict[hash]);

            toLook += 1;
        }

        // Convert to an ArrayList so that I can get values at each index
        ArrayList<String> filteredArray = new ArrayList<>(filtered);

        for (int i = 0; i < filteredArray.size(); i ++) {
            // Call lev function on each filtered word and then sort
            int[][] changes = new int[typed.length() + 1][filteredArray.get(i).length() + 1];
            int lev = lev(changes, typed, filteredArray.get(i));
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
                    if (insertIndex > 0 && lev == toReturnValues.get(insertIndex - 1)) {
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

    public int hash(String str, int length) {
        int hashed = 0;

        for(int i = 0; i < length; i++) {
            hashed = (radix * hashed + str.charAt(i)) % q;
        }

        return hashed;
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

    /*
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
     */

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