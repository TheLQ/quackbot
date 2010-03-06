/**
 * @(#)Sandbox.java
 *
 * Test Class
 *
 * @author  Lord.Quackstar
 */
 
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.io.*;
import javax.script.*;

import java.util.*;

public class Sandbox extends AbstractMap {
    
  private Map map;

  public Sandbox() {
    this(null);
  }

  public Sandbox(Map copy) {
    map = new HashMap();
    if (copy != null) {
      Iterator iter = copy.entrySet().iterator();
      while(iter.hasNext()) {
        Map.Entry entry = (Map.Entry)iter.next();
        add(entry.getKey(), entry.getValue());
      }
    }
  }

  public boolean containsKey(Object key) {
    Collection values = (Collection)map.get(key);
    return ((values != null) && (values.size() != 0));
  }
    
  public boolean containsValue(Object value) {
    Iterator iter = map.entrySet().iterator();
    boolean found = false;
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry)iter.next();
      Collection values = (Collection)entry.getValue();
      if (values.contains(value)) {
        found = true;
        break;
      }
    }
    return found;
  }


  public Object get(Object key) {
    return map.get(key);
  }

  public Object put(Object key, Object value) {
    if (!(value instanceof Collection)) {
      throw new IllegalArgumentException(value.getClass().toString());
    }
    Object original = get(key);
    map.put(key, value);
    return original;
  }

  public boolean add(Object key, Object value) {
    return getValues(key).add(value);
  }
    
  public boolean addAll(Object key, Collection values) {
    return getValues(key).addAll(values);
  }

  private Collection getValues(Object key) {
    Collection col = (Collection)map.get(key);
    if (col == null) {
      col = new HashSet();
      map.put(key, col);
    }
    return col;
  }

  public Object remove(Object key) {
    Object original = get(key);
    map.remove(key);
    return original;
  }

  public boolean remove(Object key, Object value) {
    Collection values = (Collection)map.get(key);
    if (values == null) {
      return false;
    } else {
      return values.remove(value);
    }
  }

  public void clear() {
    map.clear();
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("{");
    Iterator keys = map.keySet().iterator();
    boolean first = true;
    while (keys.hasNext()) {
      if (first) {
        first = false;
      } else {
        buff.append(", ");
      }
      Object key = keys.next();
      Collection values = getValues(key);
      buff.append("[" + key + ": " + values + "]");
    }
    buff.append("}");
    return buff.toString();
  }

  public Set entrySet() {
    int size = 0;
    Iterator iterKeys = map.entrySet().iterator();
    while (iterKeys.hasNext()) {
      Map.Entry entry = (Map.Entry)iterKeys.next();
      Collection values = (Collection)entry.getValue();
      Iterator iterValues = values.iterator();
      while (iterValues.hasNext()) { 
        size++;
        iterValues.next();
      }
    }

    final int finalSize = size;

    final Iterator entries = map.entrySet().iterator();

    return new AbstractSet() {
      int pos = 0;
      Map.Entry entry;
      Iterator values;

      public Iterator iterator() { 
        return new Iterator() {
          public void remove() {
            throw new UnsupportedOperationException();
          }
          public boolean hasNext() {
            return pos != finalSize;
          }
          public Object next() {
            while(true) {
              if (entry == null) {
                entry = (Map.Entry)entries.next();
                values = ((Collection)entry.getValue()).iterator();
              }
              Object key = entry.getKey();
              if (values.hasNext()) {
                Object value = values.next();
                pos++;
                return new Entry(key, value);
              } else {
                entry = null;
              }
            }
          }
        };
      }
      public int size() {
        return finalSize;
      }
    };
  }

  private static class Entry implements Map.Entry {
    Object key;
    Object value;

    Entry(Object key, Object value) {
      this.key = key;
      this.value = value;
    }

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object setValue(Object value) {
      Object oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      } else {
        Map.Entry e = (Map.Entry)o;
        return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
          (value==null ? e.getValue()==null : value.equals(e.getValue()));
      }
    }

    public int hashCode() {
      return ((value==null) ? 0 : value.hashCode());
    }

    public String toString() {
      return key+"="+value;
    }
  }
  public static void main (String args[]) {
    Map map = new HashMap();
    map.put("one", "two");
    map.put("three", "four");
    map.put("five", "six");
    Sandbox multi = new Sandbox(map);
    System.out.println(multi);
    multi.add("five", "seven");
    multi.add("five", "eight");
    multi.add("five", "nine");
    multi.add("five", "ten");
    multi.add("three", "seven");
    System.out.println(multi);
    multi.remove("three");
    System.out.println(multi);
  }
}

