package org.toradocu.extractor;

import java.util.Optional;

public interface InterfaceTag {

  public String toString();

  public int hashCode();

  public boolean equals(Object obj);

  public Optional<String> getCondition();

  public void setCondition(String s);

  public String getComment();
}
