package ru.home.concurrency.immutable.person;

public class Main {

	public static void main(String[] args) {

		ImmutablePerson person = new Person("first").immutable();

		System.out.println(person.getName());

		Person mutable = (Person) person;
		mutable.setName("second");

		System.out.println(mutable.getName());
	}
}
