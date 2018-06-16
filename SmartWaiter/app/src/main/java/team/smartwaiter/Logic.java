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

    public HashMap<String, Integer> generate(){
        boolean hasFoundAmount = false;
        int ttl = 0;
        HashMap<String, Integer> pairs = new HashMap<>();
        List<String> amount = Arrays.asList("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten");

        Hashtable<Integer, String> amounts = new Hashtable<>();
        for(int i = 1; i <= amount.size(); i++){
            amounts.put(i, amount.get(i - 1));
        }

        Set<Integer> numberset = amounts.keySet();
        System.out.println("query: " + query);
        for (String line : query) {
            String[] queryToList = line.toLowerCase().split(" ");
            // This is the first check to find any food without any amount indicator and add it|them directly
            for (int i = 0; i < queryToList.length; i++) {
                String currentword = queryToList[i];
                boolean foundAmount = false;
                for (String food : this.foodlist) {
                    if (i >= 1) {
                        String combinedword = queryToList[i-1] + " " + queryToList[i];
//                        ttl = (combinedword.equals(food)) ? getAmount(numberset, queryToList[i - 2], amounts) : getAmount(numberset, queryToList[i - 1], amounts);

//                        System.out.println("total amount : " + ttl);
                        if (!foundAmount) {
                            int totalamount = (combinedword.equals(food)) ? getAmount(numberset, queryToList[i - 2], amounts) : getAmount(numberset, queryToList[i - 1], amounts);
                            if (totalamount > 0){
                                foundAmount = true;
                                ttl = totalamount;
                            }
                        }

                        if(combinedword.equals(food)){
                            System.out.println("Combined food: " + combinedword);
//                            System.out.println("BEGINNING LOOPING THROUGH QUERYLIST ________________");
//                            for (String lel : queryToList){
//                                System.out.println("query to list: " + lel);
//                            }
//                            System.out.println("________________END LOOPING THROUGH QUERYLIST ");
                            System.out.println("querytolist - 2: " + queryToList[i - 2]);

                            if(ttl > 0){
                                System.out.println("putting " + ttl + " in pairs");
                                pairs.put(food, ttl) ;
                                break;
                            } else {
                                System.out.println("putting 1 in pairs");
                                pairs.put(food, 1);
                                break;
                            }
                        } else {
                            if (currentword.contains(food) && ttl == 0) {
                                pairs.put(food, 1);
                            } else if (currentword.contains(food) && ttl != 0) {
                                pairs.put(food, ttl);
                            }
                        }
                    } else {
                        if (currentword.contains(food)) {
                            pairs.put(food, 1);
                        }
                    }
                }
            }
        }
        System.out.println("these are the pairs: " + pairs);
        return pairs;
    }

    private int getAmount(Set<Integer> numberset, String word, Hashtable<Integer, String> numlist){
        System.out.println("word: " + word);
        for (Integer am : numberset){
            System.out.println("checking if word " + word + " is equal to " + am.toString() + " | " + numlist.get(am));
            if(word.equals(am.toString()) | word.equals(numlist.get(am))) {
                System.out.println("is in fact equal");
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
