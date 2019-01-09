package ru.nahk.folio.utils;

import java.util.Arrays;

/**
 * Simple array list implementation that allows to add and remove ranges.
 * @param <TElement>
 */
public class DynamicArray<TElement> {
    /**
     * Maximum array size.
     */
    private static final int MaxSize = 65535;

    /**
     * Elements buffer.
     */
    private TElement[] mItems;

    /**
     * Current number of elements.
     */
    private int mCount;

    /**
     * Creates new instance of the {@link DynamicArray} class.
     */
    @SuppressWarnings("unchecked")
    public DynamicArray() {
        mItems = (TElement[]) new Object[4];
    }

    /**
     * Gets the number of elements in the array.
     * @return Number of elements.
     */
    public int size() {
        return mCount;
    }

    /**
     * Adds an item to the array.
     * @param item Item to add.
     */
    public void add(TElement item) {
        if (mItems.length == mCount) {
            mItems = Arrays.copyOf(mItems, Math.min(MaxSize, mItems.length * 2));
        }

        mItems[mCount++] = item;
    }

    /**
     * Adds all elements from another {@link DynamicArray} to this instance.
     * @param startIndex Index to start inserting elements at.
     * @param items Another array that acts as a source.
     */
    public void addRange(int startIndex, DynamicArray<TElement> items) {
        if (items.size() == 0) {
            return;
        }

        // Ensure enough capacity for new items
        int requiredSize = mItems.length + items.size();
        if (mItems.length < requiredSize) {
            mItems = Arrays.copyOf(mItems, Math.min(MaxSize, requiredSize));
        }

        // Make room for inserted range
        System.arraycopy(
            mItems, startIndex,
            mItems, startIndex + items.size(),
            mItems.length - startIndex - items.size());

        // Insert new items
        System.arraycopy(
            items.mItems, 0,
            mItems, startIndex,
            items.size());

        mCount += items.size();
    }

    /**
     * Gets an element at the specified position.
     * @param position Element position.
     * @return Element at given position.
     */
    public TElement get(int position) {
        return mItems[position];
    }

    /**
     * Removes range of elements.
     * @param startIndex Index to start removing elements at.
     * @param count Number of elements to remove.
     */
    public void removeRange(int startIndex, int count) {
        if (count <= 0) {
            return;
        }

        System.arraycopy(
            mItems, startIndex + count,
            mItems, startIndex,
            mItems.length - startIndex - count);

        mCount -= count;

        Arrays.fill(mItems, mCount, mCount + count - 1, null);
    }

    /**
     * Removes all elements from the array.
     */
    public void clear() {
        Arrays.fill(mItems, null);
        mCount = 0;
    }
}
