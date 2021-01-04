package com.example.stegopaybeta;

import android.content.Intent;
import android.icu.text.StringSearch;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import net.amygdalum.stringsearchalgorithms.search.MatchOption;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.stringsearchalgorithms.search.chars.AhoCorasick;
import net.amygdalum.stringsearchalgorithms.search.chars.Horspool;
import net.amygdalum.stringsearchalgorithms.search.chars.SetBackwardOracleMatching;
import net.amygdalum.stringsearchalgorithms.search.chars.SetHorspool;
import net.amygdalum.stringsearchalgorithms.search.chars.StringSearchAlgorithm;
import net.amygdalum.stringsearchalgorithms.search.chars.WuManber;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.io.StringByteProvider;
import net.amygdalum.util.io.StringCharProvider;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_EMPTY;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;

public class Launch extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch);


        AhoCorasick stringSearch = new AhoCorasick (asList("011010101010101010101010", "01101010101010101010101", "0110101010101010101010", "011010101010101010101", "01101010101010101010", "0110101010101010101", "011010101010101010", "01101010101010101", "0110101010101010", "011010101010101", "01101010101010", "0110101010101", "011010101010", "01101010101", "0110101010", "011010101", "01101010", "0110101", "011010", "01101", "0110", "011"));
        CharProvider text = new StringCharProvider ("010101010110110101110101", 0);
        StringFinder finder = stringSearch.createFinder(text, LONGEST_MATCH);

        long startTime = System.nanoTime();

        /* ... the code being measured starts ... */

        List<StringMatch> all = finder.findAll();
        

        int max = Integer.MIN_VALUE;
        String maxString = all.get(0).toString();

        for(int i = 0; i < all.size(); i++){
            if(all.get(i).length() > max) {
                max = all.get(i).length();
                maxString = all.get(i).toString();
            }
        }

        System.out.println("Loop max: " + maxString);
        System.out.println("Collections max: " + Collections.max(all));


        //StringMatch longestString = all.get(0);

//        for (int i = 0; i < all.size(); i++) {
//            //if (all.get(i).length() >= longestString.length()) {
//                System.out.println(all.get(i).toString() + " with length " + all.get(i).length());
//           //}
//        }

        /* ... the code being measured ends ... */

        long endTime = System.nanoTime();

        // get difference of two nanoTime values
        long timeElapsed = endTime - startTime;

        System.out.println("Execution time in nanoseconds  : " + timeElapsed);

        System.out.println("Execution time in milliseconds : " +
                timeElapsed / 1000000);




    }




    public void encodeButtonOnClick(View view) {
        Intent i = new Intent(this, AddCard.class);
        startActivity(i);
    }

    public void decodeButtonOnClick(View view) {
        Intent i = new Intent(this, Card.class);
        startActivity(i);
    }


}
