package team.smartwaiter.tools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.List;

public class GeneralTools {
    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static String outputMoney(String money){
        System.out.println(money);

        String[] cash = money.split("\\.");

        String dollars = cash[0];
        String cents = cash[1];

        String statement = "";

        if (Integer.parseInt(dollars) == 1){
            statement += dollars + " euro";

            if (!cents.equals("00")){
                statement += " and " + (Integer.parseInt(cents) < 10 ? cents.substring(1,2) : cents) + (((Integer.parseInt(cents.substring(1, 2)) > 1) | (Integer.parseInt(cents.substring(0, 1)) > 0) ? " cents" : " cent"));
            }

        } else if (Integer.parseInt(dollars) > 1) {
            statement += dollars + " euros";

            if (!cents.equals("00")){
                statement += " and " + (Integer.parseInt(cents) < 10 ? cents.substring(1,2) : cents) + ((Integer.parseInt(cents.substring(1, 2)) > 1 | (Integer.parseInt(cents.substring(0, 1)) > 0) ? " cents" : " cent"));
            }
        } else {
            statement = "";

            if (cents.equals("00")){
                statement += " free";
            } else {
                statement += " " + (Integer.parseInt(cents) < 10 ? cents.substring(1,2) : cents) + ((Integer.parseInt(cents.substring(1, 2)) > 1 | (Integer.parseInt(cents.substring(0, 1)) > 0) ? " cents" : " cent"));

            }
        }

        return statement;
    }

    public static String checkForWords(List<String> output, List<String> words, boolean... force_ambiguity) {
        boolean ambiguity = (force_ambiguity.length != 0);

        /*

        The boolean force_ambiguity tells us if we can return the same exact word we found
        or if we have to return the keyword linked to that word. The keyword in our case is the
        first word given in the list words.

        */

        for (String line : output) {
            for (String word : words) {
                if (line.toLowerCase().contains(word) && ambiguity) {
                    System.out.println("returning words.get(0)");
                    return words.get(0);
                } else if (line.toLowerCase().contains(word) && !ambiguity) {
                    System.out.println("returning words");
                    return word;
                }
            }
        }

        System.out.println("returning null in checkforwords");
        return "null";
    }

    public static void setAlphaAnimation(View v) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha",  1f, .3f);
        fadeOut.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", .3f, 1f);
        fadeIn.setDuration(2000);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
            }
        });
        mAnimationSet.start();
    }

    public static void animateTxt(TypeWriter tw, String txt){
        tw.setText("");
        tw.setCharacterDelay(40);
        tw.animateText(txt);
    }

}
