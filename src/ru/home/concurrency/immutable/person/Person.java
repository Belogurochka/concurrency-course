package ru.home.concurrency.immutable.person;

public class Person implements ImmutablePerson {
	private String name;

	public Person(String name) {
		this.name = name;
	}

	public ImmutablePerson immutable() {
		return new ImmutablePerson() {
			@Override
			public String getName() {
				return name;
			}
		};
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
