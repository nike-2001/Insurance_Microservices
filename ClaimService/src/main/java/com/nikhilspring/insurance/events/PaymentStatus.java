/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.nikhilspring.insurance.events;
@org.apache.avro.specific.AvroGenerated
public enum PaymentStatus implements org.apache.avro.generic.GenericEnumSymbol<PaymentStatus> {
  COMPLETED, FAILED, PENDING  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"PaymentStatus\",\"namespace\":\"com.nikhilspring.insurance.events\",\"symbols\":[\"COMPLETED\",\"FAILED\",\"PENDING\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
}
