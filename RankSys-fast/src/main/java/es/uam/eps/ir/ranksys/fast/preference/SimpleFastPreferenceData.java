/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.fast.preference;

import static es.uam.eps.ir.ranksys.core.util.FastStringSplitter.split;
import es.uam.eps.ir.ranksys.core.util.parsing.DoubleParser;
import es.uam.eps.ir.ranksys.core.util.parsing.Parser;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.ranksys.core.util.iterators.StreamDoubleIterator;
import org.ranksys.core.util.iterators.StreamIntIterator;

/**
 * Simple implementation of FastPreferenceData backed by nested lists.
 *
 * @author Saúl Vargas (saul.vargas@uam.es)
 *
 * @param <U> type of the users
 * @param <I> type of the items
 */
public class SimpleFastPreferenceData<U, I> extends AbstractFastPreferenceData<U, I> implements Serializable {

    private final int numPreferences;
    private final List<List<IdxPref>> uidxList;
    private final List<List<IdxPref>> iidxList;

    /**
     * Constructor.
     *
     * @param numPreferences number of total preferences
     * @param uidxList list of lists of preferences by user index
     * @param iidxList list of lists of preferences by item index
     * @param uIndex user index
     * @param iIndex item index
     */
    protected SimpleFastPreferenceData(int numPreferences, List<List<IdxPref>> uidxList, List<List<IdxPref>> iidxList, FastUserIndex<U> uIndex, FastItemIndex<I> iIndex) {
        super(uIndex, iIndex);
        this.numPreferences = numPreferences;
        this.uidxList = uidxList;
        this.iidxList = iidxList;
    }

    @Override
    public int numUsers(int iidx) {
        if (iidxList.get(iidx) == null) {
            return 0;
        }
        return iidxList.get(iidx).size();
    }

    @Override
    public int numItems(int uidx) {
        if (uidxList.get(uidx) == null) {
            return 0;
        }
        return uidxList.get(uidx).size();
    }

    @Override
    public Stream<IdxPref> getUidxPreferences(int uidx) {
        if (uidxList.get(uidx) == null) {
            return Stream.empty();
        } else {
            return uidxList.get(uidx).stream();
        }
    }

    @Override
    public Stream<IdxPref> getIidxPreferences(int iidx) {
        if (iidxList.get(iidx) == null) {
            return Stream.empty();
        } else {
            return iidxList.get(iidx).stream();
        }
    }

    @Override
    public int numPreferences() {
        return numPreferences;
    }

    @Override
    public IntStream getUidxWithPreferences() {
        return IntStream.range(0, numUsers())
                .filter(uidx -> uidxList.get(uidx) != null);
    }

    @Override
    public IntStream getIidxWithPreferences() {
        return IntStream.range(0, numItems())
                .filter(iidx -> iidxList.get(iidx) != null);
    }

    @Override
    public int numUsersWithPreferences() {
        return (int) uidxList.stream()
                .filter(iv -> iv != null).count();
    }

    @Override
    public int numItemsWithPreferences() {
        return (int) iidxList.stream()
                .filter(iv -> iv != null).count();
    }

    @Override
    public IntIterator getUidxIidxs(final int uidx) {
        return new StreamIntIterator(getUidxPreferences(uidx).mapToInt(pref -> pref.idx));
    }

    @Override
    public DoubleIterator getUidxVs(final int uidx) {
        return new StreamDoubleIterator(getUidxPreferences(uidx).mapToDouble(pref -> pref.v));
    }

    @Override
    public IntIterator getIidxUidxs(final int iidx) {
        return new StreamIntIterator(getIidxPreferences(iidx).mapToInt(pref -> pref.idx));
    }

    @Override
    public DoubleIterator getIidxVs(final int iidx) {
        return new StreamDoubleIterator(getIidxPreferences(iidx).mapToDouble(pref -> pref.v));
    }

    @Override
    public boolean useIteratorsPreferentially() {
        return false;
    }

    /**
     * Load preferences from a file.
     *
     * Each line is a different preference, with tab-separated fields indicating user, item, weight and other information.
     *
     * @param <U> type of the users
     * @param <I> type of the items
     * @param path path of the input file
     * @param uParser user type parser
     * @param iParser item type parser
     * @param dp double parse
     * @param uIndex user index
     * @param iIndex item index
     * @return a simple list-of-lists FastPreferenceData with the information read
     * @throws IOException when path does not exists of IO error
     */
    public static <U, I> SimpleFastPreferenceData<U, I> load(String path, Parser<U> uParser, Parser<I> iParser, DoubleParser dp, FastUserIndex<U> uIndex, FastItemIndex<I> iIndex) throws IOException {
        return load(new FileInputStream(path), uParser, iParser, dp, uIndex, iIndex);
    }

    /**
     * Load preferences from an input stream.
     *
     * Each line is a different preference, with tab-separated fields indicating user, item, weight and other information.
     *
     * @param <U> type of the users
     * @param <I> type of the items
     * @param in input stream to read from
     * @param uParser user type parser
     * @param iParser item type parser
     * @param dp double parse
     * @param uIndex user index
     * @param iIndex item index
     * @return a simple list-of-lists FastPreferenceData with the information read
     * @throws IOException when path does not exists of IO error
     */
    public static <U, I> SimpleFastPreferenceData<U, I> load(InputStream in, Parser<U> uParser, Parser<I> iParser, DoubleParser dp, FastUserIndex<U> uIndex, FastItemIndex<I> iIndex) throws IOException {
        AtomicInteger numPreferences = new AtomicInteger();

        List<List<IdxPref>> uidxList = new ArrayList<>();
        for (int uidx = 0; uidx < uIndex.numUsers(); uidx++) {
            uidxList.add(null);
        }

        List<List<IdxPref>> iidxList = new ArrayList<>();
        for (int iidx = 0; iidx < iIndex.numItems(); iidx++) {
            iidxList.add(null);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            reader.lines().forEach(l -> {
                CharSequence[] tokens = split(l, '\t', 4);
                U user = uParser.parse(tokens[0]);
                I item = iParser.parse(tokens[1]);
                double value;
                if (tokens.length >= 3) {
                    value = dp.parse(tokens[2]);
                } else {
                    value = dp.parse(null);
                }

                int uidx = uIndex.user2uidx(user);
                int iidx = iIndex.item2iidx(item);

                numPreferences.incrementAndGet();

                List<IdxPref> uList = uidxList.get(uidx);
                if (uList == null) {
                    uList = new ArrayList<>();
                    uidxList.set(uidx, uList);
                }
                uList.add(new IdxPref(iidx, value));

                List<IdxPref> iList = iidxList.get(iidx);
                if (iList == null) {
                    iList = new ArrayList<>();
                    iidxList.set(iidx, iList);
                }
                iList.add(new IdxPref(uidx, value));
            });
        }

        return new SimpleFastPreferenceData<>(numPreferences.intValue(), uidxList, iidxList, uIndex, iIndex);
    }

}
