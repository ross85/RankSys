/*
 * Copyright (C) 2015 RankSys http://ranksys.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.metrics.basic;

import es.uam.eps.ir.ranksys.core.Recommendation;
import es.uam.eps.ir.ranksys.metrics.AbstractRecommendationMetric;
import es.uam.eps.ir.ranksys.metrics.rel.RelevanceModel;
import es.uam.eps.ir.ranksys.metrics.rel.RelevanceModel.UserRelevanceModel;

/**
 * K-call metric. Penalizes recommendations retrieving less than k relevant
 * documents.<br>
 * 
 * Chen, H., Karger, D. R. (2006). Less is More: Probabilistic Models for Retrieving Fewer Relevant Documents. SIGIR'06. doi:10.1145/1148170.1148245
 *
 * @author Saúl Vargas (Saul.Vargas@mendeley.com)
 */
public class KCall<U, I> extends AbstractRecommendationMetric<U, I> {

    private final RelevanceModel<U, I> relModel;
    private final int cutoff;
    private final int k;

    public KCall(int cutoff, int k, RelevanceModel<U, I> relModel) {
        this.relModel = relModel;
        this.cutoff = cutoff;
        this.k = k;
    }

    @Override
    public double evaluate(Recommendation<U, I> recommendation) {
        UserRelevanceModel<U, I> urm = relModel.getModel(recommendation.getUser());
        
        return recommendation.getItems().stream()
                .limit(cutoff)
                .filter(is -> urm.isRelevant(is.id))
                .count() >= k ? 1.0 : 0.0;
    }

}
