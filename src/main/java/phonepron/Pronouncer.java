package phonepron;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author baal
 */
public class Pronouncer {

    private final HashMap<String, ArrayList<String>> woc_;
    private final HashMap<String, String> numkey_;
    private final int PHONE_LENGTH = 11;
    private final int wordCount_;
    private final String[] words_;

    public Pronouncer() throws IOException {
        // read words
        words_ = readWordsFromProps();
        wordCount_ = words_.length;
        numkey_ = getNumKeyMap();

        // init hash
        woc_ = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < wordCount_; i++) {
            String wocWord = words_[i];
            String numWord = wordToNum(wocWord);

            ArrayList<String> words = woc_.get(numWord);
            if (words == null || words.size() < 1) {
                words = new ArrayList<String>();
            }
            words.add(wocWord);

            woc_.put(numWord, words);
        }

    }

    public List<String> getPhoneNames(String phone) {
        HashSet<String> pronList = new HashSet<String>();
        
        for(int namePartCount=PHONE_LENGTH; namePartCount>1; namePartCount--) {
            pronList.addAll(getPhoneNamesForPartCount(phone, namePartCount));
        }
        
        List<String> sortedList = new ArrayList(pronList);
        Collections.sort(sortedList);
        
        return sortedList;
    }

    private HashSet<String> getPhoneNamesForPartCount(String phone, int partCount) {
        HashSet<String> pronList = new HashSet<String>();
        
        int[] ccount = new int[partCount];
        for (int i=0; i < partCount; i++) {
            ccount[i] = 1;
        }
        ccount[0] = PHONE_LENGTH - partCount + 1;
        
        while(true) {
            HashSet<String> prons = getPhoneNamesForParts(phone, ccount);
            if(prons != null && prons.size() > 0) {
                pronList.addAll(prons);
            }
            if(!RorateParts(ccount)) {
                break;
            }
        }
        
        return pronList;
    }
    
    private HashSet<String> getPhoneNamesForParts(String phone, int[] ccount) {
        List<String> phoneParts = splitPhoneToParts(phone, ccount);
        
        List<List<String>> pronParts = new ArrayList<List<String>>(); // по кол-ву частей
        for(int i=0; i<phoneParts.size(); i++) {
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
            vars.add(nums); // добавляет сами цифры в ответ
        }
        
        ArrayList<String> words = woc_.get(nums);
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
    
    private boolean RorateParts(int[] ccount) {
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
            ccount[0] = PHONE_LENGTH - arraySumWoLast(ccount); // остаток
            return true;
        }
        
        return !isOverflow;
    }
    
    private int arraySumWoLast(int[] ccount) {
        int sum = 0;
        for(int i=1; i<ccount.length; i++) {
            sum += ccount[i];
        }
        return sum;
    }
    
    private HashMap<String, String> getNumKeyMap() {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("0", "0");
        m.put("1", "1");
        m.put("а", "2");
        m.put("б", "2");
        m.put("в", "2");
        m.put("г", "2");
        m.put("д", "3");
        m.put("е", "3");
        m.put("ж", "3");
        m.put("з", "3");
        m.put("и", "4");
        m.put("й", "4");
        m.put("к", "4");
        m.put("л", "4");
        m.put("м", "5");
        m.put("н", "5");
        m.put("о", "5");
        m.put("п", "5");
        m.put("р", "6");
        m.put("с", "6");
        m.put("т", "6");
        m.put("у", "6");
        m.put("ф", "7");
        m.put("х", "7");
        m.put("ц", "7");
        m.put("ч", "7");
        m.put("ш", "8");
        m.put("щ", "8");
        m.put("ъ", "8");
        m.put("ы", "8");
        m.put("ь", "9");
        m.put("э", "9");
        m.put("ю", "9");
        m.put("я", "9");
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

    private String[] readWordsFromProps() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = getClass().getResourceAsStream("wocabulary.properties");
        if(inputStream == null) {
            throw new RuntimeException(".props not found");
        }
        
        properties.load(inputStream);
        String ww = properties.getProperty("wocwords");
        String[] words = ww.split(",");
        return words;
    }
}
