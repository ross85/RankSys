/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.fast.index;

import es.uam.eps.ir.ranksys.core.index.ItemIndex;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Fast version of ItemIndex, where items are internally represented with 
 * numerical indices from 0 (inclusive) to the number of indexed items
 * (exclusive).
 *
 * @author Saúl Vargas (saul.vargas@uam.es)
 * 
 * @param <I> type of the items
 */
public interface FastItemIndex<I> extends ItemIndex<I> {

    @Override
    public default boolean containsItem(I i) {
        return item2iidx(i) >= 0;
    }

    @Override
    public default Stream<I> getAllItems() {
        return getAllIidx().mapToObj(iidx -> iidx2item(iidx));
    }
    
    /**
     * Gets all the indices of the items.
     *
     * @return a stream of indexes of items
     */
    public default IntStream getAllIidx() {
        return IntStream.range(0, numItems());
    }
    
    /**
     * Returns the index assigned to the item.
     *
     * @param i item
     * @return the index of the item, or -1 if the item does not exist
     */
    public int item2iidx(I i);

    /**
     * Returns the item represented with the index.
     *
     * @param iidx item index
     * @return the item whose index is iidx
     */
    public I iidx2item(int iidx);
}
