package com.instagirls.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {

    private final static List<String> endearments = new ArrayList<>();
    private final static List<String> insults = new ArrayList<>();
    private final static Random rand = new Random();

    static {
        endearments.add("honey");
        endearments.add("sweetie");
        endearments.add("darling");
        endearments.add("babe");
        endearments.add("baby");
        endearments.add("tiger");
        endearments.add("superman");
        endearments.add("bunny");
        endearments.add("sweetheart");
        endearments.add("sugar");
        endearments.add("dear");
        endearments.add("handsome");
        endearments.add("charming");
        endearments.add("Mr. Perfect");
        endearments.add("captain");

        insults.add("whore");
        insults.add("bitch");
        insults.add("slut");
        insults.add("cunt");
        insults.add("flat ass");
        insults.add("ugly");
        insults.add("abomination");
        insults.add("human mistake");
        insults.add("crewcabanger");
        insults.add("abortation victim");
        insults.add("borat sister");
    }

    public static String generateEndearment() {
        return getRandomElement(endearments);
    }

    public static String generateInsult() {
        return getRandomElement(insults);
    }

    public static <T> T getRandomElement(final List<T> list) {
        return list.get(rand.nextInt(list.size()));
    }

}
