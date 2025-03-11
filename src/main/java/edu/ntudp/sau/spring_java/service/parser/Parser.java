package edu.ntudp.sau.spring_java.service.parser;

import java.util.List;

public interface Parser<T> {
    List<T> parseProducts(String search, int pageLimit);
}
