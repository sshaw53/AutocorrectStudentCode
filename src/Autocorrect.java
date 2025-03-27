import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

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
     *
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    String[] words;
    int threshold;
    public static final int SHORT_CUTOFF = 5;
    public static final int RADIX = 26;
    public static final int LONGEST_LEN = 70;
    public static final int Q = 131071;
    public ArrayList<String> toReturn;
    public ArrayList<Integer> toReturnValues;

    public Autocorrect(String[] words, int threshold) {
        this.words = words;
        this.threshold = threshold;
        toReturn = new ArrayList<>();
        toReturnValues = new ArrayList<>();
    }

    public static void main(String[] args) {
        String[] words = loadDictionary("large");
        Autocorrect autocorrect = new Autocorrect(words, 2);
        autocorrect.run();
    }

    public void run() {
        int nGram = 2;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter a word: ");
            String typed = scanner.nextLine();
            System.out.print("Did you mean to say: ");

            int typedLen = typed.length();

            // Changing nGram based on length of the word
            if (typedLen >= 9 && typedLen < 12) {
                nGram = 3;
            }
            if (typedLen >= 12) {
                nGram = 4;
            }

            // Process to find and organize the potential words
            int wordLen = words.length;
            ArrayList[] sortedDict = new ArrayList[Q];
            ArrayList[] lenDict = new ArrayList[LONGEST_LEN];

            organizeDict(nGram, wordLen, sortedDict, lenDict);

            // Create a hash set
            HashSet<String> filtered = new HashSet<>();

            filterOptions(filtered, sortedDict, lenDict, typed, typedLen, nGram);

            // Convert to an ArrayList so that I can get values at each index
            ArrayList<String> filteredArray = new ArrayList<>(filtered);

            // Sort the arrayList
            sortArray(filteredArray, typed);

            // Print out possible words (had to add shorter options for if the options were less - for smaller dict)
            if (toReturn.toArray(new String[0]).length == 0) {
                System.out.println("No viable matches");
            }
            else if (toReturn.toArray(new String[0]).length == 1) {
                System.out.println(toReturn.toArray(new String[0])[0] + "?");
            }
            else if (toReturn.toArray(new String[0]).length == 2) {
                System.out.println(toReturn.toArray(new String[0])[0] + " or " + toReturn.toArray(new String[0])[1] + "?");
            }
            else {
                System.out.println(toReturn.toArray(new String[0])[0] + ", " + toReturn.toArray(new String[0])[1] + ", or " +
                        toReturn.toArray(new String[0])[2] + "?");
            }

            toReturn.clear();
            toReturnValues.clear();
        }
    }

    public void organizeDict(int nGram, int wordLen, ArrayList[] sortedDict, ArrayList[] lenDict) {
        for (int i = 0; i < Q; i++) {
            if (i < LONGEST_LEN) {
                lenDict[i] = new ArrayList();
            }
            sortedDict[i] = new ArrayList();
        }

        // For every word in dictionary, sort by length --> add to lenDict
        for (int i = 0; i < wordLen; i++) {
            lenDict[words[i].length()].add(words[i]);
        }

        // For every word in the dictionary, hash by n-grams --> add to sortedDict
        for (int i = 0; i < wordLen; i++) {
            // Get first hash and add it to the new data Structure
            if (words[i].length() > nGram) {
                int hash = hash(words[i], nGram);
                sortedDict[hash].add(words[i]);

                // Continue to hash n-grams until none left
                int toLook = nGram;

                while (toLook < words[i].length()) {
                    // Remove the first letter of the current hash
                    hash = ((hash + Q) - words[i].charAt(toLook - nGram) * (int) Math.pow(RADIX, nGram - 1) % Q) % Q;
                    // Add the next letter to the hash
                    hash = ((hash * RADIX) + words[i].charAt(toLook)) % Q;
                    // Add to dictionary data structure
                    sortedDict[hash].add(words[i]);

                    toLook += 1;
                }
            }
        }
    }

    public void filterOptions(HashSet<String> filtered, ArrayList[] sortedDict, ArrayList[] lenDict, String typed,
                              int typedLen, int nGram) {
        // Get first hash, add everything in that index to the hash set - ONLY do this when there are 6+ letters in typed
        if (typedLen > SHORT_CUTOFF) {
            int hash = hash(typed, nGram);
            filtered.addAll(sortedDict[hash]);

            // Continue through the rest of the word
            int toLook = nGram;

            while (toLook < typedLen) {
                // Remove the first letter of the current hash
                hash = ((hash + Q) - typed.charAt(toLook - nGram) * (int) Math.pow(RADIX, nGram - 1) % Q) % Q;
                // Add the next letter to the hash
                hash = ((hash * RADIX) + typed.charAt(toLook)) % Q;
                // Add to dictionary data structure
                filtered.addAll(sortedDict[hash]);

                toLook += 1;
            }
        }

        // Otherwise, compare to all words in the dictionary with length from 1 to typedLen + threshold
        else {
            for (int i = 1; i < typedLen + threshold; i++) {
                filtered.addAll(lenDict[i]);
            }
        }
    }

    public void sortArray(ArrayList<String> filteredArray, String typed) {
        for (int i = 0; i < filteredArray.size(); i++) {
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
                                filteredArray.get(i).compareTo(toReturn.get(insertIndex - 1)) < 0) {
                            insertIndex -= 1;
                        }
                    }

                    toReturn.add(insertIndex, filteredArray.get(i));
                    toReturnValues.add(insertIndex, lev);
                } else {
                    toReturn.add(filteredArray.get(i));
                    toReturnValues.add(lev);
                }
            }
        }
    }

    // Uses Horner's method to hash the 3-letter words
    public static int hash(String str, int length) {
        int hashed = 0;

        for (int i = 0; i < length; i++) {
            hashed = (RADIX * hashed + str.charAt(i)) % Q;
        }

        return hashed;
    }

    // Incorporating Lev's theorem
    public static int lev(int[][] changes, String str1, String str2) {
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
     * Runs a test from the tester file, AutocorrectTester.
     *
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distnace, then sorted alphabetically.
     */

    // Tester running
    public String[] runTest(String typed) {
        int nGram = 2;
        int wordLen = words.length;

        // Process to find and organize the potential words
        ArrayList[] sortedDict = new ArrayList[Q];
        ArrayList[] lenDict = new ArrayList[LONGEST_LEN];

        organizeDict(nGram, wordLen, sortedDict, lenDict);

        // Create a hash set
        HashSet<String> filtered = new HashSet<>();
        int typedLen = typed.length();

        filterOptions(filtered, sortedDict, lenDict, typed, typedLen, nGram);

        // Convert to an ArrayList so that I can get values at each index
        ArrayList<String> filteredArray = new ArrayList<>(filtered);

        // Sort the arrayList
        sortArray(filteredArray, typed);

        return toReturn.toArray(new String[0]);
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