package models;

public record DispenseResult(
    Beverage beverage,
    Integer price,
    Integer change) {}
