# Autocorrect by Sierra Shaw

The user may type in a given word of their choice in the terminal and the autocorrector will attempt to come up with 
similar words to the typed word. I first filtered down the words in my large dictionary in two ways. I sort 
through the dictionary by utilizing tokenization. I will take a given nGram value (which is altered based on the length 
of the typed word) and use a polynomial rolling hash function to slide a window along the word to create unique hashes 
for letters of nGram length of the given word. I sorted each word into an array of arrayLists with the index being their
hashed values. Then, I sorted each word by their length so that given a short word, we could utilize its length rather
than assuming nGram tokenization will be present in such small words (which it isn't always. With the combination of these 
two, I was able to efficiently parse / filter down the possible words. From there, I calculate the lev distance between
each word and determine the most similar words to recommend to the user.

## Time Complexity

- T = length of typed word
- Q = 131071 (constant)
- L = longest length of word
- N = number of words in the dictionary
- M = number of words in the filtered candidate list (≤ N)

**organizeDict() - O(L * N)**
- First loop: initialize arrays → O(Q)
- Second loop: O(N) → placing words in lenDict
- Third loop (n-gram hashing): total hashes ≈ O(L * N) worst-case

**filterOptions() - O(N)**
- Hashing nGrams for typed word IF length > 5: For each hash worst-case O(N) if sortedDict poorly distributed
- Otherwise: up to T + threshold lists, each with ≤ N total size

**sortArray() - O(M)**
- Sorting M words based on distance

**lev() - O(T * L)**
- Levenshtein complexity = O(T * L) per comparison
- Dynamic programming → fills T * L matrix

**run()**
- Combining all of the functions
- Worst-Case: O(L * N + M * T * L)