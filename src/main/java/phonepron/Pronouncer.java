package phonepron;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class Pronouncer {

    private final HashMap<String, ArrayList<String>> voc_;
    private HashMap<String, String> numkey_;
    private String[] words_;
    private final int wordCount_;

    public Pronouncer(Lang language) throws IOException {
        // read words_ & numkey_
        readVocabulary(language);
        wordCount_ = words_.length;

        // init words hash
        voc_ = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < wordCount_; i++) {
            String wocWord = words_[i];
            String numWord = wordToNum(wocWord);

            ArrayList<String> words = voc_.get(numWord);
            if (words == null || words.size() < 1) {
                words = new ArrayList<String>();
            }
            words.add(wocWord);

            voc_.put(numWord, words);
        }

    }

    public List<String> getPhoneNames(String phone) {
        HashSet<String> pronList = new HashSet<String>();
        
        for(int namePartCount=phone.length(); namePartCount>1; namePartCount--) {
            pronList.addAll(getPhoneNamesForPartCount(phone, namePartCount));
        }
        
        List<String> sortedList = new ArrayList(pronList);
        Collections.sort(sortedList);
        
        return sortedList;
    }

    private HashSet<String> getPhoneNamesForPartCount(String phone, int partCount) {
        int phoneLength = phone.length();
                
        HashSet<String> pronList = new HashSet<String>();
        
        int[] ccount = new int[partCount];
        for (int i = 0; i < partCount; i++) {
            ccount[i] = 1;
        }
        ccount[0] = phoneLength - partCount + 1;
        
        while(true) {
            HashSet<String> prons = getPhoneNamesForParts(phone, ccount);
            if(prons != null && prons.size() > 0) {
                pronList.addAll(prons);
            }
            if(!RorateParts(ccount, phoneLength)) {
                break;
            }
        }
        
        return pronList;
    }
    
    private HashSet<String> getPhoneNamesForParts(String phone, int[] ccount) {
        List<String> phoneParts = splitPhoneToParts(phone, ccount);
        
        List<List<String>> pronParts = new ArrayList<List<String>>();
        for(int i = 0; i < phoneParts.size(); i++) {
            List<String> partVariations = getpartVariationsForNums(phoneParts.get(i));
            pronParts.add(partVariations);
        }
        
        // combine parts !!!
        HashSet<String> pronList = new HashSet<String>();
        combineParts(pronParts, "", pronList, 0, phone);
        
        return pronList;
    }
    
    private void combineParts(List<List<String>> pronParts, String fullWord, HashSet<String> pronList, int level, String phone) {
        if(level >= pronParts.size()) {
            if(!phone.equals(fullWord)) {
                pronList.add(fullWord);
            }
            return;
        }
        List<String> levelWords = pronParts.get(level);
                
        for(int i=0; i<levelWords.size(); i++) {
            String curFullWord = fullWord + levelWords.get(i);
            combineParts(pronParts, curFullWord, pronList, level+1, phone);
        }
    }
    
    private List<String> getpartVariationsForNums(String nums) {
        List<String> vars = new ArrayList<String>();
        
        if(nums.length() >= 1) {
            vars.add(nums); // add numbers inself in answer
        }
        
        ArrayList<String> words = voc_.get(nums);
        if(words != null && words.size() > 0) {
            vars.addAll(words);
        }
        
        return vars;
    }
    
    private List<String> splitPhoneToParts(String phone, int[] ccount) {
        List<String> phoneParts = new ArrayList<String>();
        
        int startInd = 0;
        for(int i=0; i<ccount.length; i++) {
            phoneParts.add(phone.substring(startInd, startInd + ccount[i]));
            startInd += ccount[i];
        }        
        
        return phoneParts;
    }
    
    private boolean RorateParts(int[] ccount, int phoneLength) {
        if(ccount.length < 2) {
            return false;
        }
        
        boolean isOverflow = false;
        for(int i=1; i<ccount.length; i++) {
            if(ccount[i-1] == 1) {
                isOverflow = true;
                continue;
            }
            // here ccount[i-1] > 1
            if(!isOverflow) {
                ccount[i-1]--;
                ccount[i]++;
                return true;
            }
            // isOverflow
            ccount[i]++;
            // reset last :)
            for(int j=i-1; j>0; j--) {
                ccount[j] = 1;
            }
            ccount[0] = phoneLength - arraySumWithoutLast(ccount); // last part
            return true;
        }
        
        return !isOverflow;
    }
    
    private int arraySumWithoutLast(int[] ccount) {
        int sum = 0;
        for(int i = 1; i < ccount.length; i++) {
            sum += ccount[i];
        }
        return sum;
    }
    
    private HashMap<String, String> getNumKeyMap(String[] nummapping) {
        HashMap<String, String> m = new HashMap<String, String>();
        int mapCount = nummapping.length;
        if(mapCount % 2 > 0) {
            throw new RuntimeException("nummapping must have even count of elements");
        }
        
        for(int i = 0; i < mapCount; i += 2) {
            m.put(nummapping[i], nummapping[i+1]);
        }
        return m;
    }

    private String wordToNum(String w) {
        StringBuilder ss = new StringBuilder();

        for (int i = 0; i < w.length(); i++) {
            String wordChar = Character.toString(w.charAt(i));
            String numChar = numkey_.get(wordChar);
            ss.append(numChar);
        }

        return ss.toString();
    }

    private void readVocabulary(Lang language) throws IOException {
        String propFileName = "vocabulary_" + language.name() + ".properties";
        Properties properties = new Properties();
        InputStream inputStream = getClass().getResourceAsStream(propFileName);
        if(inputStream == null) {
            throw new RuntimeException(propFileName + " not found");
        }
        
        properties.load(inputStream);
        String ww = properties.getProperty("wocwords");
        words_ = ww.split(",");
        
        String nummap = properties.getProperty("nummapping");
        String[] nummaparray = nummap.split(",");
        numkey_ = getNumKeyMap(nummaparray);
    }
}
