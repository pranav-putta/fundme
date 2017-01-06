package net.codealizer.fundme.assets;

import java.util.Comparator;

public class ItemComparator implements Comparator<Item> {

        public ItemComparator() {
            
        }
        
        @Override
        public int compare(Item item, Item t1) {
            long compareLongDate = item.getDateCreated();

            return (int) (compareLongDate - t1.dateCreated);
        }
    }