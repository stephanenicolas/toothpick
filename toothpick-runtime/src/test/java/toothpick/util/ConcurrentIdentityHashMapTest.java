package toothpick.util;

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConcurrentIdentityHashMapTest {
  static final Integer ZERO = new Integer(0);
  static final Integer ONE = new Integer(1);
  static final Integer TWO = new Integer(2);
  static final Integer THREE = new Integer(3);
  static final Integer FOUR = new Integer(4);
  static final Integer FIVE = new Integer(5);
  static final Integer SIX = new Integer(6);
  public static long shortDelayMs;
  public static long smallDelayMs;
  public static long mediumDelayMs;
  public static long longDelayMs;
  /**
   * Flag set true if any threadAssert methods fail
   */
  volatile boolean threadFailed;

  /**
   * Create a map from Integers 1-5 to Strings "A"-"E".
   */
  private static ConcurrentIdentityHashMap map5() {
    ConcurrentIdentityHashMap map = new ConcurrentIdentityHashMap(5);
    assertTrue(map.isEmpty());
    map.put(ONE, "A");
    map.put(TWO, "B");
    map.put(THREE, "C");
    map.put(FOUR, "D");
    map.put(FIVE, "E");
    assertFalse(map.isEmpty());
    assertEquals(5, map.size());
    return map;
  }

  /**
   * clear removes all pairs
   */
  @Test
  public void testClear() {
    ConcurrentIdentityHashMap map = map5();
    map.clear();
    assertEquals(map.size(), 0);
  }

  /**
   * Maps with same contents are equal
   */
  @Test
  public void testEquals() {
    ConcurrentIdentityHashMap map1 = map5();
    ConcurrentIdentityHashMap map2 = map5();
    assertEquals(map1, map2);
    assertEquals(map2, map1);
    map1.clear();
    assertFalse(map1.equals(map2));
    assertFalse(map2.equals(map1));
  }

  /**
   * contains returns true for contained value
   */
  @Test
  public void testContains() {
    ConcurrentIdentityHashMap map = map5();
    assertTrue(map.contains("A"));
    assertFalse(map.contains("Z"));
  }

  /**
   * containsKey returns true for contained key
   */
  @Test
  public void testContainsKey() {
    ConcurrentIdentityHashMap map = map5();
    assertTrue(map.containsKey(ONE));
    assertFalse(map.containsKey(ZERO));
  }

  /**
   * containsValue returns true for held values
   */
  @Test
  public void testContainsValue() {
    ConcurrentIdentityHashMap map = map5();
    assertTrue(map.contains("A"));
    assertFalse(map.contains("Z"));
  }

  /**
   * enumeration returns an enumeration containing the correct
   * elements
   */
  @Test
  public void testEnumeration() {
    ConcurrentIdentityHashMap map = map5();
    Enumeration e = map.elements();
    int count = 0;
    while (e.hasMoreElements()) {
      count++;
      e.nextElement();
    }
    assertEquals(5, count);
  }

  /**
   * Clone creates an equal map
   */
  @Test
  public void testClone() {
    ConcurrentIdentityHashMap map = map5();
    ConcurrentIdentityHashMap m2 = (ConcurrentIdentityHashMap) (map.clone());
    assertEquals(map, m2);
  }

  /**
   * get returns the correct element at the given key,
   * or null if not present
   */
  @Test
  public void testGet() {
    ConcurrentIdentityHashMap map = map5();
    assertEquals("A", (String) map.get(ONE));
    ConcurrentIdentityHashMap empty = new ConcurrentIdentityHashMap();
    assertNull(map.get("anything"));
  }

  /**
   * isEmpty is true of empty map and false for non-empty
   */
  @Test
  public void testIsEmpty() {
    ConcurrentIdentityHashMap empty = new ConcurrentIdentityHashMap();
    ConcurrentIdentityHashMap map = map5();
    assertTrue(empty.isEmpty());
    assertFalse(map.isEmpty());
  }

  /**
   * keys returns an enumeration containing all the keys from the map
   */
  @Test
  public void testKeys() {
    ConcurrentIdentityHashMap map = map5();
    Enumeration e = map.keys();
    int count = 0;
    while (e.hasMoreElements()) {
      count++;
      e.nextElement();
    }
    assertEquals(5, count);
  }

  /**
   * keySet returns a Set containing all the keys
   */
  @Test
  public void testKeySet() {
    ConcurrentIdentityHashMap map = map5();
    Set s = map.keySet();
    assertEquals(5, s.size());
    assertTrue(s.contains(ONE));
    assertTrue(s.contains(TWO));
    assertTrue(s.contains(THREE));
    assertTrue(s.contains(FOUR));
    assertTrue(s.contains(FIVE));
  }

  /**
   * values collection contains all values
   */
  @Test
  public void testValues() {
    ConcurrentIdentityHashMap map = map5();
    Collection s = map.values();
    assertEquals(5, s.size());
    assertTrue(s.contains("A"));
    assertTrue(s.contains("B"));
    assertTrue(s.contains("C"));
    assertTrue(s.contains("D"));
    assertTrue(s.contains("E"));
  }

  /**
   * entrySet contains all pairs
   */
  @Test
  public void testEntrySet() {
    ConcurrentIdentityHashMap map = map5();
    Set s = map.entrySet();
    assertEquals(5, s.size());
    Iterator it = s.iterator();
    while (it.hasNext()) {
      Map.Entry e = (Map.Entry) it.next();
      assertTrue(
          (e.getKey().equals(ONE) && "A".equals(e.getValue())) || (e.getKey().equals(TWO) && "B".equals(e.getValue())) || (e.getKey().equals(THREE)
              && "C".equals(e.getValue())) || (e.getKey().equals(FOUR) && "D".equals(e.getValue())) || (e.getKey().equals(FIVE) && "E".equals(
              e.getValue())));
    }
  }

  /**
   * putAll  adds all key-value pairs from the given map
   */
  @Test
  public void testPutAll() {
    ConcurrentIdentityHashMap empty = new ConcurrentIdentityHashMap();
    ConcurrentIdentityHashMap map = map5();
    empty.putAll(map);
    assertEquals(5, empty.size());
    assertTrue(empty.containsKey(ONE));
    assertTrue(empty.containsKey(TWO));
    assertTrue(empty.containsKey(THREE));
    assertTrue(empty.containsKey(FOUR));
    assertTrue(empty.containsKey(FIVE));
  }

  /**
   * putIfAbsent works when the given key is not present
   */
  @Test
  public void testPutIfAbsent() {
    ConcurrentIdentityHashMap map = map5();
    map.putIfAbsent(SIX, "Z");
    assertTrue(map.containsKey(SIX));
  }

  /**
   * putIfAbsent does not add the pair if the key is already present
   */
  @Test
  public void testPutIfAbsent2() {
    ConcurrentIdentityHashMap map = map5();
    assertEquals("A", map.putIfAbsent(ONE, "Z"));
  }

  /**
   * replace fails when the given key is not present
   */
  @Test
  public void testReplace() {
    ConcurrentIdentityHashMap map = map5();
    assertNull(map.replace(SIX, "Z"));
    assertFalse(map.containsKey(SIX));
  }

  /**
   * replace succeeds if the key is already present
   */
  @Test
  public void testReplace2() {
    ConcurrentIdentityHashMap map = map5();
    assertNotNull(map.replace(ONE, "Z"));
    assertEquals("Z", map.get(ONE));
  }

  /**
   * replace value fails when the given key not mapped to expected value
   */
  @Test
  public void testReplaceValue() {
    ConcurrentIdentityHashMap map = map5();
    assertEquals("A", map.get(ONE));
    assertFalse(map.replace(ONE, "Z", "Z"));
    assertEquals("A", map.get(ONE));
  }

  /**
   * replace value succeeds when the given key mapped to expected value
   */
  @Test
  public void testReplaceValue2() {
    ConcurrentIdentityHashMap map = map5();
    assertEquals("A", map.get(ONE));
    assertTrue(map.replace(ONE, "A", "Z"));
    assertEquals("Z", map.get(ONE));
  }

  /**
   * remove removes the correct key-value pair from the map
   */
  @Test
  public void testRemove() {
    ConcurrentIdentityHashMap map = map5();
    map.remove(FIVE);
    assertEquals(4, map.size());
    assertFalse(map.containsKey(FIVE));
  }

  /**
   * remove(key,value) removes only if pair present
   */
  @Test
  public void testRemove2() {
    ConcurrentIdentityHashMap map = map5();
    map.remove(FIVE, "E");
    assertEquals(4, map.size());
    assertFalse(map.containsKey(FIVE));
    map.remove(FOUR, "A");
    assertEquals(4, map.size());
    assertTrue(map.containsKey(FOUR));
  }

  /**
   * size returns the correct values
   */
  @Test
  public void testSize() {
    ConcurrentIdentityHashMap map = map5();
    ConcurrentIdentityHashMap empty = new ConcurrentIdentityHashMap();
    assertEquals(0, empty.size());
    assertEquals(5, map.size());
  }

  /**
   * toString contains toString of elements
   */
  @Test
  public void testToString() {
    ConcurrentIdentityHashMap map = map5();
    String s = map.toString();
    for (int i = 1; i <= 5; ++i) {
      assertTrue(s.indexOf(String.valueOf(i)) >= 0);
    }
  }

  // Exception tests

  /**
   * Cannot create with negative capacity
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor1() {
    new ConcurrentIdentityHashMap(-1, 0, 1);
    fail("Should throw exception");
  }

  /**
   * Cannot create with negative concurrency level
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor2() {
    new ConcurrentIdentityHashMap(1, 0, -1);
    fail("Should throw exception");
  }

  /**
   * Cannot create with only negative capacity
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor3() {
    new ConcurrentIdentityHashMap(-1);
    fail("Should throw exception");
  }

  /**
   * get(null) returns value
   */
  @Test
  public void testGet_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    assertEquals(c.get(null), null);
  }

  /**
   * containsKey(null) returns false
   */
  @Test
  public void testContainsKey_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    assertFalse(c.containsKey(null));
  }

  /**
   * containsValue(null) throws NPE
   */
  @Test(expected = NullPointerException.class)
  public void testContainsValue_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.containsValue(null);
    fail("Should throw exception");
  }

  /**
   * contains(null) throws NPE
   */
  @Test(expected = NullPointerException.class)
  public void testContains_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.contains(null);
    fail("Should throw exception");
  }

  /**
   * put(null,x) throws NPE
   */
  @Test
  public void testPut1_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    assertEquals(null, c.put(null, "whatever"));
  }

  /**
   * put(x, null) throws NPE
   */
  @Test(expected = NullPointerException.class)
  public void testPut2_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.put("whatever", null);
    fail("Should throw exception");
  }

  /**
   * putIfAbsent(null, x) returns previous value
   */
  @Test
  public void testPutIfAbsent1_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    assertEquals(c.putIfAbsent(null, "whatever"), null);
  }

  /**
   * replace(null, x) replaces previous value
   */
  @Test
  public void testReplace_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.replace(null, "whatever");
  }

  /**
   * replace(null, x, y) previous value
   */
  @Test
  public void testReplaceValue_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.replace(null, ONE, "whatever");
  }

  /**
   * putIfAbsent(x, null) throws NPE
   */
  @Test(expected = NullPointerException.class)
  public void testPutIfAbsent2_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.putIfAbsent("whatever", null);
    fail("Should throw exception");
  }

  /**
   * replace(x, null) throws NPE
   */
  @Test(expected = NullPointerException.class)
  public void testReplace2_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.replace("whatever", null);
    fail("Should throw exception");
  }

  /**
   * replace(x, null, y) throws NPE
   */
  @Test(expected = NullPointerException.class)
  public void testReplaceValue2_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.replace("whatever", null, "A");
    fail("Should throw exception");
  }

  /**
   * replace(x, y, null) throws NPE
   */
  @Test(expected = NullPointerException.class)
  public void testReplaceValue3_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.replace("whatever", ONE, null);
    fail("Should throw exception");
  }

  /**
   * remove(null) returns previous value
   */
  @Test
  public void testRemove1_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.put("sadsdf", "asdads");
    assertEquals(c.remove(null), null);
  }

  /**
   * remove(null, x) returns previous value
   */
  @Test
  public void testRemove2_NullPointerException() {
    ConcurrentIdentityHashMap c = new ConcurrentIdentityHashMap(5);
    c.put("sadsdf", "asdads");
    assertFalse(c.remove(null, "whatever"));
  }

  /**
   * SetValue of an EntrySet entry sets value in the map.
   */
  @Test
  public void testSetValueWriteThrough() {
    // Adapted from a bug report by Eric Zoerner
    ConcurrentIdentityHashMap map = new ConcurrentIdentityHashMap(2, 5.0f, 1);
    assertTrue(map.isEmpty());
    for (int i = 0; i < 20; i++) {
      map.put(new Integer(i), new Integer(i));
    }
    assertFalse(map.isEmpty());
    Map.Entry entry1 = (Map.Entry) map.entrySet().iterator().next();

    // assert that entry1 is not 16
    assertTrue("entry is 16, test not valid", !entry1.getKey().equals(new Integer(16)));

    // remove 16 (a different key) from map
    // which just happens to cause entry1 to be cloned in map
    map.remove(new Integer(16));
    entry1.setValue("XYZ");
    assertTrue(map.containsValue("XYZ")); // fails
  }

  /**
   * Return the shortest timed delay. This could
   * be reimplemented to use for example a Property.
   */
  protected long getShortDelay() {
    return 50;
  }

  /**
   * Set delays as multiples of SHORT_DELAY.
   */
  protected void setDelays() {
    shortDelayMs = getShortDelay();
    smallDelayMs = shortDelayMs * 5;
    mediumDelayMs = shortDelayMs * 10;
    longDelayMs = shortDelayMs * 50;
  }

  /**
   * Initialize test to indicate that no thread assertions have failed
   */
  public void setUp() {
    setDelays();
    threadFailed = false;
  }

  /**
   * Trigger test case failure if any thread assertions have failed
   */
  public void tearDown() {
    assertFalse(threadFailed);
  }
}