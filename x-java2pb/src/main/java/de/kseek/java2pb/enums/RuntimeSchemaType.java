package de.kseek.java2pb.enums;

public enum RuntimeSchemaType {

	ArraySchema,
	ObjectSchema,
	MapSchema,
	PolymorphicEnumSchema;
	
	public static RuntimeSchemaType findByName(String name) {
		for (RuntimeSchemaType value : values()) {
			if (value.name().equals(name)) {
				return value;
			}
		}
		return null;
	}
	
}
