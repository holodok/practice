package com.gogaworm.praktika.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 26.03.2017.
 *
 * @author Ilona
 */
public class Student extends Person {
    public List<Person> teachers = new ArrayList<>(2);

    public Student(long id, int type, String name) {
        super(id, type, name);
    }

    public Student(int type, String name) {
        super(type, name);
    }

    public Student(Person person) {
        super(person.id, person.type, person.name);
    }
}
