import java.util.*; // import utility classes
// ================= TRIE NODE =================
class TrieNode{
    Map<Character,TrieNode> children; // child nodes
    Map<String,Integer> freqMap; // sentence -> frequency
    public TrieNode(){
        children=new HashMap<>(); // initialize children
        freqMap=new HashMap<>(); // initialize frequency map
    }
}
// ================= SEARCH AUTOCOMPLETE =================
class SearchAutocomplete{
    private TrieNode root; // trie root
    private StringBuilder currentInput; // current typed prefix
    // ================= CONSTRUCTOR =================
    // Time Complexity: O(total characters in phrases)
    public SearchAutocomplete(String[] phrases,int[] counts){
        root=new TrieNode(); // create root
        currentInput=new StringBuilder(); // initialize buffer
        for(int i=0;i<phrases.length;i++)
            insert(phrases[i],counts[i]); // load historical data
    }
    // ================= INSERT SENTENCE =================
    // Time Complexity: O(L)
    private void insert(String sentence,int count){
        TrieNode curr=root;
        for(char c:sentence.toCharArray()){ // traverse characters
            curr.children.putIfAbsent(c,new TrieNode());
            curr=curr.children.get(c);
            curr.freqMap.put(sentence,curr.freqMap.getOrDefault(sentence,0)+count);
        }
    }
    // ================= GET SUGGESTIONS =================
    // Time Complexity: O(P + N log N)
    public List<String> getSuggestions(char ch){
        // end of sentence
        if(ch=='#'){
            insert(currentInput.toString(),1); // save new sentence
            currentInput=new StringBuilder(); // reset input
            return new ArrayList<>(); // return empty list
        }
        currentInput.append(ch); // append character
        TrieNode curr=root;
        // traverse trie using prefix
        for(char c:currentInput.toString().toCharArray()){
            if(!curr.children.containsKey(c))
                return new ArrayList<>(); // no match
            curr=curr.children.get(c);
        }
        // fetch candidate sentences
        List<Map.Entry<String,Integer>> candidates=new ArrayList<>(curr.freqMap.entrySet());
        // sort by frequency desc, ASCII asc
        Collections.sort(candidates,(a,b)->{
            if(!a.getValue().equals(b.getValue()))
                return b.getValue()-a.getValue(); // higher freq first
            return a.getKey().compareTo(b.getKey()); // ASCII order
        });
        // pick top 3
        List<String> result=new ArrayList<>();
        for(int i=0;i<Math.min(3,candidates.size());i++)
            result.add(candidates.get(i).getKey());
        return result;
    }
}

// ================= DRIVER =================
public class Main{
    public static void main(String[] args){

        String[] phrases={"i love you","island","ironman","i love coding"};
        int[] counts={5,3,2,2};
        SearchAutocomplete system=new SearchAutocomplete(phrases,counts);
        System.out.println(system.getSuggestions('i')); // suggestions
        System.out.println(system.getSuggestions(' '));
        System.out.println(system.getSuggestions('l'));
        System.out.println(system.getSuggestions('#')); // save sentence
    }
}