package com.kwetril.highload.database;

public class RepositoryProvider {
    public static IRepository repo = new ConcurrentHashMapRepo();
}
