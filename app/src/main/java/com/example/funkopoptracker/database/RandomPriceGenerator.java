package com.example.funkopoptracker.database;

import java.util.Random;

public class RandomPriceGenerator {

    private static final double MIN_PRICE = 5.0;
    private static final double MAX_PRICE = 5000.0;

    private static final double RARITY_1_MAX = 30.0;
    private static final double RARITY_2_MAX = 100.0;
    private static final double RARITY_3_MAX = 300.0;
    private static final double RARITY_4_MAX = 1000.0;

    //generate deterministic price for a pop
    public static double generatePrice(String name, int number) {
        long seed = createSeed(name, number);
        double basePrice = generateExponentialPrice(seed);
        double modifier = getVintageMultiplier(number);
        double finalPrice = basePrice * modifier;

        return Math.min(Math.max(finalPrice, MIN_PRICE), MAX_PRICE);
    }

    //convert price to rarity stars (1-5)
    public static int calculateRarity(double price) {
        if (price <= RARITY_1_MAX) return 1;
        if (price <= RARITY_2_MAX) return 2;
        if (price <= RARITY_3_MAX) return 3;
        if (price <= RARITY_4_MAX) return 4;
        return 5;
    }

    //create seed from name and number for consistency
    private static long createSeed(String name, int number) {
        return name.hashCode() + number;
    }

    //curve for realistic market (many cheap, few expensive)
    private static double generateExponentialPrice(long seed) {
        Random random = new Random(seed);
        double randomValue = random.nextDouble();

        //power curve
        double curvedValue = Math.pow(randomValue, 6.7);

        return MIN_PRICE + (curvedValue * (MAX_PRICE - MIN_PRICE));
    }

    //vintage pops get higher multipliers
    private static double getVintageMultiplier(int number) {
        //vintage tier (1-100)
        if (number <= 100) {
            return 3.0 - ((number - 1) / 100.0 * 1.5);
        }

        //mid-range tier (101-500)
        if (number <= 500) {
            return 1.5 - ((number - 100) / 400.0 * 0.5);
        }

        //modern tier (501+)
        Random numberRandom = new Random(number);
        return 0.8 + (numberRandom.nextDouble() * 0.4);
    }

    //generate new price based on previous price and day
    public static double generatePriceDelta(double previousPrice, String name, int number, int day) {
        long seed = createSeed(name, number) + day;
        Random random = new Random(seed);

        double roll = random.nextDouble();
        double change;

        if (roll < 0.01) {
            change = 0.20 + (random.nextDouble() * 0.30); //20% - 50%, 1% of the time
        } else {
            change = 0.01 + (random.nextDouble() * 0.04);//1% - 5%, 99% of the time
        }

        if (random.nextBoolean()) { //coin flip for neg/pos
            return Math.max(previousPrice * (1.0 + change), 0.67); //0.67 is lower bound for pop price3
        } else {
            return Math.max(previousPrice * (1.0 - change), 0.67);
        }
    }
}
