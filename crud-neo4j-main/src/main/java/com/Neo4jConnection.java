package com;

import org.neo4j.driver.*;

public class Neo4jConnection {

    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "tavo2000";

    private Driver driver;

    public Neo4jConnection() {
        this.driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
    }

    public void close() {
        this.driver.close();
    }

    public Session getSession() {
        return this.driver.session();
    }
}