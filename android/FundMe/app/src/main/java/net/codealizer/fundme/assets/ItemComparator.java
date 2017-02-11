package net.codealizer.fundme.assets;

import java.util.Comparator;

/**
 * Comparing class for an item
 *
 * This class essentially ranks the item in order by descending date
 */

public class ItemComparator implements Comparator<Item> {

        public ItemComparator() {
            
        }
        
        @Override
        public int compare(Item item, Item t1) {
            long compareLongDate = item.getDateCreated();

            return (int) (t1.dateCreated - compareLongDate);
        }
    }