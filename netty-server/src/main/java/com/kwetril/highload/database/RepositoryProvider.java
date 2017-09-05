package com.kwetril.highload.database;

public class RepositoryProvider {
    public static final IRepository repo = new SingleThreadRepo();
}
