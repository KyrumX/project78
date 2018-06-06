package team.smartwaiter;

import java.util.*;

/**
 * Created by Selim on 6/6/2018.
 */

public class Logic {
    List<String> foodlist;
    ArrayList<String> query;
    public Logic(List<String> foodlist, ArrayList<String> query){
        this.foodlist = foodlist;
        this.query = query;
    }

    public Hashtable<String, String> generate(){
        Hashtable<String, String> pairs = new Hashtable<>();
        List<String> amount = Arrays.asList("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten");

        Hashtable<Integer, String> amounts = new Hashtable<>();
        for(int i = 1; i <= amount.size(); i++){
            amounts.put(i, amount.get(i - 1));
        }

        Set<Integer> numberset = amounts.keySet();

        for (String line : query) {
            String[] queryToList = line.toLowerCase().split(" ");

            // This is the first check to find any food without any amount indicator and add it|them directly
            for (int i = 0; i < queryToList.length; i++) {
                String currentword = queryToList[i];
                System.out.println(currentword);
                for (String food : this.foodlist) {
                    if (i >= 1) {
                        int totalamount = getAmount(numberset, queryToList[i - 1], amounts);
                        if (currentword.contains(food) && totalamount == 0) {
                            pairs.put(food, "1");
                        } else if (currentword.contains(food) && totalamount != 0) {
                            pairs.put(food, Integer.toString(totalamount));
                        }
                    } else {
                        if (currentword.contains(food)) {
                            pairs.put(food, "1");
                        }
                    }
                }
            }
        }

        return pairs;
    }

    private int getAmount(Set<Integer> numberset, String word, Hashtable<Integer, String> numlist){
        for (Integer am : numberset){
            if(word.equals(am.toString()) | word.equals(numlist.get(am))) {
                return am;
            } else if (word.equals("to") | word.equals("into")){
                // Aaron maak ff een switch case hiervan ty
                return 2;
            } else if(word.equals("for")){
                return 4;
            }
        }
        return 0;
    }

}
