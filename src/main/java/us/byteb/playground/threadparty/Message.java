package us.byteb.playground.threadparty;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Message {
  private final int source;
  private final int value;
  private final long created = System.nanoTime();

  public Message(final int source, final int value) {
    this.source = source;
    this.value = value;
  }

  public int getSource() {
    return source;
  }

  public int getValue() {
    return value;
  }

  public long getCreated() {
    return created;
  }

  @Override
  public String toString() {
    return reflectionToString(this, SHORT_PREFIX_STYLE);
  }

  @Override
  public boolean equals(final Object o) {
    return reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return reflectionHashCode(this);
  }
}
