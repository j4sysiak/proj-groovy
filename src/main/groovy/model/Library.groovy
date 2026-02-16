package model

import groovy.transform.ToString
import groovy.transform.Canonical

@Canonical
class Library {
    String name
    List<Book> books = []
}

@Canonical
class Book {
    String title
    Author author
}

@Canonical
class Author {
    String lastName
}
