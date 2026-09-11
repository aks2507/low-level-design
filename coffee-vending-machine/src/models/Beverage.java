package models;

import java.util.Map;

public record Beverage (
    String code,
    String name,
    Integer price,
    Map<Ingredient, Integer> recipe) {}
