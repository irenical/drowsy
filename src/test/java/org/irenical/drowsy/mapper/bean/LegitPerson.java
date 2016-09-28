package org.irenical.drowsy.mapper.bean;

public class LegitPerson {

  private int Id;   // fields "cammel"-cased for testing purposes

  private String Name;

  public void setId(int id) {
    this.Id = id;
  }

  public void setName(String name) {
    this.Name = name;
  }

  public int getId() {
    return Id;
  }

  public String getName() {
    return Name;
  }

}
