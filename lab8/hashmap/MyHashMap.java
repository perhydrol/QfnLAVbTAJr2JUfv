package hashmap;

import java.util.*;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private double maxLoad = 0.75;
    private int bucketsCount = 16;
    private int itemCount = 0;

    /**
     * Constructors
     */
    public MyHashMap() {
        buckets = createTable(bucketsCount);
    }

    public MyHashMap(int initialSize) {
        buckets = createTable(initialSize);
        bucketsCount = initialSize;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        bucketsCount = initialSize;
        this.maxLoad = maxLoad;
    }

    @Override
    public void clear() {
        bucketsCount = 16;
        itemCount = 0;
        buckets = createTable(16);
    }

    private int getHashCode(K key, int l) {
        int hashCode = Math.abs(key.hashCode());
        return hashCode % l;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public V get(K key) {
        int hashCode = getHashCode(key, bucketsCount);
        for (Node current : buckets[hashCode]) {
            if (current.key.equals(key)) {
                return current.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return itemCount;
    }

    private void resize(int size) {
        Collection<Node>[] newBuckets = createTable(size);
        for (int i = 0; i < bucketsCount; i += 1) {
            for (Node current : buckets[i]) {
                int newHashCode = getHashCode(current.key, newBuckets.length);
                newBuckets[newHashCode].add(current);
            }
        }
        buckets = newBuckets;
        bucketsCount = size;
    }

    private Node getNodeInCollection(K key, Collection<Node> list) {
        for (Node temp : list) {
            if (temp.key.equals(key)) {
                return temp;
            }
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        if ((double) itemCount / bucketsCount >= maxLoad) {
            resize((int) (bucketsCount * 1.5));
        }
        int hashCode = getHashCode(key, bucketsCount);
        Collection<Node> bucket = buckets[hashCode];
        Node existingNode = getNodeInCollection(key, bucket);

        if (existingNode == null) {
            bucket.add(createNode(key, value));
            itemCount += 1;
        } else {
            existingNode.value = value;
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> ret = new HashSet<>(itemCount);
        for (K temp : this) {
            ret.add(temp);
        }
        return ret;
    }

    @Override
    public V remove(K key) {
        int hashCode = getHashCode(key, bucketsCount);
        Iterator<Node> point = buckets[hashCode].iterator();
        while (point.hasNext()) {
            Node temp = point.next();
            if (temp.key.equals(key)) {
                point.remove();
                return temp.value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        return remove(key);
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIter();
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] temp = new Collection[tableSize];
        for (int i = 0; i < temp.length; i += 1) {
            temp[i] = createBucket();
        }
        return temp;
    }

    /**
     * An iterator that iterates over the keys of the dictionary.
     */
    private class MyHashMapIter implements Iterator<K> {
        private int cur;
        private Iterator<Node> point;
        private int count;


        MyHashMapIter() {
            cur = 0;
            count = 0;
            point = buckets[0].iterator();
        }

        @Override
        public boolean hasNext() {
            return count < itemCount;
        }

        @Override
        public K next() {
            if (point.hasNext()) {
                count += 1;
                return point.next().key;
            } else {
                point = getNextNonEmptyBucketIterator();
                if (point != null && point.hasNext()) {
                    count += 1;
                    return point.next().key;
                }
            }
            return null;
        }

        private Iterator<Node> getNextNonEmptyBucketIterator() {
            while (++cur < bucketsCount && (buckets[cur] == null || buckets[cur].isEmpty())) {

            }
            return (cur < bucketsCount) ? buckets[cur].iterator() : null;
        }
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

}
