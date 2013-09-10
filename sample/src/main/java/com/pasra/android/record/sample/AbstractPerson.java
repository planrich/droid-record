package com.pasra.android.record.sample;

public class AbstractPerson {

    private String mName;
    private Integer mAge;
    private Long mId;
    
    public AbstractPerson(Long id) {

        this.mId = id;
    }
    
    public String getName() { return mName; }
    public void setName(String value) { mName = value; }
    public Integer getAge() { return mAge; }
    public void setAge(Integer value) { mAge = value; }
    public Long getId() { return mId; }
    public void setId(Long value) { mId = value; }
}
